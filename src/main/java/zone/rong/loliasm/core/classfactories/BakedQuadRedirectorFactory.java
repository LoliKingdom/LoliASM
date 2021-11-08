package zone.rong.loliasm.core.classfactories;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.*;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.LoliReflector;
import zone.rong.loliasm.bakedquad.BakedQuadClassFactory;
import zone.rong.loliasm.core.LoliLoadingPlugin;
import zone.rong.loliasm.core.LoliTransformer;

import static org.objectweb.asm.Opcodes.*;

public class BakedQuadRedirectorFactory {

    public static String generateRedirectorClass() {

        ClassWriter writer = new ClassWriter(0);
        final String className = "zone.rong.loliasm.client.models.bakedquad.mixins.NewBakedQuadCallsRedirector";

        writer.visit(V1_8, ACC_PUBLIC | ACC_SUPER, className.replace('.', '/'), null, "java/lang/Object", null);
        AnnotationVisitor mixinVisitor = writer.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
        AnnotationVisitor valueVisitor = mixinVisitor.visitArray("value");

        if (!LoliTransformer.squashBakedQuads) {
            LoliLogger.instance.info("Defining a mock NewBakedQuadCallsRedirector Mixin.");

            valueVisitor.visit(null, Type.getType("Lnet/minecraft/client/renderer/block/model/FaceBakery;"));
            valueVisitor.visitEnd();
            mixinVisitor.visitEnd();
        } else {
            BakedQuadClassFactory.predefineBakedQuadClasses();
            String[] targetNames = LoliConfig.instance.classesThatCallBakedQuadCtor;

            LoliLogger.instance.info("Defining NewBakedQuadCallsRedirector Mixin. With these mixin targets: {}", (Object) targetNames);

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

            for (String methodName : new String[] { "redirect", "staticRedirect", "deprecatedRedirect", "staticDeprecatedRedirect"} ) {
                boolean isStatic = methodName.startsWith("static");
                boolean isDeprecated = StringUtils.containsIgnoreCase(methodName, "deprecated");

                methodVisitor = writer.visitMethod(ACC_PRIVATE | (isStatic ? ACC_STATIC : 0), methodName, "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;" + (isDeprecated ? "" : "ZLnet/minecraft/client/renderer/vertex/VertexFormat;") + ")Lnet/minecraft/client/renderer/block/model/BakedQuad;", null, null);

                AnnotationVisitor redirectVisitor = methodVisitor.visitAnnotation("Lorg/spongepowered/asm/mixin/injection/Redirect;", true);

                valueVisitor = redirectVisitor.visitArray("method");
                valueVisitor.visit(null, "*");
                valueVisitor.visitEnd();

                valueVisitor = redirectVisitor.visitAnnotation("at", "Lorg/spongepowered/asm/mixin/injection/At;");
                valueVisitor.visit("value", "NEW");
                valueVisitor.visit("target", "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;" + (isDeprecated ? "" : "ZLnet/minecraft/client/renderer/vertex/VertexFormat;") + ")Lnet/minecraft/client/renderer/block/model/BakedQuad;");
                valueVisitor.visitEnd();

                methodVisitor.visitCode();

                l0 = new Label();
                methodVisitor.visitLabel(l0);
                methodVisitor.visitVarInsn(ALOAD, isStatic ? 0 : 1);
                methodVisitor.visitVarInsn(ILOAD, isStatic ? 1 : 2);
                methodVisitor.visitVarInsn(ALOAD, isStatic ? 2 : 3);
                methodVisitor.visitVarInsn(ALOAD, isStatic ? 3 : 4);
                if (isDeprecated) {
                    methodVisitor.visitInsn(ICONST_1);
                    methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/client/renderer/vertex/DefaultVertexFormats", LoliLoadingPlugin.isDeobf ? "ITEM" : "field_176599_b", "Lnet/minecraft/client/renderer/vertex/VertexFormat;");
                } else {
                    methodVisitor.visitVarInsn(ILOAD, isStatic ? 4 : 5);
                    methodVisitor.visitVarInsn(ALOAD, isStatic ? 5 : 6);
                }
                methodVisitor.visitMethodInsn(INVOKESTATIC, "zone/rong/loliasm/bakedquad/BakedQuadFactory", LoliConfig.instance.vertexDataCanonicalization ? "canonicalize" : "create", "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZLnet/minecraft/client/renderer/vertex/VertexFormat;)Lnet/minecraft/client/renderer/block/model/BakedQuad;", false);
                methodVisitor.visitInsn(ARETURN);

                l1 = new Label();
                methodVisitor.visitLabel(l1);
                if (!isStatic) {
                    methodVisitor.visitLocalVariable("this", "L" + className.replace('.', '/') + ";", null, l0, l1, 0);
                }
                methodVisitor.visitLocalVariable("vertexDataIn", "[I", null, l0, l1, isStatic ? 0 : 1);
                methodVisitor.visitLocalVariable("tintIndexIn", "I", null, l0, l1, isStatic ? 1 : 2);
                methodVisitor.visitLocalVariable("faceIn", "Lnet/minecraft/util/EnumFacing;", null, l0, l1, isStatic ? 2 : 3);
                methodVisitor.visitLocalVariable("spriteIn", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, l0, l1, isStatic ? 3 : 4);
                if (!isDeprecated) {
                    methodVisitor.visitLocalVariable("applyDiffuseLighting", "Z", null, l0, l1, isStatic ? 4 : 5);
                    methodVisitor.visitLocalVariable("format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, l0, l1, isStatic ? 5 : 6);
                    methodVisitor.visitMaxs(isStatic ? 5 : 6, isStatic ? 6 : 7);
                } else {
                    methodVisitor.visitMaxs(isStatic ? 4 : 5, isStatic ? 4 : 5);
                }
                methodVisitor.visitEnd();
            }
        }

        writer.visitEnd();

        LoliReflector.defineMixinClass(className, writer.toByteArray());
        return className.substring(className.lastIndexOf('.') + 1);
    }

    private BakedQuadRedirectorFactory() { }

}
