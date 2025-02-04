package zone.rong.garyasm.core;

import betterwithmods.module.gameplay.Gameplay;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import zone.rong.garyasm.GaryReflector;
import zone.rong.garyasm.api.GaryStringPool;
import zone.rong.garyasm.config.GaryConfig;
import zone.rong.garyasm.GaryLogger;
import zone.rong.garyasm.patches.*;

import java.util.*;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.*;

public class GaryTransformer implements IClassTransformer {

    public static boolean isOptifineInstalled, isSodiumPortInstalled;
    public static boolean squashBakedQuads = GaryConfig.instance.squashBakedQuads;

    Multimap<String, Function<byte[], byte[]>> transformations;

    public GaryTransformer() {
        GaryLogger.instance.info("The garys are now preparing to bytecode manipulate your game.");
        isOptifineInstalled = GaryReflector.doesClassExist("optifine.OptiFineForgeTweaker");
        isSodiumPortInstalled = GaryReflector.doesClassExist("me.jellysquid.mods.sodium.client.SodiumMixinTweaker");
        if (squashBakedQuads) {
            if (isOptifineInstalled) {
                squashBakedQuads = false;
                GaryLogger.instance.info("Optifine is installed. BakedQuads won't be squashed as it is incompatible with OptiFine.");
            } else if (isSodiumPortInstalled) {
                squashBakedQuads = false;
                GaryLogger.instance.info("A sodium port is installed. BakedQuads won't be squashed as it is incompatible with Sodium.");
            }
        }
        transformations = MultimapBuilder.hashKeys(30).arrayListValues(1).build();
        if (GaryLoadingPlugin.isClient) {
            // addTransformation("codechicken.lib.model.loader.blockstate.CCBlockStateLoader", bytes -> stripSubscribeEventAnnotation(bytes, "onModelBake", "onTextureStitchPre"));
            if (squashBakedQuads) {
                addTransformation("net.minecraft.client.renderer.block.model.BakedQuad", BakedQuadPatch::rewriteBakedQuad);
                addTransformation("net.minecraft.client.renderer.block.model.BakedQuadRetextured", BakedQuadRetexturedPatch::patchBakedQuadRetextured);
                addTransformation("net.minecraftforge.client.model.pipeline.UnpackedBakedQuad", UnpackedBakedQuadPatch::rewriteUnpackedBakedQuad);
                addTransformation("net.minecraftforge.client.model.pipeline.UnpackedBakedQuad$Builder", UnpackedBakedQuadPatch::rewriteUnpackedBakedQuad$Builder);
                addTransformation("zone.rong.garyasm.bakedquad.BakedQuadFactory", BakedQuadFactoryPatch::patchCreateMethod);
                for (String classThatExtendBakedQuad : GaryConfig.instance.classesThatExtendBakedQuad) {
                    if (!classThatExtendBakedQuad.trim().isEmpty()) {
                        addTransformation(classThatExtendBakedQuad, this::extendSupportingBakedQuadInstead);
                    }
                }
            } else if (GaryConfig.instance.vertexDataCanonicalization) {
                addTransformation("net.minecraft.client.renderer.block.model.BakedQuad", this::canonicalizeVertexData);
            }
            if (GaryConfig.instance.modelConditionCanonicalization) {
                addTransformation("net.minecraft.client.renderer.block.model.multipart.ICondition", this::canonicalBoolConditions);
                addTransformation("net.minecraft.client.renderer.block.model.multipart.ConditionOr", bytes -> canonicalPredicatedConditions(bytes, true));
                addTransformation("net.minecraft.client.renderer.block.model.multipart.ConditionAnd", bytes -> canonicalPredicatedConditions(bytes, false));
                addTransformation("net.minecraft.client.renderer.block.model.multipart.ConditionPropertyValue", this::canonicalPropertyValueConditions);
                // addTransformation("net.minecraft.client.renderer.block.model.MultipartBakedModel$Builder", this::cacheMultipartBakedModels); TODO
            }
            if (GaryConfig.instance.resourceLocationCanonicalization) {
                addTransformation("net.minecraft.client.renderer.block.model.ModelResourceLocation", this::canonicalizeResourceLocationStrings);
            }
            if (GaryConfig.instance.stripInstancedRandomFromSoundEventAccessor) {
                addTransformation("net.minecraft.client.audio.SoundEventAccessor", this::removeInstancedRandom);
            }
            if (GaryConfig.instance.optimizeRegistries) {
                addTransformation("net.minecraft.client.audio.SoundRegistry", this::removeDupeMapFromSoundRegistry);
                addTransformation("net.minecraftforge.client.model.ModelLoader", this::optimizeDataStructures);
                addTransformation("net.minecraft.client.renderer.block.statemap.StateMapperBase", this::optimizeDataStructures);
                addTransformation("net.minecraft.client.renderer.BlockModelShapes", this::optimizeDataStructures);
                addTransformation("net.minecraft.client.renderer.block.statemap.BlockStateMapper", this::optimizeDataStructures);
            }
            if (GaryConfig.instance.optimizeSomeRendering) {
                addTransformation("net.minecraft.client.renderer.RenderGlobal", bytes -> fixEnumFacingValuesClone(bytes, GaryLoadingPlugin.isDeobf ? "setupTerrain" : "func_174970_a"));
            }
            if (GaryConfig.instance.stripUnnecessaryLocalsInRenderHelper) {
                addTransformation("net.minecraft.client.renderer.RenderHelper", this::stripLocalsInEnableStandardItemLighting);
            }
            if (GaryConfig.instance.spriteNameCanonicalization) {
                addTransformation("net.minecraft.client.renderer.texture.TextureAtlasSprite", this::canonicalizeSpriteNames);
            }
            if (GaryConfig.instance.removeExcessiveGCCalls) {
                addTransformation("net.minecraft.client.Minecraft", this::removeExcessiveGCCalls);
            }
            if (GaryConfig.instance.smoothDimensionChange) {
                addTransformation("net.minecraft.client.network.NetHandlerPlayClient", this::smoothDimensionChange);
            }
            if (GaryConfig.instance.fixMC88176) {
                addTransformation("net.minecraft.client.renderer.RenderGlobal", this::disappearingEntitiesRenderGlobalFix);
                addTransformation("net.minecraft.client.renderer.chunk.RenderChunk", this::disappearingEntitiesRenderChunkFix);
            }
        }
        if (GaryConfig.instance.resourceLocationCanonicalization) {
            addTransformation("net.minecraft.util.ResourceLocation", this::canonicalizeResourceLocationStrings);
        }
        if (GaryConfig.instance.optimizeRegistries) {
            addTransformation("net.minecraft.util.registry.RegistrySimple", this::removeValuesArrayFromRegistrySimple);
        }
        if (GaryConfig.instance.nbtTagStringBackingStringCanonicalization) {
            addTransformation("net.minecraft.nbt.NBTTagString", this::nbtTagStringRevamp);
        }
        if (GaryConfig.instance.packageStringCanonicalization) {
            addTransformation("net.minecraftforge.fml.common.discovery.ModCandidate", this::removePackageField);
        }
        if (GaryConfig.instance.asmDataStringCanonicalization) {
            addTransformation("net.minecraftforge.fml.common.discovery.ASMDataTable$ASMData", this::deduplicateASMDataStrings);
        }
        if (GaryConfig.instance.stripNearUselessItemStackFields) {
            addTransformation("net.minecraft.item.ItemStack", this::stripItemStackFields);
        }
        if (GaryConfig.instance.optimizeFurnaceRecipeStore) {
            addTransformation("net.minecraft.item.crafting.FurnaceRecipes", this::improveFurnaceRecipes);
        }
        if (GaryConfig.instance.fixAmuletHolderCapability) {
            addTransformation("hellfirepvp.astralsorcery.common.enchantment.amulet.PlayerAmuletHandler", bytes -> stripSubscribeEventAnnotation(bytes, "attachAmuletItemCapability"));
        }
        if (GaryConfig.instance.labelCanonicalization) {
            addTransformation("mezz.jei.suffixtree.Edge", this::deduplicateEdgeLabels);
        }
        if (GaryConfig.instance.bwmBlastingOilOptimization) {
            addTransformation("betterwithmods.event.BlastingOilEvent", bytes -> stripSubscribeEventAnnotation(bytes, "onPlayerTakeDamage", "onHitGround"));
            addTransformation("betterwithmods.common.items.ItemMaterial", this::injectBlastingOilEntityItemUpdate);
        }
        if (GaryConfig.instance.optimizeQMDBeamRenderer) {
            addTransformation("lach_01298.qmd.render.entity.BeamRenderer", bytes -> stripSubscribeEventAnnotation(bytes, "renderBeamEffects"));
        }
        if (GaryConfig.instance.fixTFCFallingBlockFalseStartingTEPos) {
            addTransformation("net.dries007.tfc.objects.entity.EntityFallingBlockTFC", this::fixTFCFallingBlock);
        }
        if (GaryConfig.instance.delayItemStackCapabilityInit) {
            addTransformation("net.minecraft.item.ItemStack", this::delayItemStackCapabilityInit);
        }
        if (GaryConfig.instance.fixMC30845) {
            addTransformation("net.minecraft.client.renderer.EntityRenderer", this::fixMC30845);
        }
        if (GaryConfig.instance.fixMC31681) {
            addTransformation("net.minecraft.client.renderer.EntityRenderer", this::fixMC31681);
        }
        addTransformation("net.minecraft.nbt.NBTTagCompound", bytes -> nbtTagCompound$replaceDefaultHashMap(bytes, GaryConfig.instance.optimizeNBTTagCompoundBackingMap, GaryConfig.instance.optimizeNBTTagCompoundMapThreshold, GaryConfig.instance.nbtBackingMapStringCanonicalization));
    }

    public void addTransformation(String key, Function<byte[], byte[]> value) {
        GaryLogger.instance.info("Adding class {} to the transformation queue", key);
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
            node.superName = "zone/rong/garyasm/bakedquad/SupportingBakedQuad";
        }

        Set<String> fieldsToLookOutFor = new ObjectOpenHashSet<>(new String[] { "face", "applyDiffuseLighting", "tintIndex" });

        for (MethodNode method : node.methods) {
            ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode instruction = iter.next();
                if (method.name.equals("<init>") && instruction instanceof MethodInsnNode) {
                    MethodInsnNode methodNode = (MethodInsnNode) instruction;
                    if (methodNode.getOpcode() == INVOKESPECIAL && methodNode.owner.equals("net/minecraft/client/renderer/block/model/BakedQuad")) {
                        methodNode.owner = "zone/rong/garyasm/bakedquad/SupportingBakedQuad";
                    }
                } else if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode fieldNode = (FieldInsnNode) instruction;
                    if (fieldNode.owner.equals("net/minecraft/client/renderer/block/model/BakedQuad")) {
                        if (fieldsToLookOutFor.contains(fieldNode.name)) {
                            fieldNode.owner = "zone/rong/garyasm/bakedquad/SupportingBakedQuad";
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
                        GaryLogger.instance.info("Injecting calls in {}{} to canonicalize strings", node.name, method.name);
                        iter.previous();
                        iter.previous(); // Move to GETSTATIC
                        iter.remove(); // Remove GETSTATIC
                        iter.next(); // Replace INVOKEVIRTUAL with INVOKESTATIC
                        iter.set(new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/api/GaryStringPool", "lowerCaseAndCanonicalize", "(Ljava/lang/String;)Ljava/lang/String;", false));
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
                        GaryLogger.instance.info("Canonizing {} IConditions", bool ? "TRUE" : "FALSE");
                        iter.remove(); // Remove NEW
                        iter.next();
                        iter.remove(); // Remove DUP
                        iter.next();
                        iter.remove(); // Remove INVOKESPECIAL
                        iter.add(new FieldInsnNode(GETSTATIC, "zone/rong/garyasm/client/models/conditions/CanonicalConditions", bool ? "TRUE" : "FALSE", "Lnet/minecraft/client/renderer/block/model/multipart/ICondition;"));
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

        final String getPredicate = GaryLoadingPlugin.isDeobf ? "getPredicate" : "func_188118_a";

        for (MethodNode method : node.methods) {
            if (method.name.equals(getPredicate)) {
                final String conditions = GaryLoadingPlugin.isDeobf ? "conditions" : or ? "field_188127_c" : "field_188121_c";
                GaryLogger.instance.info("Transforming {}::getPredicate to canonicalize different IConditions", node.name);
                method.instructions.clear();
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, node.name, conditions, "Ljava/lang/Iterable;"));
                method.instructions.add(new VarInsnNode(ALOAD, 1));
                method.instructions.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/client/models/conditions/CanonicalConditions", or ? "orCache" : "andCache", "(Ljava/lang/Iterable;Lnet/minecraft/block/state/BlockStateContainer;)Lcom/google/common/base/Predicate;", false));
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

        final String getPredicate = GaryLoadingPlugin.isDeobf ? "getPredicate" : "func_188118_a";

        for (MethodNode method : node.methods) {
            if (method.name.equals(getPredicate)) {
                GaryLogger.instance.info("Transforming {}::getPredicate to canonicalize different PropertyValueConditions", node.name);
                method.instructions.clear();
                method.instructions.add(new VarInsnNode(ALOAD, 1));
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, node.name, GaryLoadingPlugin.isDeobf ? "key" : "field_188125_d", "Ljava/lang/String;"));
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, node.name, GaryLoadingPlugin.isDeobf ? "value" : "field_188126_e", "Ljava/lang/String;"));
                method.instructions.add(new FieldInsnNode(GETSTATIC, node.name, GaryLoadingPlugin.isDeobf ? "SPLITTER" : "field_188124_c", "Lcom/google/common/base/Splitter;"));
                method.instructions.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/client/models/conditions/CanonicalConditions", "propertyValueCache", "(Lnet/minecraft/block/state/BlockStateContainer;Ljava/lang/String;Ljava/lang/String;Lcom/google/common/base/Splitter;)Lcom/google/common/base/Predicate;", false));
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

        final String makeMultipartModel = GaryLoadingPlugin.isDeobf ? "makeMultipartModel" : "func_188647_a";

        for (MethodNode method : node.methods) {
            if (method.name.equals(makeMultipartModel)) {
                GaryLogger.instance.info("Transforming {}::makeMultipartModel", node.name);
                final String builderSelectors = GaryLoadingPlugin.isDeobf ? "builderSelectors" : "field_188649_a";
                method.instructions.clear();
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, node.name, builderSelectors, "Ljava/util/Map;"));
                method.instructions.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/client/models/MultipartBakedModelCache", "makeMultipartModel", "(Ljava/util/Map;)Lnet/minecraft/client/renderer/block/model/MultipartBakedModel;", false));
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

        final String values = GaryLoadingPlugin.isDeobf ? "values" : "field_186802_b";

        node.fields.removeIf(f -> f.name.equals(values));

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] removeDupeMapFromSoundRegistry(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        final String soundRegistry = GaryLoadingPlugin.isDeobf ? "soundRegistry" : "field_148764_a";

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
                GaryLogger.instance.info("Improving FurnaceRecipes. Lookups are now a lot faster.");
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction instanceof MethodInsnNode) {
                        MethodInsnNode methodInstruction = (MethodInsnNode) instruction;
                        if (methodInstruction.owner.equals("com/google/common/collect/Maps")) {
                            iter.remove();
                            if (!isExperienceList) {
                                iter.add(new TypeInsnNode(NEW, "it/unimi/dsi/fastutil/objects/Object2ObjectOpenCustomHashMap"));
                                iter.add(new InsnNode(DUP));
                                iter.add(new FieldInsnNode(GETSTATIC, "zone/rong/garyasm/api/HashingStrategies", "FURNACE_INPUT_HASH", "Lit/unimi/dsi/fastutil/Hash$Strategy;"));
                                iter.add(new MethodInsnNode(INVOKESPECIAL, "it/unimi/dsi/fastutil/objects/Object2ObjectOpenCustomHashMap", "<init>", "(Lit/unimi/dsi/fastutil/Hash$Strategy;)V", false));
                                if (GaryConfig.instance.furnaceExperienceVanilla) {
                                    break outer;
                                }
                                isExperienceList = true;
                            } else {
                                iter.add(new TypeInsnNode(NEW, "it/unimi/dsi/fastutil/objects/Object2FloatOpenCustomHashMap"));
                                iter.add(new InsnNode(DUP));
                                iter.add(new FieldInsnNode(GETSTATIC, "zone/rong/garyasm/api/HashingStrategies", "FURNACE_INPUT_HASH", "Lit/unimi/dsi/fastutil/Hash$Strategy;"));
                                iter.add(new MethodInsnNode(INVOKESPECIAL, "it/unimi/dsi/fastutil/objects/Object2FloatOpenCustomHashMap", "<init>", "(Lit/unimi/dsi/fastutil/Hash$Strategy;)V", false));
                                iter.next();
                                iter.add(new VarInsnNode(ALOAD, 0));
                                iter.add(new FieldInsnNode(GETFIELD, "net/minecraft/item/crafting/FurnaceRecipes", GaryLoadingPlugin.isDeobf ? "experienceList" : "field_77605_c", "Ljava/util/Map;"));
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
            } else if (method.name.equals(GaryLoadingPlugin.isDeobf ? "cloneEntry" : "func_148720_g")) {
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

    private byte[] nbtTagCompound$replaceDefaultHashMap(byte[] bytes, boolean optimizeMap, int mapThreshold, boolean canonicalizeString) {
        if ((!optimizeMap || mapThreshold == 0) && !canonicalizeString) {
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
                        iter.set(new TypeInsnNode(NEW, "zone/rong/garyasm/api/datastructures/GaryTagMap"));
                        iter.add(new InsnNode(DUP));
                        iter.add(new MethodInsnNode(INVOKESPECIAL, "zone/rong/garyasm/api/datastructures/GaryTagMap", "<init>", "()V", false));
                        break;
                    }
                }
                break;
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
                            GaryLogger.instance.info("Transforming EnumFacing::values() to EnumFacing::VALUES in {}", node.name);
                            iter.set(new FieldInsnNode(GETSTATIC, "net/minecraft/util/EnumFacing", GaryLoadingPlugin.isDeobf ? "VALUES" : "field_82609_l", "[Lnet/minecraft/util/EnumFacing;"));
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
            Map<String, Package> packages = (Map<String, Package>)  GaryReflector.getField(ClassLoader.class, "packages").get(Launch.classLoader);
            Set<String> packageStrings = packages.keySet();
            packageStrings.forEach(GaryStringPool::canonicalize);
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
                            iter.set(new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/core/GaryHooks", "createHashSet", "()Lit/unimi/dsi/fastutil/objects/ObjectOpenHashSet;", false));
                            break;
                        }
                    }
                }
            } else if (method.name.equals("addClassEntry")) { // see: GaryHooks::modCandidate$override$addClassEntry
                method.instructions.clear();
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new VarInsnNode(ALOAD, 1));
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/fml/common/discovery/ModCandidate", "foundClasses", "Ljava/util/Set;"));
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/fml/common/discovery/ModCandidate", "packages", "Ljava/util/Set;"));
                method.instructions.add(new VarInsnNode(ALOAD, 0));
                method.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/fml/common/discovery/ModCandidate", "table", "Lnet/minecraftforge/fml/common/discovery/ASMDataTable;"));
                method.instructions.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/core/GaryHooks", "modCandidate$override$addClassEntry", "(Lnet/minecraftforge/fml/common/discovery/ModCandidate;Ljava/lang/String;Ljava/util/Set;Ljava/util/Set;Lnet/minecraftforge/fml/common/discovery/ASMDataTable;)V", false));
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

        // node.fields.removeIf(f -> f.name.equals(GaryLoadingPlugin.isDeobf ? "data" : "field_74751_a"));

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>") && method.desc.equals("(Ljava/lang/String;)V")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == PUTFIELD) {
                        method.instructions.insertBefore(instruction, new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/core/GaryHooks", "nbtTagString$override$ctor", "(Ljava/lang/String;)Ljava/lang/String;", false));
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
        fields[0] = !GaryLoadingPlugin.isDeobf ? "field_82843_f" : "itemFrame";
        fields[1] = !GaryLoadingPlugin.isDeobf ? "field_179552_h" : "canDestroyCacheBlock";
        fields[2] = !GaryLoadingPlugin.isDeobf ? "field_179553_i" : "canDestroyCacheResult";
        fields[3] = !GaryLoadingPlugin.isDeobf ? "field_179550_j" : "canPlaceOnCacheBlock";
        fields[4] = !GaryLoadingPlugin.isDeobf ? "field_179551_k" : "canPlaceOnCacheResult";

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
            if (method.name.equals(GaryLoadingPlugin.isDeobf ? "enableStandardItemLighting" : "func_74519_b")) {
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
                        iter.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/api/GaryStringPool", "canonicalize", "(Ljava/lang/String;)Ljava/lang/String;", false));
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
                            iter.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/core/GaryHooks", "asmData$redirect$CtorStringsToIntern", "(Ljava/lang/String;)Ljava/lang/String;", false));
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
                            iter.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/api/GaryStringPool", "canonicalize", "(Ljava/lang/String;)Ljava/lang/String;", false));
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
            methodVisitor.visitMethodInsn(INVOKESTATIC, "zone/rong/garyasm/common/modfixes/betterwithmods/BWMBlastingOilOptimization", "inject$ItemMaterial$onEntityItemUpdate", "(Lnet/minecraft/entity/item/EntityItem;)Z", false);
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
                            iter.add(new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/bakedquad/GaryVertexDataPool", "canonicalize", "([ILnet/minecraft/client/renderer/block/model/BakedQuad;)[I", false));
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
                                methodNode.owner = "zone/rong/garyasm/core/GaryHooks";
                                methodNode.name = "createReferenceMap";
                                methodNode.desc = "()Lit/unimi/dsi/fastutil/objects/Reference2ObjectOpenHashMap;";
                                break;
                            case "newLinkedHashMap":
                                methodNode.owner = "zone/rong/garyasm/core/GaryHooks";
                                methodNode.name = "createLinkedMap";
                                methodNode.desc = "()Lit/unimi/dsi/fastutil/objects/Object2ObjectLinkedOpenHashMap;";
                                break;
                            case "newHashMap":
                                methodNode.owner = "zone/rong/garyasm/core/GaryHooks";
                                methodNode.name = "createHashMap";
                                methodNode.desc = "()Lit/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap;";
                                break;
                            case "newHashSet":
                                methodNode.owner = "zone/rong/garyasm/core/GaryHooks";
                                methodNode.name = "createHashSet";
                                methodNode.desc = "()Lit/unimi/dsi/fastutil/objects/ObjectOpenHashSet;";
                                break;
                            case "newIdentityHashSet":
                                methodNode.owner = "zone/rong/garyasm/core/GaryHooks";
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

    private byte[] delayItemStackCapabilityInit(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        String writeToNBT = !GaryLoadingPlugin.isDeobf ? "func_77955_b" : "writeToNBT";
        String isEmpty = !GaryLoadingPlugin.isDeobf ? "func_82582_d" : "isEmpty";
        String setTag = !GaryLoadingPlugin.isDeobf ? "func_74782_a" : "setTag";

        LabelNode branchLabel = new LabelNode(new Label());
        LabelNode returnLabel = null;

        all: for (MethodNode method : node.methods) {
            if (method.name.equals(writeToNBT)) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                boolean found = false;
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (!found && instruction.getOpcode() == GETFIELD) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction;
                        if (fieldInsnNode.name.equals("capabilities")) {
                            JumpInsnNode jumpInsnNode = (JumpInsnNode) iter.next(); // IFNULL L9
                            returnLabel = jumpInsnNode.label;
                            jumpInsnNode.label = branchLabel;
                            found = true;
                        }
                    } else if (found && instruction == returnLabel) {
                        InsnList instructions = new InsnList();
                        instructions.add(new JumpInsnNode(GOTO, returnLabel));
                        instructions.add(branchLabel);
                        instructions.add(new FrameNode(F_SAME, -1, null, -1, null));
                        instructions.add(new VarInsnNode(ALOAD, 0));
                        instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/item/ItemStack", "capNBT", "Lnet/minecraft/nbt/NBTTagCompound;"));
                        instructions.add(new JumpInsnNode(IFNULL, returnLabel));
                        instructions.add(new VarInsnNode(ALOAD, 0));
                        instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/item/ItemStack", "capNBT", "Lnet/minecraft/nbt/NBTTagCompound;"));
                        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", isEmpty, "()Z", false));
                        instructions.add(new JumpInsnNode(IFNE, returnLabel));
                        LabelNode branchNode = new LabelNode(new Label());
                        instructions.add(branchNode);
                        instructions.add(new VarInsnNode(ALOAD, 1));
                        instructions.add(new LdcInsnNode("ForgeCaps"));
                        instructions.add(new VarInsnNode(ALOAD, 0));
                        instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/item/ItemStack", "capNBT", "Lnet/minecraft/nbt/NBTTagCompound;"));
                        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", setTag, "(Ljava/lang/String;Lnet/minecraft/nbt/NBTBase;)V", false));
                        method.instructions.insertBefore(instruction, instructions);
                        break all;
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] removeExcessiveGCCalls(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        String loadWorld = !GaryLoadingPlugin.isDeobf ? "func_71353_a" : "loadWorld";

        for (MethodNode method : node.methods) {
            if (method.name.equals(loadWorld)) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == INVOKESTATIC) {
                        MethodInsnNode invokeStatic = (MethodInsnNode) instruction;
                        if (invokeStatic.owner.equals("java/lang/System") && invokeStatic.name.equals("gc")) {
                            LabelNode l56 = (LabelNode) iter.next(); // Capture for earlier GOTO
                            iter.previous();
                            iter.previous();
                            iter.remove(); // INVOKESTATIC
                            iter.previous();
                            iter.remove(); // FRAME SAME
                            iter.previous();
                            iter.remove(); // LINENUMBER
                            LabelNode l54 = (LabelNode) iter.previous();
                            iter.remove(); // LABEL L54
                            while (iter.hasPrevious()) {
                                AbstractInsnNode previousInstruction = iter.previous();
                                if (previousInstruction.getOpcode() == GOTO) {
                                    JumpInsnNode gotoNode = (JumpInsnNode) previousInstruction;
                                    if (gotoNode.label == l54) {
                                        gotoNode.label = l56;
                                        break;
                                    }
                                }
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

    private byte[] smoothDimensionChange(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        String handleJoinGame = !GaryLoadingPlugin.isDeobf ? "func_147282_a" : "handleJoinGame";
        String handleRespawn = !GaryLoadingPlugin.isDeobf ? "func_147280_a" : "handleRespawn";

        for (MethodNode method : node.methods) {
            if (method.name.equals(handleJoinGame) || method.name.equals(handleRespawn)) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == INVOKESPECIAL) {
                        MethodInsnNode invokeSpecial = (MethodInsnNode) instruction;
                        if (invokeSpecial.owner.equals("net/minecraft/client/gui/GuiDownloadTerrain")) {
                            iter.remove(); // INVOKESPECIAL
                            iter.previous();
                            iter.remove(); // DUP
                            iter.previous();
                            iter.set(new InsnNode(ACONST_NULL)); // replaces NEW
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] fixMC30845(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        String orientCamera = !GaryLoadingPlugin.isDeobf ? "func_78467_g" : "orientCamera";
        String rayTraceBlocksOld = !GaryLoadingPlugin.isDeobf ? "func_72933_a" : "rayTraceBlocks";
        String rayTraceBlocksNew = !GaryLoadingPlugin.isDeobf ? "func_147447_a" : "rayTraceBlocks";

        for (MethodNode method : node.methods) {
            if (method.name.equals(orientCamera)) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == INVOKEVIRTUAL) {
                        MethodInsnNode invokeVirtual = (MethodInsnNode) instruction;
                        if (invokeVirtual.name.equals(rayTraceBlocksOld)) {
                            iter.set(new InsnNode(ICONST_0));
                            iter.add(new InsnNode(ICONST_1));
                            iter.add(new InsnNode(ICONST_1));
                            iter.add(new MethodInsnNode(
                                    invokeVirtual.getOpcode(),
                                    invokeVirtual.owner,
                                    rayTraceBlocksNew,
                                    "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;",
                                    false));
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] fixMC31681(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        String updateRenderer = !GaryLoadingPlugin.isDeobf ? "func_78464_a" : "updateRenderer";
        String renderDistanceChunks = !GaryLoadingPlugin.isDeobf ? "field_151451_c" : "renderDistanceChunks";

        for (MethodNode method : node.methods) {
            if (method.name.equals(updateRenderer)) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == GETFIELD) {
                        FieldInsnNode getField = (FieldInsnNode) instruction;
                        if (getField.name.equals(renderDistanceChunks)) {
                            iter.remove(); // GETFIELD
                            iter.previous();
                            iter.remove(); // GETFIELD
                            iter.previous();
                            iter.remove(); // GETFIELD
                            iter.previous();
                            iter.remove(); // ALOAD 0
                            iter.next();
                            iter.remove(); // I2F
                            iter.next();
                            iter.remove(); // LDC 32.0
                            iter.next();
                            iter.set(new InsnNode(FCONST_1)); // Replaces FDIV
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] disappearingEntitiesRenderGlobalFix(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        String setupTerrain = !GaryLoadingPlugin.isDeobf ? "func_174970_a" : "setupTerrain";
        String renderChunkBoundingBox = !GaryLoadingPlugin.isDeobf ? "field_178591_c" : "boundingBox";

        for (MethodNode method : node.methods) {
            if (method.name.equals(setupTerrain)) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == GETFIELD) {
                        FieldInsnNode getField = (FieldInsnNode) instruction;
                        if (getField.name.equals(renderChunkBoundingBox)) {
                            iter.set(new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/patches/RenderGlobalPatch", "getCorrectBoundingBox", "(Lnet/minecraft/client/renderer/chunk/RenderChunk;)Lnet/minecraft/util/math/AxisAlignedBB;", false));
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] disappearingEntitiesRenderChunkFix(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        String renderChunkBoundingBox = !GaryLoadingPlugin.isDeobf ? "field_178591_c" : "boundingBox";

        for (MethodNode method : node.methods) {
            /* OptiFine adds this */
            if (method.name.equals("isBoundingBoxInFrustum")) {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode instruction = iter.next();
                    if (instruction.getOpcode() == GETFIELD) {
                        FieldInsnNode getField = (FieldInsnNode) instruction;
                        if (getField.name.equals(renderChunkBoundingBox)) {
                            iter.set(new MethodInsnNode(INVOKESTATIC, "zone/rong/garyasm/patches/RenderGlobalPatch", "getCorrectBoundingBox", "(Lnet/minecraft/client/renderer/chunk/RenderChunk;)Lnet/minecraft/util/math/AxisAlignedBB;", false));
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

}
