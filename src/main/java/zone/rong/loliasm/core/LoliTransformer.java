package zone.rong.loliasm.core;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.patches.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.objectweb.asm.Opcodes.*;

public class LoliTransformer implements IClassTransformer {

    Map<String, Function<byte[], byte[]>> transformations;

    public LoliTransformer() {
        LoliLogger.instance.info("The lolis are now preparing to bytecode manipulate your game.");
        transformations = new Object2ObjectOpenHashMap<>();
        if (LoliLoadingPlugin.isClient) {
            addTransformation("codechicken.lib.model.loader.blockstate.CCBlockStateLoader", bytes -> stripSubscribeEventAnnotation(bytes, "onModelBake", "onTextureStitchPre"));
            if (LoliLoadingPlugin.squashBakedQuads) {
                addTransformation("net.minecraft.client.renderer.block.model.BakedQuad", BakedQuadPatch::rewriteBakedQuad);
                addTransformation("net.minecraft.client.renderer.block.model.BakedQuadRetextured", BakedQuadRetexturedPatch::patchBakedQuadRetextured);
                addTransformation("net.minecraftforge.client.model.pipeline.UnpackedBakedQuad", UnpackedBakedQuadPatch::rewriteUnpackedBakedQuad);
                addTransformation("net.minecraftforge.client.model.pipeline.UnpackedBakedQuad$Builder", UnpackedBakedQuadPatch::rewriteUnpackedBakedQuad$Builder);
                addTransformation("zone.rong.loliasm.bakedquad.BakedQuadFactory", BakedQuadFactoryPatch::patchCreateMethod);
            }
            if (LoliConfig.instance.modelConditionCanonicalization) {
                addTransformation("net.minecraft.client.renderer.block.model.multipart.ICondition", this::canonicalBoolConditions);
                addTransformation("net.minecraft.client.renderer.block.model.multipart.ConditionOr", bytes -> canonicalPredicatedConditions(bytes, true));
                addTransformation("net.minecraft.client.renderer.block.model.multipart.ConditionAnd", bytes -> canonicalPredicatedConditions(bytes, false));
                addTransformation("net.minecraft.client.renderer.block.model.multipart.ConditionPropertyValue", this::canonicalPropertyValueConditions);
                // addTransformation("net.minecraft.client.renderer.block.model.MultipartBakedModel$Builder", this::cacheMultipartBakedModels); TODO
            }
            if (LoliConfig.instance.resourceLocationCanonicalization) {
                addTransformation("net.minecraft.client.renderer.block.model.ModelResourceLocation", this::canonicalizeResourceLocationStrings);
            }
            if (LoliConfig.instance.stripInstancedRandomFromSoundEventAccessor) {
                addTransformation("net.minecraft.client.audio.SoundEventAccessor", this::removeInstancedRandom);
            }
            if (LoliConfig.instance.optimizeRegistries) {
                addTransformation("net.minecraft.client.audio.SoundRegistry", this::removeDupeMapFromSoundRegistry);
                addTransformation("net.minecraftforge.client.model.ModelLoader", this::optimizeModelLoaderDataStructures);
                addTransformation("net.minecraft.client.renderer.block.statemap.StateMapperBase", this::optimizeStateMapperBaseBackingMap);
            }
            if (LoliConfig.instance.optimizeSomeRendering) {
                addTransformation("net.minecraft.client.renderer.RenderGlobal", bytes -> fixEnumFacingValuesClone(bytes, LoliLoadingPlugin.isDeobf ? "setupTerrain" : "func_174970_a"));
            }
            if (LoliConfig.instance.stripUnnecessaryLocalsInRenderHelper) {
                addTransformation("net.minecraft.client.renderer.RenderHelper", this::stripLocalsInEnableStandardItemLighting);
            }
        }
        if (LoliConfig.instance.resourceLocationCanonicalization) {
            addTransformation("net.minecraft.util.ResourceLocation", this::canonicalizeResourceLocationStrings);
        }
        if (LoliConfig.instance.optimizeRegistries) {
            addTransformation("net.minecraft.util.registry.RegistrySimple", this::removeValuesArrayFromRegistrySimple);
        }
        if (LoliConfig.instance.optimizeNBTTagCompoundBackingMap) {
            addTransformation("net.minecraft.nbt.NBTTagCompound", this::nbtTagCompound$replaceDefaultHashMap);
        }
        if (LoliConfig.instance.nbtTagStringBackingStringCanonicalization) {
            addTransformation("net.minecraft.nbt.NBTTagString", this::nbtTagStringRevamp);
        }
        if (LoliConfig.instance.packageStringCanonicalization) {
            addTransformation("net.minecraftforge.fml.common.discovery.ModCandidate", this::removePackageField);
        }
        if (LoliConfig.instance.stripNearUselessItemStackFields) {
            addTransformation("net.minecraft.item.ItemStack", this::stripItemStackFields);
        }
        if (LoliConfig.instance.optimizeFurnaceRecipeStore) {
            addTransformation("net.minecraft.item.crafting.FurnaceRecipes", this::improveFurnaceRecipes);
        }
        if (LoliConfig.instance.fixAmuletHolderCapability) {
            addTransformation("hellfirepvp.astralsorcery.common.enchantment.amulet.PlayerAmuletHandler", bytes -> stripSubscribeEventAnnotation(bytes, "attachAmuletItemCapability"));
        }
    }

    public void addTransformation(String key, Function<byte[], byte[]> value) {
        LoliLogger.instance.info("Adding class {} to the transformation queue", key);
        transformations.put(key, value);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        Function<byte[], byte[]> getBytes = transformations.get(transformedName);
        if (getBytes != null) {
            return getBytes.apply(bytes);
        }
        return bytes;
    }

    private byte[] canonicalizeResourceLocationStrings(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>") && method.desc.equals("(I[Ljava/lang/String;)V")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == GETSTATIC) {
                        LoliLogger.instance.info("Injecting calls in {} to canonicalize strings", node.name);
                        iter.remove(); // Remove GETSTATIC
                        iter.next(); // Move to INVOKEVIRTUAL, set replaces it
                        iter.set(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/api/StringPool", "lowerCaseAndCanonize", "(Ljava/lang/String;)Ljava/lang/String;", false));
                        break;
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] canonicalBoolConditions(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<clinit>")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction instanceof TypeInsnNode && instruction.getOpcode() == NEW) {
                        boolean bool = ((TypeInsnNode) instruction).desc.endsWith("$1");
                        LoliLogger.instance.info("Canonizing {} IConditions", bool ? "TRUE" : "FALSE");
                        iter.remove(); // Remove NEW
                        iter.next();
                        iter.remove(); // Remove DUP
                        iter.next();
                        iter.remove(); // Remove INVOKESPECIAL
                        iter.add(new FieldInsnNode(GETSTATIC, "zone/rong/loliasm/client/models/conditions/CanonicalConditions", bool ? "TRUE" : "FALSE", "Lnet/minecraft/client/renderer/block/model/multipart/ICondition;"));
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);

        return writer.toByteArray();
    }

    private byte[] canonicalPredicatedConditions(byte[] bytes, boolean or) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        final String getPredicate = LoliLoadingPlugin.isDeobf ? "getPredicate" : "func_188118_a";

        for (MethodNode method : node.methods) {
            if (method.name.equals(getPredicate)) {
                final String conditions = LoliLoadingPlugin.isDeobf ? "conditions" : or ? "field_188127_c" : "field_188121_c";
                LoliLogger.instance.info("Transforming {}::getPredicate to canonicalize different IConditions", node.name);
                method.instructions.clear();
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, node.name, conditions, "Ljava/lang/Iterable;"));
                method.instructions.add(new VarInsnNode(ALOAD, 1));
                method.instructions.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/client/models/conditions/CanonicalConditions", or ? "orCache" : "andCache", "(Ljava/lang/Iterable;Lnet/minecraft/block/state/BlockStateContainer;)Lcom/google/common/base/Predicate;", false));
                method.instructions.add(new InsnNode(ARETURN));
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);

        return writer.toByteArray();
    }

    private byte[] canonicalPropertyValueConditions(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        final String getPredicate = LoliLoadingPlugin.isDeobf ? "getPredicate" : "func_188118_a";

        for (MethodNode method : node.methods) {
            if (method.name.equals(getPredicate)) {
                LoliLogger.instance.info("Transforming {}::getPredicate to canonicalize different PropertyValueConditions", node.name);
                method.instructions.clear();
                method.instructions.add(new VarInsnNode(ALOAD, 1));
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, node.name, LoliLoadingPlugin.isDeobf ? "key" : "field_188125_d", "Ljava/lang/String;"));
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, node.name, LoliLoadingPlugin.isDeobf ? "value" : "field_188126_e", "Ljava/lang/String;"));
                method.instructions.add(new FieldInsnNode(GETSTATIC, node.name, LoliLoadingPlugin.isDeobf ? "SPLITTER" : "field_188124_c", "Lcom/google/common/base/Splitter;"));
                method.instructions.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/client/models/conditions/CanonicalConditions", "propertyValueCache", "(Lnet/minecraft/block/state/BlockStateContainer;Ljava/lang/String;Ljava/lang/String;Lcom/google/common/base/Splitter;)Lcom/google/common/base/Predicate;", false));
                method.instructions.add(new InsnNode(ARETURN));
                // method.localVariables.remove(0);
                method.localVariables.clear();
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);

        return writer.toByteArray();
    }

    private byte[] cacheMultipartBakedModels(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        final String makeMultipartModel = LoliLoadingPlugin.isDeobf ? "makeMultipartModel" : "func_188647_a";

        for (MethodNode method : node.methods) {
            if (method.name.equals(makeMultipartModel)) {
                LoliLogger.instance.info("Transforming {}::makeMultipartModel", node.name);
                final String builderSelectors = LoliLoadingPlugin.isDeobf ? "builderSelectors" : "field_188649_a";
                method.instructions.clear();
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, node.name, builderSelectors, "Ljava/util/Map;"));
                method.instructions.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/client/models/MultipartBakedModelCache", "makeMultipartModel", "(Ljava/util/Map;)Lnet/minecraft/client/renderer/block/model/MultipartBakedModel;", false));
                method.instructions.add(new InsnNode(ARETURN));
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] removeValuesArrayFromRegistrySimple(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        final String values = LoliLoadingPlugin.isDeobf ? "values" : "field_186802_b";

        node.fields.removeIf(f -> f.name.equals(values));

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] removeDupeMapFromSoundRegistry(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        final String soundRegistry = LoliLoadingPlugin.isDeobf ? "soundRegistry" : "field_148764_a";

        node.fields.removeIf(f -> f.name.equals(soundRegistry));

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] improveFurnaceRecipes(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                boolean isExperienceList = false;
                LoliLogger.instance.info("Improving FurnaceRecipes. Lookups are now a lot faster.");
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction instanceof MethodInsnNode) {
                        MethodInsnNode methodInstruction = (MethodInsnNode) instruction;
                        if (methodInstruction.owner.equals("com/google/common/collect/Maps")) {
                            iter.remove();
                            if (!isExperienceList) {
                                iter.add(new TypeInsnNode(NEW, "it/unimi/dsi/fastutil/objects/Object2ObjectOpenCustomHashMap"));
                                iter.add(new InsnNode(DUP));
                                iter.add(new FieldInsnNode(GETSTATIC, "zone/rong/loliasm/api/HashingStrategies", "FURNACE_INPUT_HASH", "Lit/unimi/dsi/fastutil/Hash$Strategy;"));
                                iter.add(new MethodInsnNode(INVOKESPECIAL, "it/unimi/dsi/fastutil/objects/Object2ObjectOpenCustomHashMap", "<init>", "(Lit/unimi/dsi/fastutil/Hash$Strategy;)V", false));
                                isExperienceList = true;
                            } else {
                                iter.add(new TypeInsnNode(NEW, "it/unimi/dsi/fastutil/objects/Object2FloatOpenHashMap"));
                                iter.add(new InsnNode(DUP));
                                iter.add(new MethodInsnNode(INVOKESPECIAL, "it/unimi/dsi/fastutil/objects/Object2FloatOpenHashMap", "<init>", "()V", false));
                                iter.next();
                                iter.add(new VarInsnNode(ALOAD, 0));
                                iter.add(new FieldInsnNode(GETFIELD, "net/minecraft/item/crafting/FurnaceRecipes", LoliLoadingPlugin.isDeobf ? "experienceList" : "field_77605_c", "Ljava/util/Map;"));
                                iter.add(new TypeInsnNode(CHECKCAST, "it/unimi/dsi/fastutil/objects/Object2FloatFunction"));
                                iter.add(new LdcInsnNode(0F));
                                iter.add(new MethodInsnNode(INVOKEINTERFACE, "it/unimi/dsi/fastutil/objects/Object2FloatFunction", "defaultReturnValue", "(F)V", true));
                            }
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] removeInstancedRandom(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        node.fields.removeIf(f -> f.desc.equals("Ljava/util/Random;"));

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction instanceof TypeInsnNode) {
                        TypeInsnNode newNode = (TypeInsnNode) instruction;
                        if (newNode.desc.equals("java/util/Random")) {
                            iter.previous();
                            iter.remove(); // Remove ALOAD
                            iter.next();
                            iter.remove(); // Remove NEW
                            iter.next();
                            iter.remove(); // Remove DUP
                            iter.next();
                            iter.remove(); // Remove INVOKESPECIAL
                            iter.next();
                            iter.remove(); // Remove PUTFIELD
                        }
                    }
                }
            } else if (method.name.equals(LoliLoadingPlugin.isDeobf ? "cloneEntry" : "func_148720_g")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == GETFIELD) {
                        if (((FieldInsnNode) instruction).desc.equals("Ljava/util/Random;")) {
                            iter.previous();
                            iter.remove(); // Remove ALOAD
                            iter.next();
                            iter.remove(); // Remove GETFIELD
                            iter.next();
                            iter.remove(); // Remove ILOAD
                            iter.add(new MethodInsnNode(INVOKESTATIC, "java/util/concurrent/ThreadLocalRandom", "current", "()Ljava/util/concurrent/ThreadLocalRandom;", false));
                            iter.add(new IntInsnNode(ILOAD, 1));
                            iter.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/concurrent/ThreadLocalRandom", "nextInt", "(I)I", false));
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] nbtTagCompound$replaceDefaultHashMap(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == INVOKESTATIC) {
                        iter.set(new TypeInsnNode(NEW, "it/unimi/dsi/fastutil/objects/Object2ObjectArrayMap"));
                        iter.add(new InsnNode(DUP));
                        iter.add(new MethodInsnNode(INVOKESPECIAL, "it/unimi/dsi/fastutil/objects/Object2ObjectArrayMap", "<init>", "()V", false));
                        break;
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] fixEnumFacingValuesClone(byte[] bytes, String methodMatch) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals(methodMatch)) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == INVOKESTATIC) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                        if (methodInsnNode.name.equals("values") && methodInsnNode.desc.equals("()[Lnet/minecraft/util/EnumFacing;")) {
                            LoliLogger.instance.info("Transforming EnumFacing::values() to EnumFacing::VALUES in {}", node.name);
                            iter.set(new FieldInsnNode(GETSTATIC, "net/minecraft/util/EnumFacing", LoliLoadingPlugin.isDeobf ? "VALUES" : "field_82609_l", "[Lnet/minecraft/util/EnumFacing;"));
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] removePackageField(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        node.fields.stream().filter(f -> f.name.equals("packages")).findFirst().ifPresent(f -> {
            f.desc = "Ljava/util/Set;";
            f.signature = "Ljava/util/Set<Ljava/lang/String;>;";
        });

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>") && method.desc.equals("(Ljava/io/File;Ljava/io/File;Lnet/minecraftforge/fml/common/discovery/ContainerType;ZZ)V")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == PUTFIELD) {
                        FieldInsnNode fieldNode = (FieldInsnNode) instruction;
                        if (fieldNode.name.equals("packages")) {
                            fieldNode.desc = "Ljava/util/Set;";
                            iter.previous();
                            iter.previous();
                            iter.set(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/core/LoliHooks", "createHashSet", "()Lit/unimi/dsi/fastutil/objects/ObjectOpenHashSet;", false));
                            break;
                        }
                    }
                }
            } else if (method.name.equals("addClassEntry")) { // see: LoliHooks::modCandidate$override$addClassEntry
                method.instructions.clear();
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new VarInsnNode(ALOAD, 1));
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/fml/common/discovery/ModCandidate", "foundClasses", "Ljava/util/Set;"));
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/fml/common/discovery/ModCandidate", "packages", "Ljava/util/Set;"));
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/fml/common/discovery/ModCandidate", "table", "Lnet/minecraftforge/fml/common/discovery/ASMDataTable;"));
                method.instructions.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/core/LoliHooks", "modCandidate$override$addClassEntry", "(Lnet/minecraftforge/fml/common/discovery/ModCandidate;Ljava/lang/String;Ljava/util/Set;Ljava/util/Set;Lnet/minecraftforge/fml/common/discovery/ASMDataTable;)V", false));
                method.instructions.add(new InsnNode(RETURN));
            } else if (method.name.equals("getContainedPackages")) { // Return ArrayList with Set elements
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == GETFIELD) {
                        FieldInsnNode fieldNode = (FieldInsnNode) instruction;
                        fieldNode.desc = "Ljava/util/Set;";
                        iter.add(new MethodInsnNode(INVOKESTATIC, "com/google/common/collect/Lists", "newArrayList", "(Ljava/lang/Iterable;)Ljava/util/ArrayList;", false));
                        break;
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] nbtTagStringRevamp(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        // node.fields.removeIf(f -> f.name.equals(LoliLoadingPlugin.isDeobf ? "data" : "field_74751_a"));

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>") && method.desc.equals("(Ljava/lang/String;)V")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == PUTFIELD) {
                        method.instructions.insertBefore(instruction, new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/core/LoliHooks", "nbtTagString$override$ctor", "(Ljava/lang/String;)Ljava/lang/String;", false));
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] stripSubscribeEventAnnotation(byte[] bytes, String... methodNames) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            for (String methodName : methodNames) {
                if (method.name.equals(methodName)) {
                    List<AnnotationNode> annotations = method.visibleAnnotations;
                    if (annotations != null) {
                        annotations.removeIf(a -> a.desc.equals("Lnet/minecraftforge/fml/common/eventhandler/SubscribeEvent;"));
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] stripItemStackFields(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        String[] fields = new String[5];
        fields[0] = !LoliLoadingPlugin.isDeobf ? "field_82843_f" : "itemFrame";
        fields[1] = !LoliLoadingPlugin.isDeobf ? "field_179552_h" : "canDestroyCacheBlock";
        fields[2] = !LoliLoadingPlugin.isDeobf ? "field_179553_i" : "canDestroyCacheResult";
        fields[3] = !LoliLoadingPlugin.isDeobf ? "field_179550_j" : "canPlaceOnCacheBlock";
        fields[4] = !LoliLoadingPlugin.isDeobf ? "field_179551_k" : "canPlaceOnCacheResult";

        node.fields.removeIf(f -> ArrayUtils.contains(fields, f.name));

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] stripLocalsInEnableStandardItemLighting(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals(LoliLoadingPlugin.isDeobf ? "enableStandardItemLighting" : "func_74519_b")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == FSTORE) {
                        iter.remove(); // FSTORE
                        iter.previous();
                        iter.remove(); // LDC
                        method.localVariables = null;
                        break;
                    }
                }

            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] optimizeModelLoaderDataStructures(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == INVOKESTATIC) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                        if (methodInsnNode.desc.equals("()Ljava/util/HashMap;")) {
                            iter.set(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/core/LoliHooks", "createHashMap", "()Lit/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap;", false));
                        } else if (methodInsnNode.desc.equals("()Ljava/util/HashSet;")) {
                            iter.set(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/core/LoliHooks", "createHashSet", "()Lit/unimi/dsi/fastutil/objects/ObjectOpenHashSet;", false));
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] optimizeStateMapperBaseBackingMap(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == INVOKESTATIC && ((MethodInsnNode) instruction).name.equals("newLinkedHashMap")) {
                        iter.set(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/core/LoliHooks", "createArrayMap", "()Lit/unimi/dsi/fastutil/objects/Object2ObjectArrayMap;", false));
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
