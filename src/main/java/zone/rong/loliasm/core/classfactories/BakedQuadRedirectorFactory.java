package zone.rong.loliasm.core.classfactories;

import org.objectweb.asm.*;
import zone.rong.loliasm.LoliConfig;
import zone.rong.loliasm.LoliReflector;
import zone.rong.loliasm.bakedquad.BakedQuadClassFactory;

import static org.objectweb.asm.Opcodes.*;

public class BakedQuadRedirectorFactory {

    public static String generateRedirectorClass() {

        BakedQuadClassFactory.predefineBakedQuadClasses();

        final String className = "ifuckinglovelolis.mixins.NewBakedQuadCallsRedirector";
        ClassWriter writer = new ClassWriter(0);
        String[] targetNames = LoliConfig.getConfig().bakedQuadPatchClasses;

        writer.visit(V1_8, ACC_PUBLIC | ACC_SUPER, className.replace('.', '/'), null, "java/lang/Object", null);

        AnnotationVisitor mixinVisitor = writer.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
        AnnotationVisitor valueVisitor = mixinVisitor.visitArray("value");
        for (String target : targetNames) {
            String descriptor = ("L" + target + ";").replace('.', '/');
            valueVisitor.visit(null, Type.getType(descriptor));
        }
        valueVisitor.visitEnd();
        mixinVisitor.visitEnd();

        MethodVisitor methodVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        Label l0 = new Label();
        methodVisitor.visitLabel(l0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        methodVisitor.visitInsn(RETURN);
        Label l1 = new Label();
        methodVisitor.visitLabel(l1);
        methodVisitor.visitLocalVariable("this", "L" + className.replace('.', '/') + ";", null, l0, l1, 0);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();

        methodVisitor = writer.visitMethod(ACC_PRIVATE, "redirect", "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZLnet/minecraft/client/renderer/vertex/VertexFormat;)Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, null);

        AnnotationVisitor redirectVisitor = methodVisitor.visitAnnotation("Lorg/spongepowered/asm/mixin/injection/Redirect;", true);

        valueVisitor = redirectVisitor.visitArray("method");
        valueVisitor.visit(null, "*");
        valueVisitor.visitEnd();

        valueVisitor = redirectVisitor.visitAnnotation("at", "Lorg/spongepowered/asm/mixin/injection/At;");
        valueVisitor.visit("value", "NEW");
        valueVisitor.visit("target", "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZLnet/minecraft/client/renderer/vertex/VertexFormat;)Lnet/minecraft/client/renderer/block/model/BakedQuad;");
        valueVisitor.visitEnd();

        methodVisitor.visitCode();

        l0 = new Label();
        methodVisitor.visitLabel(l0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ILOAD, 2);
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitVarInsn(ALOAD, 6);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "zone/rong/loliasm/bakedquad/BakedQuadFactory", "create", "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZLnet/minecraft/client/renderer/vertex/VertexFormat;)Lnet/minecraft/client/renderer/block/model/BakedQuad;", false);
        methodVisitor.visitInsn(ARETURN);

        l1 = new Label();
        methodVisitor.visitLabel(l1);
        methodVisitor.visitLocalVariable("this", "L" + className.replace('.', '/') + ";", null, l0, l1, 0);
        methodVisitor.visitLocalVariable("vertexDataIn", "[I", null, l0, l1, 1);
        methodVisitor.visitLocalVariable("tintIndexIn", "I", null, l0, l1, 2);
        methodVisitor.visitLocalVariable("faceIn", "Lnet/minecraft/util/EnumFacing;", null, l0, l1, 3);
        methodVisitor.visitLocalVariable("spriteIn", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, l0, l1, 4);
        methodVisitor.visitLocalVariable("applyDiffuseLighting", "Z", null, l0, l1, 5);
        methodVisitor.visitLocalVariable("format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, l0, l1, 6);
        methodVisitor.visitMaxs(6, 7);
        methodVisitor.visitEnd();

        methodVisitor = writer.visitMethod(ACC_PRIVATE, "redirectDeprecated", "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Z)Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, null);

        redirectVisitor = methodVisitor.visitAnnotation("Lorg/spongepowered/asm/mixin/injection/Redirect;", true);

        valueVisitor = redirectVisitor.visitArray("method");
        valueVisitor.visit(null, "*");
        valueVisitor.visitEnd();

        valueVisitor = redirectVisitor.visitAnnotation("at", "Lorg/spongepowered/asm/mixin/injection/At;");
        valueVisitor.visit("value", "NEW");
        valueVisitor.visit("target", "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Z)Lnet/minecraft/client/renderer/block/model/BakedQuad;");
        valueVisitor.visitEnd();

        methodVisitor.visitCode();

        l0 = new Label();
        methodVisitor.visitLabel(l0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ILOAD, 2);
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitVarInsn(ALOAD, 4);
        methodVisitor.visitVarInsn(ILOAD, 5);
        methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/client/renderer/vertex/DefaultVertexFormats", "ITEM", "Lnet/minecraft/client/renderer/vertex/VertexFormat;");
        methodVisitor.visitMethodInsn(INVOKESTATIC, "zone/rong/loliasm/bakedquad/BakedQuadFactory", "create", "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZLnet/minecraft/client/renderer/vertex/VertexFormat;)Lnet/minecraft/client/renderer/block/model/BakedQuad;", false);
        methodVisitor.visitInsn(ARETURN);
        l1 = new Label();
        methodVisitor.visitLabel(l1);
        methodVisitor.visitLocalVariable("this", "L" + className.replace('.', '/') + ";", null, l0, l1, 0);
        methodVisitor.visitLocalVariable("vertexDataIn", "[I", null, l0, l1, 1);
        methodVisitor.visitLocalVariable("tintIndexIn", "I", null, l0, l1, 2);
        methodVisitor.visitLocalVariable("faceIn", "Lnet/minecraft/util/EnumFacing;", null, l0, l1, 3);
        methodVisitor.visitLocalVariable("spriteIn", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, l0, l1, 4);
        methodVisitor.visitLocalVariable("applyDiffuseLighting", "Z", null, l0, l1, 5);
        methodVisitor.visitMaxs(6, 6);
        methodVisitor.visitEnd();

        writer.visitEnd();

        LoliReflector.defineMixinClass(className, writer.toByteArray());

        return className.substring(className.lastIndexOf('.') + 1);
    }

    private BakedQuadRedirectorFactory() { }

}
