package zone.rong.loliasm.patches;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import zone.rong.loliasm.core.LoliLoadingPlugin;
import zone.rong.loliasm.core.LoliTransformer;

import static org.objectweb.asm.Opcodes.*;

/**
 * This class contains class writers for patched BakedQuadRetextured
 * defineClass not called here, pass raw byte[] back to {@link LoliTransformer#transform(String, String, byte[])}
 *
 * It is one of the very few instances where BakedQuad actually gets extended.
 *
 * Not many but still worthwhile optimizations here.
 *
 * 1. We keep the previous BakedQuad reference.
 * => This is suitable since the reference won't be kept because of this new BakedQuadRetextured instance
 * => The reference will only be lost when the bakedmodel is expended, which at that point in time - this BakedQuadRetextured will get thrown out anyways
 *
 * 2. Calling int[].clone() instead of Arrays.copyOf
 * => Shallow copies are fine, after all its an 1-D array.
 * => Skips 2 layers of wrapped calls, arrayCopy is a native call that calls clone() (?)
 *
 * 3. No need for an additional TextureAtlasSprite reference
 * => We remap the quad's vertexData within the ctor
 *
 * Only real 'downside' is delegating calls of some of the overriden methods to old BakedQuad, you all know that's nothing.
 */
public final class BakedQuadRetexturedPatch {
    
    public static byte[] patchBakedQuadRetextured(byte[] originalClass) {
        ClassWriter writer = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;
        
        writer.visit(52, ACC_PUBLIC | ACC_SUPER, "net/minecraft/client/renderer/block/model/BakedQuadRetextured", null, "net/minecraft/client/renderer/block/model/BakedQuad", null);

        writer.visitSource("BakedQuadRetextured.java", null);

        {
            fieldVisitor = writer.visitField(ACC_PRIVATE | ACC_FINAL, "quad", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", "(Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(12, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", LoliLoadingPlugin.isDeobf ? "vertexData" : "field_178215_a", "[I");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "[I", "clone", "()Ljava/lang/Object;", false);
            methodVisitor.visitTypeInsn(CHECKCAST, "[I");
            // methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/block/model/BakedQuad", "getVertexData", "()[I", false);
            // methodVisitor.visitInsn(ARRAYLENGTH);
            // methodVisitor.visitIntInsn(NEWARRAY, T_INT);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/block/model/BakedQuad", "getFormat", "()Lnet/minecraft/client/renderer/vertex/VertexFormat;", false);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/minecraft/client/renderer/block/model/BakedQuad", "<init>", "([ILnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/vertex/VertexFormat;)V", false);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLineNumber(13, l1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/client/renderer/block/model/BakedQuadRetextured", "quad", "Lnet/minecraft/client/renderer/block/model/BakedQuad;");
            Label l2 = new Label();
            methodVisitor.visitLabel(l2);
            methodVisitor.visitLineNumber(14, l2);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ISTORE, 3);
            Label l3 = new Label();
            methodVisitor.visitLabel(l3);
            methodVisitor.visitFrame(F_FULL, 4, new Object[]{"net/minecraft/client/renderer/block/model/BakedQuadRetextured", "net/minecraft/client/renderer/block/model/BakedQuad", "net/minecraft/client/renderer/texture/TextureAtlasSprite", INTEGER}, 0, new Object[]{});
            methodVisitor.visitVarInsn(ILOAD, 3);
            methodVisitor.visitInsn(ICONST_4);
            Label l4 = new Label();
            methodVisitor.visitJumpInsn(IF_ICMPGE, l4);
            Label l5 = new Label();
            methodVisitor.visitLabel(l5);
            methodVisitor.visitLineNumber(15, l5);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuadRetextured", "format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/vertex/VertexFormat", LoliLoadingPlugin.isDeobf ? "getIntegerSize" : "func_181719_f", "()I", false);
            methodVisitor.visitVarInsn(ILOAD, 3);
            methodVisitor.visitInsn(IMUL);
            methodVisitor.visitVarInsn(ISTORE, 4);
            Label l6 = new Label();
            methodVisitor.visitLabel(l6);
            methodVisitor.visitLineNumber(16, l6);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuadRetextured", "format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;");
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/vertex/VertexFormat", LoliLoadingPlugin.isDeobf ? "getUvOffsetById" : "func_177344_b", "(I)I", false);
            methodVisitor.visitInsn(ICONST_4);
            methodVisitor.visitInsn(IDIV);
            methodVisitor.visitVarInsn(ISTORE, 5);
            Label l7 = new Label();
            methodVisitor.visitLabel(l7);
            methodVisitor.visitLineNumber(17, l7);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuadRetextured", LoliLoadingPlugin.isDeobf ? "vertexData" : "field_178215_a", "[I");
            methodVisitor.visitVarInsn(ILOAD, 4);
            methodVisitor.visitVarInsn(ILOAD, 5);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/block/model/BakedQuad", LoliLoadingPlugin.isDeobf ? "getSprite" : "func_187508_a", "()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", false);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuadRetextured", LoliLoadingPlugin.isDeobf ? "vertexData" : "field_178215_a", "[I");
            methodVisitor.visitVarInsn(ILOAD, 4);
            methodVisitor.visitVarInsn(ILOAD, 5);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitInsn(IALOAD);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "intBitsToFloat", "(I)F", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/texture/TextureAtlasSprite", LoliLoadingPlugin.isDeobf ? "getUnInterpolatedU" : "func_188537_a", "(F)F", false);
            methodVisitor.visitInsn(F2D);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/texture/TextureAtlasSprite", LoliLoadingPlugin.isDeobf ? "getInterpolatedU" : "func_94214_a", "(D)F", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "floatToRawIntBits", "(F)I", false);
            methodVisitor.visitInsn(IASTORE);
            Label l8 = new Label();
            methodVisitor.visitLabel(l8);
            methodVisitor.visitLineNumber(18, l8);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuadRetextured", LoliLoadingPlugin.isDeobf ? "vertexData" : "field_178215_a", "[I");
            methodVisitor.visitVarInsn(ILOAD, 4);
            methodVisitor.visitVarInsn(ILOAD, 5);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/block/model/BakedQuad", LoliLoadingPlugin.isDeobf ? "getSprite" : "func_187508_a", "()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", false);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuadRetextured", LoliLoadingPlugin.isDeobf ? "vertexData" : "field_178215_a", "[I");
            methodVisitor.visitVarInsn(ILOAD, 4);
            methodVisitor.visitVarInsn(ILOAD, 5);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitInsn(IALOAD);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "intBitsToFloat", "(I)F", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/texture/TextureAtlasSprite", LoliLoadingPlugin.isDeobf ? "getUnInterpolatedV" : "func_188536_b", "(F)F", false);
            methodVisitor.visitInsn(F2D);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/texture/TextureAtlasSprite", LoliLoadingPlugin.isDeobf ? "getInterpolatedV" : "func_94207_b", "(D)F", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "floatToRawIntBits", "(F)I", false);
            methodVisitor.visitInsn(IASTORE);
            Label l9 = new Label();
            methodVisitor.visitLabel(l9);
            methodVisitor.visitLineNumber(14, l9);
            methodVisitor.visitIincInsn(3, 1);
            methodVisitor.visitJumpInsn(GOTO, l3);
            methodVisitor.visitLabel(l4);
            methodVisitor.visitLineNumber(20, l4);
            methodVisitor.visitFrame(F_CHOP, 1, null, 0, null);
            methodVisitor.visitInsn(RETURN);
            Label l10 = new Label();
            methodVisitor.visitLabel(l10);
            methodVisitor.visitLocalVariable("j", "I", null, l6, l9, 4);
            methodVisitor.visitLocalVariable("uvIndex", "I", null, l7, l9, 5);
            methodVisitor.visitLocalVariable("i", "I", null, l3, l4, 3);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuadRetextured;", null, l0, l10, 0);
            methodVisitor.visitLocalVariable("quad", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l10, 1);
            methodVisitor.visitLocalVariable("textureIn", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, l0, l10, 2);
            methodVisitor.visitMaxs(7, 6);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "hasTintIndex" : "func_178212_b", "()Z", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(24, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuadRetextured", "quad", "Lnet/minecraft/client/renderer/block/model/BakedQuad;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/block/model/BakedQuad", LoliLoadingPlugin.isDeobf ? "hasTintIndex" : "func_178212_b", "()Z", false);
            methodVisitor.visitInsn(IRETURN);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuadRetextured;", null, l0, l1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "getTintIndex" : "func_178211_c", "()I", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(29, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuadRetextured", "quad", "Lnet/minecraft/client/renderer/block/model/BakedQuad;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/block/model/BakedQuad", LoliLoadingPlugin.isDeobf ? "getTintIndex" : "func_178211_c", "()I", false);
            methodVisitor.visitInsn(IRETURN);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuadRetextured;", null, l0, l1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "getFace" : "func_178210_d", "()Lnet/minecraft/util/EnumFacing;", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(34, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuadRetextured", "quad", "Lnet/minecraft/client/renderer/block/model/BakedQuad;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/block/model/BakedQuad", LoliLoadingPlugin.isDeobf ? "getFace" : "func_178210_d", "()Lnet/minecraft/util/EnumFacing;", false);
            methodVisitor.visitInsn(ARETURN);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuadRetextured;", null, l0, l1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, "shouldApplyDiffuseLighting", "()Z", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(39, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuadRetextured", "quad", "Lnet/minecraft/client/renderer/block/model/BakedQuad;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/client/renderer/block/model/BakedQuad", "shouldApplyDiffuseLighting", "()Z", false);
            methodVisitor.visitInsn(IRETURN);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuadRetextured;", null, l0, l1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        writer.visitEnd();
        
        return writer.toByteArray();
    }

    private BakedQuadRetexturedPatch() { }
    
}
