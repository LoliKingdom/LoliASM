package zone.rong.loliasm.core;

import betterwithmods.module.gameplay.Gameplay;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import zone.rong.loliasm.LoliReflector;
import zone.rong.loliasm.api.LoliStringPool;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.patches.*;

import java.util.*;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.*;

public class LoliTransformer implements IClassTransformer {

    public static boolean isOptifineInstalled;
    public static boolean squashBakedQuads = LoliConfig.instance.squashBakedQuads;

    Multimap<String, Function<byte[], byte[]>> transformations;

    public LoliTransformer() {
        LoliLogger.instance.info("The lolis are now preparing to bytecode manipulate your game.");
        isOptifineInstalled = LoliReflector.doesClassExist("optifine.OptiFineForgeTweaker");
        if (squashBakedQuads && isOptifineInstalled) {
            squashBakedQuads = false;
            LoliLogger.instance.info("Optifine is installed. BakedQuads won't be squashed as it is incompatible with OptiFine.");
        }
        transformations = MultimapBuilder.hashKeys(30).arrayListValues(1).build();
        if (LoliLoadingPlugin.isClient) {
            // addTransformation("codechicken.lib.model.loader.blockstate.CCBlockStateLoader", bytes -> stripSubscribeEventAnnotation(bytes, "onModelBake", "onTextureStitchPre"));
            if (squashBakedQuads) {
                addTransformation("net.minecraft.client.renderer.block.model.BakedQuad", BakedQuadPatch::rewriteBakedQuad);
                addTransformation("net.minecraft.client.renderer.block.model.BakedQuadRetextured", BakedQuadRetexturedPatch::patchBakedQuadRetextured);
                addTransformation("net.minecraftforge.client.model.pipeline.UnpackedBakedQuad", UnpackedBakedQuadPatch::rewriteUnpackedBakedQuad);
                addTransformation("net.minecraftforge.client.model.pipeline.UnpackedBakedQuad$Builder", UnpackedBakedQuadPatch::rewriteUnpackedBakedQuad$Builder);
                addTransformation("zone.rong.loliasm.bakedquad.BakedQuadFactory", BakedQuadFactoryPatch::patchCreateMethod);
                for (String classThatExtendBakedQuad : LoliConfig.instance.classesThatExtendBakedQuad) {
                    if (!classThatExtendBakedQuad.trim().isEmpty()) {
                        addTransformation(classThatExtendBakedQuad, this::extendSupportingBakedQuadInstead);
                    }
                }
            } else if (LoliConfig.instance.vertexDataCanonicalization) {
                addTransformation("net.minecraft.client.renderer.block.model.BakedQuad", this::canonicalizeVertexData);
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
                addTransformation("net.minecraftforge.client.model.ModelLoader", this::optimizeDataStructures);
                addTransformation("net.minecraft.client.renderer.block.statemap.StateMapperBase", this::optimizeDataStructures);
                addTransformation("net.minecraft.client.renderer.BlockModelShapes", this::optimizeDataStructures);
                addTransformation("net.minecraft.client.renderer.block.statemap.BlockStateMapper", this::optimizeDataStructures);
            }
            if (LoliConfig.instance.optimizeSomeRendering) {
                addTransformation("net.minecraft.client.renderer.RenderGlobal", bytes -> fixEnumFacingValuesClone(bytes, LoliLoadingPlugin.isDeobf ? "setupTerrain" : "func_174970_a"));
            }
            if (LoliConfig.instance.stripUnnecessaryLocalsInRenderHelper) {
                addTransformation("net.minecraft.client.renderer.RenderHelper", this::stripLocalsInEnableStandardItemLighting);
            }
            if (LoliConfig.instance.spriteNameCanonicalization) {
                addTransformation("net.minecraft.client.renderer.texture.TextureAtlasSprite", this::canonicalizeSpriteNames);
            }
        }
        if (LoliConfig.instance.resourceLocationCanonicalization) {
            addTransformation("net.minecraft.util.ResourceLocation", this::canonicalizeResourceLocationStrings);
        }
        if (LoliConfig.instance.optimizeRegistries) {
            addTransformation("net.minecraft.util.registry.RegistrySimple", this::removeValuesArrayFromRegistrySimple);
        }
        if (LoliConfig.instance.nbtTagStringBackingStringCanonicalization) {
            addTransformation("net.minecraft.nbt.NBTTagString", this::nbtTagStringRevamp);
        }
        if (LoliConfig.instance.packageStringCanonicalization) {
            addTransformation("net.minecraftforge.fml.common.discovery.ModCandidate", this::removePackageField);
        }
        if (LoliConfig.instance.asmDataStringCanonicalization) {
            addTransformation("net.minecraftforge.fml.common.discovery.ASMDataTable$ASMData", this::deduplicateASMDataStrings);
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
        if (LoliConfig.instance.labelCanonicalization) {
            addTransformation("mezz.jei.suffixtree.Edge", this::deduplicateEdgeLabels);
        }
        if (LoliConfig.instance.bwmBlastingOilOptimization) {
            addTransformation("betterwithmods.event.BlastingOilEvent", bytes -> stripSubscribeEventAnnotation(bytes, "onPlayerTakeDamage", "onHitGround"));
            addTransformation("betterwithmods.common.items.ItemMaterial", this::injectBlastingOilEntityItemUpdate);
        }
        if (LoliConfig.instance.optimizeQMDBeamRenderer) {
            addTransformation("lach_01298.qmd.render.entity.BeamRenderer", bytes -> stripSubscribeEventAnnotation(bytes, "renderBeamEffects"));
        }
        if (LoliConfig.instance.fixTFCFallingBlockFalseStartingTEPos) {
            addTransformation("net.dries007.tfc.objects.entity.EntityFallingBlockTFC", this::fixTFCFallingBlock);
        }
        addTransformation("net.minecraft.nbt.NBTTagCompound", bytes -> nbtTagCompound$replaceDefaultHashMap(bytes, LoliConfig.instance.optimizeNBTTagCompoundBackingMap, LoliConfig.instance.nbtBackingMapStringCanonicalization));
    }

    public void addTransformation(String key, Function<byte[], byte[]> value) {
        LoliLogger.instance.info("Adding class {} to the transformation queue", key);
        transformations.put(key, value);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        Collection<Function<byte[], byte[]>> getBytes = transformations.get(transformedName);
        if (getBytes != null) {
            byte[] transformedByteArray = bytes;
            for (Function<byte[], byte[]> func : getBytes) {
                transformedByteArray = func.apply(transformedByteArray);
            }
            return transformedByteArray;
        }
        // transformations.removeAll(transformedName);
        return bytes;
    }

    private byte[] extendSupportingBakedQuadInstead(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        if (node.superName.equals("net/minecraft/client/renderer/block/model/BakedQuad")) {
            node.superName = "zone/rong/loliasm/bakedquad/SupportingBakedQuad";
        }

        Set<String> fieldsToLookOutFor = new ObjectOpenHashSet<>(new String[] { "face", "applyDiffuseLighting", "tintIndex" });

        for (MethodNode method : node.methods) {
            ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode instruction = iter.next();
                if (method.name.equals("<init>") && instruction instanceof MethodInsnNode) {
                    MethodInsnNode methodNode = (MethodInsnNode) instruction;
                    if (methodNode.getOpcode() == INVOKESPECIAL && methodNode.owner.equals("net/minecraft/client/renderer/block/model/BakedQuad")) {
                        methodNode.owner = "zone/rong/loliasm/bakedquad/SupportingBakedQuad";
                    }
                } else if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode fieldNode = (FieldInsnNode) instruction;
                    if (fieldNode.owner.equals("net/minecraft/client/renderer/block/model/BakedQuad")) {
                        if (fieldsToLookOutFor.contains(fieldNode.name)) {
                            fieldNode.owner = "zone/rong/loliasm/bakedquad/SupportingBakedQuad";
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
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
                    if (instruction instanceof MethodInsnNode && instruction.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) instruction).name.equals("toLowerCase")) {
                        LoliLogger.instance.info("Injecting calls in {}{} to canonicalize strings", node.name, method.name);
                        iter.previous();
                        iter.previous(); // Move to GETSTATIC
                        iter.remove(); // Remove GETSTATIC
                        iter.next(); // Replace INVOKEVIRTUAL with INVOKESTATIC
                        iter.set(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/api/LoliStringPool", "lowerCaseAndCanonicalize", "(Ljava/lang/String;)Ljava/lang/String;", false));
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
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

        outer: for (MethodNode method : node.methods) {
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
                                if (LoliConfig.instance.furnaceExperienceVanilla) {
                                    break outer;
                                }
                                isExperienceList = true;
                            } else {
                                iter.add(new TypeInsnNode(NEW, "it/unimi/dsi/fastutil/objects/Object2FloatOpenCustomHashMap"));
                                iter.add(new InsnNode(DUP));
                                iter.add(new FieldInsnNode(GETSTATIC, "zone/rong/loliasm/api/HashingStrategies", "FURNACE_INPUT_HASH", "Lit/unimi/dsi/fastutil/Hash$Strategy;"));
                                iter.add(new MethodInsnNode(INVOKESPECIAL, "it/unimi/dsi/fastutil/objects/Object2FloatOpenCustomHashMap", "<init>", "(Lit/unimi/dsi/fastutil/Hash$Strategy;)V", false));
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

    private byte[] nbtTagCompound$replaceDefaultHashMap(byte[] bytes, boolean optimizeMap, boolean canonicalizeString) {
        if (!optimizeMap && !canonicalizeString) {
            return bytes;
        }
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == INVOKESTATIC) {
                        iter.set(new TypeInsnNode(NEW, canonicalizeString ? "zone/rong/loliasm/api/datastructures/canonical/AutoCanonizingArrayMap" : "it/unimi/dsi/fastutil/objects/Object2ObjectArrayMap"));
                        iter.add(new InsnNode(DUP));
                        iter.add(new MethodInsnNode(INVOKESPECIAL, canonicalizeString ? "zone/rong/loliasm/api/datastructures/canonical/AutoCanonizingArrayMap" : "it/unimi/dsi/fastutil/objects/Object2ObjectArrayMap", "<init>", "()V", false));
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

        // Canonicalize default ClassLoader packages strings first so any more of the same package strings uses those instances instead.

        try {
            Map<String, Package> packages = (Map<String, Package>)  LoliReflector.getField(ClassLoader.class, "packages").get(Launch.classLoader);
            Set<String> packageStrings = packages.keySet();
            packageStrings.forEach(LoliStringPool::canonicalize);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

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

    private byte[] deduplicateEdgeLabels(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>") || method.name.equals("setLabel")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == PUTFIELD && ((FieldInsnNode) instruction).name.equals("label")) {
                        iter.previous();
                        iter.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/api/LoliStringPool", "canonicalize", "(Ljava/lang/String;)Ljava/lang/String;", false));
                        // iter.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "intern", "()Ljava/lang/String;", false));
                        iter.next();
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] deduplicateASMDataStrings(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == PUTFIELD) {
                        FieldInsnNode fieldNode = (FieldInsnNode) instruction;
                        if (fieldNode.name.equals("annotationName") || fieldNode.name.equals("className")) {
                            iter.previous();
                            iter.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/core/LoliHooks", "asmData$redirect$CtorStringsToIntern", "(Ljava/lang/String;)Ljava/lang/String;", false));
                            iter.next();
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] canonicalizeSpriteNames(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == PUTFIELD) {
                        FieldInsnNode fieldNode = (FieldInsnNode) instruction;
                        if (fieldNode.desc.equals("Ljava/lang/String")) {
                            iter.previous();
                            iter.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/api/LoliStringPool", "canonicalize", "(Ljava/lang/String;)Ljava/lang/String;", false));
                            // iter.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "intern", "()Ljava/lang/String;", false));
                            iter.next();
                            break;
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] injectBlastingOilEntityItemUpdate(byte[] bytes) {
        if (!Gameplay.disableBlastingOilEvents) {
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            node.accept(writer);
            MethodVisitor methodVisitor = writer.visitMethod(ACC_PUBLIC, "onEntityItemUpdate", "(Lnet/minecraft/entity/item/EntityItem;)Z", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "zone/rong/loliasm/common/modfixes/betterwithmods/BWMBlastingOilOptimization", "inject$ItemMaterial$onEntityItemUpdate", "(Lnet/minecraft/entity/item/EntityItem;)Z", false);
            methodVisitor.visitInsn(IRETURN);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
            writer.visitEnd();

            return writer.toByteArray();
        }
        return bytes;
    }

    private byte[] canonicalizeVertexData(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>") && method.desc.equals("([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZLnet/minecraft/client/renderer/vertex/VertexFormat;)V")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == PUTFIELD) {
                        FieldInsnNode fieldNode = (FieldInsnNode) instruction;
                        if (fieldNode.desc.equals("[I")) {
                            iter.previous();
                            iter.add(new VarInsnNode(ALOAD, 0));
                            iter.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/bakedquad/LoliVertexDataPool", "canonicalize", "([ILnet/minecraft/client/renderer/block/model/BakedQuad;)[I", false));
                            method.maxStack = 3;
                            break;
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] optimizeDataStructures(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == INVOKESTATIC) {
                        MethodInsnNode methodNode = (MethodInsnNode) instruction;
                        switch (methodNode.name) {
                            case "newIdentityHashMap":
                                methodNode.owner = "zone/rong/loliasm/core/LoliHooks";
                                methodNode.name = "createReferenceMap";
                                methodNode.desc = "()Lit/unimi/dsi/fastutil/objects/Reference2ObjectOpenHashMap;";
                                break;
                            case "newLinkedHashMap":
                                methodNode.owner = "zone/rong/loliasm/core/LoliHooks";
                                methodNode.name = "createLinkedMap";
                                methodNode.desc = "()Lit/unimi/dsi/fastutil/objects/Object2ObjectLinkedOpenHashMap;";
                                break;
                            case "newHashMap":
                                methodNode.owner = "zone/rong/loliasm/core/LoliHooks";
                                methodNode.name = "createHashMap";
                                methodNode.desc = "()Lit/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap;";
                                break;
                            case "newHashSet":
                                methodNode.owner = "zone/rong/loliasm/core/LoliHooks";
                                methodNode.name = "createHashSet";
                                methodNode.desc = "()Lit/unimi/dsi/fastutil/objects/ObjectOpenHashSet;";
                                break;
                            case "newIdentityHashSet":
                                methodNode.owner = "zone/rong/loliasm/core/LoliHooks";
                                methodNode.name = "createReferenceSet";
                                methodNode.desc = "()Lit/unimi/dsi/fastutil/objects/ReferenceOpenHashSet;";
                                break;
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] fixTFCFallingBlock(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        all: for (MethodNode method : node.methods) {
            if (method.name.equals("<init>") && method.desc.equals("(Lnet/minecraft/world/World;DDDLnet/minecraft/block/state/IBlockState;)V")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == NEW && ((TypeInsnNode) instruction).desc.equals("net/minecraft/util/math/BlockPos")) {
                        iter.next(); // DUP
                        iter.next();
                        iter.set(new VarInsnNode(DLOAD, 2));
                        iter.add(new VarInsnNode(DLOAD, 4));
                        iter.add(new VarInsnNode(DLOAD, 6));
                        MethodInsnNode currentInstruction = (MethodInsnNode) iter.next();
                        currentInstruction.desc = "(DDD)V";
                        break all;
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }

}
