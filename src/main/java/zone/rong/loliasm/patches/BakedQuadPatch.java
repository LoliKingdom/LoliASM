package zone.rong.loliasm.patches;

import org.objectweb.asm.*;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.core.LoliLoadingPlugin;
import zone.rong.loliasm.core.LoliTransformer;

import static org.objectweb.asm.Opcodes.*;

/**
 * TODO Commenting
 *
 * This class contains class writers for patched BakedQuad
 * defineClass not called here, pass raw byte[] back to {@link LoliTransformer#transform(String, String, byte[])}
 *
 * Optimizations:
 *
 * 1. Reduced memory footprint
 * => Normally, a BakedQuad consists of:
 *      => final int[] vertexData;
 *      => final int tintIndex;
 *      => final EnumFacing facing;
 *      => final TextureAtlasSprite sprite;
 *      => final VertexFormat format;
 *      => final boolean applyDiffuseLighting;
 * => (Estimated) Shallow Heap: ~40b, Retained Heap: ~168b (Retained Heap does not really matter as these objects rarely free up)
 * => The only way is if you introduce a model caching system, or use experimental VanillaFix (Shuuseigatari)
 * => The changes made here:
 *      => Removed `int tintIndex` (soft patch modifies this field to a byte field)
 *      => Removed `EnumFacing facing`; field
 *      => Removed `boolean applyDiffuseLighting` field
 *      => TODO: remove `VertexFormat format` field (most of these BakedQuads reference to {@link net.minecraft.client.renderer.vertex.DefaultVertexFormats#ITEM}!
 * => That reduces the shallow heap estimation from ~40b to ~24b (nearly half!), retained heap from ~168b to ~152b
 *
 * Physical representation: {@link zone.rong.loliasm.patches.visualization.BakedQuad}
 */
public final class BakedQuadPatch {

    public static byte[] rewriteBakedQuad(byte[] originalClass) {
        ClassWriter writer = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;
        AnnotationVisitor annotationVisitor;

        writer.visit(52, ACC_PUBLIC | ACC_SUPER, "net/minecraft/client/renderer/block/model/BakedQuad", null, "java/lang/Object", new String[]{ "net/minecraftforge/client/model/pipeline/IVertexProducer" });

        writer.visitSource("BakedQuad.java", null);

        {
            annotationVisitor = writer.visitAnnotation("Lnet/minecraftforge/fml/relauncher/SideOnly;", true);
            annotationVisitor.visitEnum("value", "Lnet/minecraftforge/fml/relauncher/Side;", "CLIENT");
            annotationVisitor.visitEnd();
        }
        {
            fieldVisitor = writer.visitField(ACC_PROTECTED | ACC_FINAL, LoliLoadingPlugin.isDeobf ? "vertexData" : "field_178215_a", "[I", null, null);
            fieldVisitor.visitEnd();
        }
        {
            fieldVisitor = writer.visitField(ACC_PROTECTED | ACC_FINAL, LoliLoadingPlugin.isDeobf ? "sprite" : "field_187509_d", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            fieldVisitor = writer.visitField(ACC_PROTECTED | ACC_FINAL, "format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC | ACC_DEPRECATED, "<init>", "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V", null, null);
            {
                annotationVisitor = methodVisitor.visitAnnotation("Ljava/lang/Deprecated;", true);
                annotationVisitor.visitEnd();
            }
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(29, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ILOAD, 2);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/client/renderer/vertex/DefaultVertexFormats", LoliLoadingPlugin.isDeobf ? "ITEM" : "field_176599_b", "Lnet/minecraft/client/renderer/vertex/VertexFormat;");
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/minecraft/client/renderer/block/model/BakedQuad", "<init>", "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZLnet/minecraft/client/renderer/vertex/VertexFormat;)V", false);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLineNumber(30, l1);
            methodVisitor.visitInsn(RETURN);
            Label l2 = new Label();
            methodVisitor.visitLabel(l2);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l2, 0);
            methodVisitor.visitLocalVariable("vertexDataIn", "[I", null, l0, l2, 1);
            methodVisitor.visitLocalVariable("tintIndexIn", "I", null, l0, l2, 2);
            methodVisitor.visitLocalVariable("faceIn", "Lnet/minecraft/util/EnumFacing;", null, l0, l2, 3);
            methodVisitor.visitLocalVariable("spriteIn", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, l0, l2, 4);
            methodVisitor.visitMaxs(7, 5);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC | ACC_DEPRECATED, "<init>", "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZLnet/minecraft/client/renderer/vertex/VertexFormat;)V", null, null);
            {
                annotationVisitor = methodVisitor.visitAnnotation("Ljava/lang/Deprecated;", true);
                annotationVisitor.visitEnd();
            }
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(40, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLineNumber(41, l1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 6);
            methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", "format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;");
            Label l2 = new Label();
            methodVisitor.visitLabel(l2);
            methodVisitor.visitLineNumber(42, l2);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", LoliLoadingPlugin.isDeobf ? "vertexData" : "field_178215_a", "[I");
            Label l3 = new Label();
            methodVisitor.visitLabel(l3);
            methodVisitor.visitLineNumber(43, l3);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", LoliLoadingPlugin.isDeobf ? "sprite" : "field_187509_d", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;");
            Label l4 = new Label();
            methodVisitor.visitLabel(l4);
            methodVisitor.visitLineNumber(44, l4);
            Label startingLabel = null;
            if (LoliConfig.instance.logClassesThatCallBakedQuadCtor) {
                methodVisitor.visitFieldInsn(GETSTATIC, "me/nallar/whocalled/WhoCalled", "$", "Lme/nallar/whocalled/WhoCalled;");
                methodVisitor.visitInsn(ICONST_1);
                methodVisitor.visitMethodInsn(INVOKEINTERFACE, "me/nallar/whocalled/WhoCalled", "getCallingClass", "(I)Ljava/lang/Class;", true);
                methodVisitor.visitVarInsn(ASTORE, 7);
                Label l5 = new Label();
                startingLabel = l5;
                methodVisitor.visitLabel(l5);
                methodVisitor.visitLineNumber(45, l5);
                methodVisitor.visitVarInsn(ALOAD, 7);
                methodVisitor.visitLdcInsn(Type.getType("Lnet/minecraft/client/renderer/block/model/BakedQuad;"));
                Label l6 = new Label();
                methodVisitor.visitJumpInsn(IF_ACMPNE, l6);
                Label l7 = new Label();
                methodVisitor.visitLabel(l7);
                methodVisitor.visitLineNumber(46, l7);
                methodVisitor.visitFieldInsn(GETSTATIC, "me/nallar/whocalled/WhoCalled", "$", "Lme/nallar/whocalled/WhoCalled;");
                methodVisitor.visitInsn(ICONST_2);
                methodVisitor.visitMethodInsn(INVOKEINTERFACE, "me/nallar/whocalled/WhoCalled", "getCallingClass", "(I)Ljava/lang/Class;", true);
                methodVisitor.visitVarInsn(ASTORE, 7);
                methodVisitor.visitLabel(l6);
                methodVisitor.visitLineNumber(48, l6);
                methodVisitor.visitFrame(Opcodes.F_FULL, 8, new Object[]{"net/minecraft/client/renderer/block/model/BakedQuad", "[I", Opcodes.INTEGER, "net/minecraft/util/EnumFacing", "net/minecraft/client/renderer/texture/TextureAtlasSprite", Opcodes.INTEGER, "net/minecraft/client/renderer/vertex/VertexFormat", "java/lang/Class"}, 0, new Object[]{});
                methodVisitor.visitFieldInsn(GETSTATIC, "zone/rong/loliasm/LoliLogger", "instance", "Lorg/apache/logging/log4j/Logger;");
                methodVisitor.visitLdcInsn("{} needs their BakedQuad calls redirecting! Insert the string into config/loliasm.json and report to Rongmario.");
                methodVisitor.visitVarInsn(ALOAD, 7);
                methodVisitor.visitMethodInsn(INVOKEINTERFACE, "org/apache/logging/log4j/Logger", "warn", "(Ljava/lang/String;Ljava/lang/Object;)V", true);
                Label l8 = new Label();
                methodVisitor.visitLabel(l8);
                methodVisitor.visitLineNumber(49, l8);
            }
            methodVisitor.visitInsn(RETURN);
            Label finalLabel = new Label();
            methodVisitor.visitLabel(finalLabel);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, finalLabel, 0);
            methodVisitor.visitLocalVariable("vertexDataIn", "[I", null, l0, finalLabel, 1);
            methodVisitor.visitLocalVariable("tintIndexIn", "I", null, l0, finalLabel, 2);
            methodVisitor.visitLocalVariable("faceIn", "Lnet/minecraft/util/EnumFacing;", null, l0, finalLabel, 3);
            methodVisitor.visitLocalVariable("spriteIn", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, l0, finalLabel, 4);
            methodVisitor.visitLocalVariable("applyDiffuseLighting", "Z", null, l0, finalLabel, 5);
            methodVisitor.visitLocalVariable("format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, l0, finalLabel, 6);
            if (LoliConfig.instance.logClassesThatCallBakedQuadCtor) {
                methodVisitor.visitLocalVariable("callee", "Ljava/lang/Class;", null, startingLabel, finalLabel, 7);
                methodVisitor.visitMaxs(3, 8);
            } else {
                methodVisitor.visitMaxs(2, 7);
            }
            methodVisitor.visitEnd();
            /*
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(34, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLineNumber(35, l1);
            methodVisitor.visitFieldInsn(GETSTATIC, "me/nallar/whocalled/WhoCalled", "$", "Lme/nallar/whocalled/WhoCalled;");
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "me/nallar/whocalled/WhoCalled", "getCallingClass", "(I)Ljava/lang/Class;", true);
            methodVisitor.visitVarInsn(ASTORE, 7);
            Label l2 = new Label();
            methodVisitor.visitLabel(l2);
            methodVisitor.visitLineNumber(36, l2);
            methodVisitor.visitVarInsn(ALOAD, 7);
            methodVisitor.visitLdcInsn(Type.getType("Lnet/minecraft/client/renderer/block/model/BakedQuad;"));
            Label l3 = new Label();
            methodVisitor.visitJumpInsn(IF_ACMPNE, l3);
            Label l4 = new Label();
            methodVisitor.visitLabel(l4);
            methodVisitor.visitLineNumber(37, l4);
            methodVisitor.visitFieldInsn(GETSTATIC, "me/nallar/whocalled/WhoCalled", "$", "Lme/nallar/whocalled/WhoCalled;");
            methodVisitor.visitInsn(ICONST_2);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "me/nallar/whocalled/WhoCalled", "getCallingClass", "(I)Ljava/lang/Class;", true);
            methodVisitor.visitVarInsn(ASTORE, 7);
            methodVisitor.visitLabel(l3);
            methodVisitor.visitLineNumber(39, l3);
            methodVisitor.visitFrame(Opcodes.F_FULL, 8, new Object[]{ "net/minecraft/client/renderer/block/model/BakedQuad", "[I", Opcodes.INTEGER, "net/minecraft/util/EnumFacing", "net/minecraft/client/renderer/texture/TextureAtlasSprite", Opcodes.INTEGER, "net/minecraft/client/renderer/vertex/VertexFormat", "java/lang/Class"}, 0, new Object[]{});
            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            methodVisitor.visitLdcInsn("Callee: ");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitVarInsn(ALOAD, 7);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label l5 = new Label();
            methodVisitor.visitLabel(l5);
            methodVisitor.visitLineNumber(40, l5);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 6);
            methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", "format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;");
            Label l6 = new Label();
            methodVisitor.visitLabel(l6);
            methodVisitor.visitLineNumber(41, l6);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", "vertexData", "[I");
            Label l7 = new Label();
            methodVisitor.visitLabel(l7);
            methodVisitor.visitLineNumber(42, l7);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", "sprite", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;");
            Label l8 = new Label();
            methodVisitor.visitLabel(l8);
            methodVisitor.visitLineNumber(43, l8);
            methodVisitor.visitInsn(RETURN);
            Label l9 = new Label();
            methodVisitor.visitLabel(l9);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l9, 0);
            methodVisitor.visitLocalVariable("vertexDataIn", "[I", null, l0, l9, 1);
            methodVisitor.visitLocalVariable("tintIndexIn", "I", null, l0, l9, 2);
            methodVisitor.visitLocalVariable("faceIn", "Lnet/minecraft/util/EnumFacing;", null, l0, l9, 3);
            methodVisitor.visitLocalVariable("spriteIn", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, l0, l9, 4);
            methodVisitor.visitLocalVariable("applyDiffuseLighting", "Z", null, l0, l9, 5);
            methodVisitor.visitLocalVariable("format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, l0, l9, 6);
            methodVisitor.visitLocalVariable("callee", "Ljava/lang/Class;", "Ljava/lang/Class<*>;", l2, l9, 7);
            methodVisitor.visitMaxs(3, 8);
            methodVisitor.visitEnd();
             */
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", "([ILnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/vertex/VertexFormat;)V", null, null);
            {
                annotationVisitor = methodVisitor.visitAnnotation("Lcom/google/common/annotations/Beta;", false);
                annotationVisitor.visitEnd();
            }
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(47, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLineNumber(48, l1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", LoliLoadingPlugin.isDeobf ? "vertexData" : "field_178215_a", "[I");
            Label l2 = new Label();
            methodVisitor.visitLabel(l2);
            methodVisitor.visitLineNumber(49, l2);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", LoliLoadingPlugin.isDeobf ? "sprite" : "field_187509_d", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;");
            Label l3 = new Label();
            methodVisitor.visitLabel(l3);
            methodVisitor.visitLineNumber(50, l3);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitFieldInsn(PUTFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", "format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;");
            Label l4 = new Label();
            methodVisitor.visitLabel(l4);
            methodVisitor.visitLineNumber(51, l4);
            methodVisitor.visitInsn(RETURN);
            Label l5 = new Label();
            methodVisitor.visitLabel(l5);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l5, 0);
            methodVisitor.visitLocalVariable(LoliLoadingPlugin.isDeobf ? "vertexData" : "field_178215_a", "[I", null, l0, l5, 1);
            methodVisitor.visitLocalVariable(LoliLoadingPlugin.isDeobf ? "sprite" : "field_187509_d", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, l0, l5, 2);
            methodVisitor.visitLocalVariable("format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, l0, l5, 3);
            methodVisitor.visitMaxs(2, 4);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            {
                annotationVisitor = methodVisitor.visitAnnotation("Lcom/google/common/annotations/Beta;", false);
                annotationVisitor.visitEnd();
            }
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(55, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "getSprite" : "func_187508_a", "()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(58, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", LoliLoadingPlugin.isDeobf ? "sprite" : "field_187509_d", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;");
            methodVisitor.visitInsn(ARETURN);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "getVertexData" : "func_178209_a", "()[I", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(62, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", LoliLoadingPlugin.isDeobf ? "vertexData" : "field_178215_a", "[I");
            methodVisitor.visitInsn(ARETURN);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "hasTintIndex" : "func_178212_b", "()Z", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitInsn(IRETURN);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "getTintIndex" : "func_178211_c", "()I", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitInsn(ICONST_M1);
            methodVisitor.visitInsn(IRETURN);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "getFace" : "func_178210_d", "()Lnet/minecraft/util/EnumFacing;", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitInsn(ACONST_NULL);
            methodVisitor.visitInsn(ARETURN); // Return dummy null
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, "shouldApplyDiffuseLighting", "()Z", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitInsn(IRETURN);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, "pipe", "(Lnet/minecraftforge/client/model/pipeline/IVertexConsumer;)V", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(73, l0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            // methodVisitor.visitTypeInsn(CHECKCAST, "net/minecraft/client/renderer/block/model/BakedQuad");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "net/minecraftforge/client/model/pipeline/LightUtil", "putBakedQuad", "(Lnet/minecraftforge/client/model/pipeline/IVertexConsumer;Lnet/minecraft/client/renderer/block/model/BakedQuad;)V", false);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLineNumber(74, l1);
            methodVisitor.visitInsn(RETURN);
            Label l2 = new Label();
            methodVisitor.visitLabel(l2);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l2, 0);
            methodVisitor.visitLocalVariable("consumer", "Lnet/minecraftforge/client/model/pipeline/IVertexConsumer;", null, l0, l2, 1);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = writer.visitMethod(ACC_PUBLIC, "getFormat", "()Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, null);
            methodVisitor.visitCode();
            Label l0 = new Label();
            methodVisitor.visitLabel(l0);
            methodVisitor.visitLineNumber(77, l0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "net/minecraft/client/renderer/block/model/BakedQuad", "format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;");
            methodVisitor.visitInsn(ARETURN);
            Label l1 = new Label();
            methodVisitor.visitLabel(l1);
            methodVisitor.visitLocalVariable("this", "Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, l0, l1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        writer.visitEnd();

        return writer.toByteArray();
    }
    
    private BakedQuadPatch() { }
    
}
