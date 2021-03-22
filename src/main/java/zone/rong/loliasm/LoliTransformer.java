package zone.rong.loliasm;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.*;
import zone.rong.loliasm.patches.*;

import java.io.IOException;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.*;

public class LoliTransformer implements IClassTransformer {

    public static final boolean isDeobf = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    Map<String, Function<byte[], byte[]>> transformations;

    public LoliTransformer() throws IOException {
        LoliLogger.instance.info("The lolis are now preparing to bytecode manipulate your game.");
        LoliConfig.Data data = LoliConfig.initConfig();
        if (data.softPatch) {
            transformations = new Object2ObjectOpenHashMap<>(2);
            addTransformation("net.minecraft.client.renderer.block.model.BakedQuad", this::optimize$BakedQuad);
            addTransformation("net.minecraft.client.renderer.block.model.BakedQuadRetextured", this::fixReferences$BakedQuadRetextured);
        } else if (data.hardPatch) {
            transformations = new Object2ObjectOpenHashMap<>(5 + data.hardPatchClasses.length);
            addTransformation("net.minecraft.client.renderer.block.model.BakedQuad", BakedQuadPatch::rewriteBakedQuad);
            addTransformation("net.minecraft.client.renderer.block.model.BakedQuadRetextured", BakedQuadRetexturedPatch::patchBakedQuadRetextured);
            addTransformation("net.minecraftforge.client.model.pipeline.UnpackedBakedQuad", UnpackedBakedQuadPatch::rewriteUnpackedBakedQuad);
            addTransformation("net.minecraftforge.client.model.pipeline.UnpackedBakedQuad$Builder", UnpackedBakedQuadPatch::rewriteUnpackedBakedQuad$Builder);
            addTransformation("zone.rong.loliasm.bakedquad.BakedQuadFactory", BakedQuadFactoryPatch::patchCreateMethod);
            for (String classToPatch : data.hardPatchClasses) {
                // addTransformation(classToPatch, this::redirectNewBakedQuadCalls);
                addTransformation(classToPatch, this::redirectNewBakedQuadCalls$EventBased);
            }
        }
    }

    public void addTransformation(String key, Function<byte[], byte[]> value) {
        LoliLogger.instance.info("Adding class {} to the transformation queue", key);
        transformations.put(key, value);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (transformations == null) {
            return bytes;
        }
        Function<byte[], byte[]> getBytes = transformations.get(transformedName);
        if (getBytes != null) {
            return getBytes.apply(bytes);
        }
        return bytes;
    }

    // Better way
    private byte[] redirectNewBakedQuadCalls$EventBased(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, 0);

        MethodInsnNode node = new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/bakedquad/BakedQuadFactory", "create", "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZLnet/minecraft/client/renderer/vertex/VertexFormat;)Lnet/minecraft/client/renderer/block/model/BakedQuad;", false);

        reader.accept(new RedirectNewWithStaticCallClassVisitor(writer, "net/minecraft/client/renderer/block/model/BakedQuad", node), 0);

        return writer.toByteArray();
    }

    @Deprecated
    private byte[] redirectNewBakedQuadCalls(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        final String creationDesc = "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZLnet/minecraft/client/renderer/vertex/VertexFormat;)Lnet/minecraft/client/renderer/block/model/BakedQuad;";

        for (MethodNode method : node.methods) {
            ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
            while (iter.hasNext()) {
                AbstractInsnNode instruction = iter.next();
                if (instruction instanceof TypeInsnNode) {
                    TypeInsnNode newInstruction = (TypeInsnNode) instruction;
                    if (newInstruction.desc.equals("net/minecraft/client/renderer/block/model/BakedQuad")) {
                        iter.remove(); // Remove NEW
                        iter.next();
                        iter.remove(); // Remove DUP
                    }
                }
                if (instruction instanceof MethodInsnNode && instruction.getOpcode() == INVOKESPECIAL) {
                    MethodInsnNode methodInstruction = (MethodInsnNode) instruction;
                    if (methodInstruction.name.equals("<init>") && methodInstruction.owner.equals("net/minecraft/client/renderer/block/model/BakedQuad")) {
                        MethodInsnNode replaceInstruction = new MethodInsnNode(INVOKESTATIC, "zone/rong/loliasm/bakedquad/BakedQuadFactory", "create", creationDesc, false);
                        // Replace call to BakedQuad::new, with EfficientBakedQuadsFactory::create
                        iter.remove();
                        iter.add(replaceInstruction);
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] optimize$BakedQuad(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        final String tintIndex = isDeobf ? "tintIndex" : "field_178213_b";
        final String ctor = "<init>";
        final String hasTintIndex = isDeobf ? "hasTintIndex" : "func_178212_b";
        final String getTintIndex = isDeobf ? "getTintIndex" : "func_178211_c";

        // Transform tintIndex int field -> byte field
        for (FieldNode field : node.fields) {
            if (field.name.equals(tintIndex)) {
                field.desc = "B";
                break;
            }
        }

        for (MethodNode method : node.methods) {
            if (method.access == 0x1 && method.name.equals(ctor)) {
                ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode instruction = iterator.next();
                    if (instruction.getOpcode() == PUTFIELD) {
                        FieldInsnNode fieldInstruction = (FieldInsnNode) instruction;
                        if (fieldInstruction.name.equals(tintIndex)) {
                            method.instructions.insertBefore(instruction, new InsnNode(I2B));
                            fieldInstruction.desc = "B";
                            break;
                        }
                    }
                }
            } else if (method.name.equals(hasTintIndex) || method.name.equals(getTintIndex)) {
                ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode instruction = iterator.next();
                    if (instruction.getOpcode() == GETFIELD) {
                        FieldInsnNode fieldInstruction = (FieldInsnNode) instruction;
                        if (fieldInstruction.name.equals(tintIndex)) {
                            fieldInstruction.desc = "B";
                            break;
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] fixReferences$BakedQuadRetextured(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        final String tintIndex = isDeobf ? "tintIndex" : "field_178213_b";
        final String ctor = "<init>";
        // final String applyDiffuseLighting = "applyDiffuseLighting"; // forge-added, no-deobf.

        for (MethodNode method : node.methods) {
            if (method.access == 0x1 && method.name.equals(ctor)) {
                ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode instruction = iterator.next();
                    if (instruction.getOpcode() == GETFIELD) {
                        FieldInsnNode fieldInstruction = (FieldInsnNode) instruction;
                        if (fieldInstruction.name.equals(tintIndex)) {
                            fieldInstruction.desc = "B";
                            break;
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    static class RedirectNewWithStaticCallClassVisitor extends ClassVisitor {

        final String ownerOfNewCall;
        final MethodInsnNode staticRedirect;

        RedirectNewWithStaticCallClassVisitor(ClassVisitor cv, String ownerOfNewCall, MethodInsnNode staticRedirect) {
            super(ASM5, cv);
            this.ownerOfNewCall = ownerOfNewCall;
            this.staticRedirect = staticRedirect;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new ReplaceNewWithStaticCall(ASM5, cv.visitMethod(access, name, desc, signature, exceptions), ownerOfNewCall, staticRedirect);
        }
    }

    static class ReplaceNewWithStaticCall extends MethodVisitor {

        final String ownerOfNewCall;
        final MethodInsnNode staticRedirect;

        boolean foundBakedQuad = false;

        ReplaceNewWithStaticCall(int api, MethodVisitor mv, String ownerOfNewCall, MethodInsnNode staticRedirect) {
            super(api, mv);
            this.ownerOfNewCall = ownerOfNewCall;
            this.staticRedirect = staticRedirect;
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (opcode == NEW && type.equals(ownerOfNewCall)) {
                foundBakedQuad = true;
            } else {
                super.visitTypeInsn(opcode, type);
            }
        }

        @Override
        public void visitInsn(int opcode) {
            if (foundBakedQuad && opcode == DUP) {
                foundBakedQuad = false;
            } else {
                super.visitInsn(opcode);
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == INVOKESPECIAL && owner.equals(ownerOfNewCall) && name.equals("<init>")) {
                super.visitMethodInsn(staticRedirect.getOpcode(), staticRedirect.owner, staticRedirect.name, staticRedirect.desc, staticRedirect.itf);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }
    }

}
