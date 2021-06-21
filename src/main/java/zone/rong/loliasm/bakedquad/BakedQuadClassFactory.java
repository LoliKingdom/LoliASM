package zone.rong.loliasm.bakedquad;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.LoliReflector;
import zone.rong.loliasm.core.LoliLoadingPlugin;

import java.util.Locale;

import static org.objectweb.asm.Opcodes.*;

/**
 * This is the class that handles the creation of BakedQuad classes.
 *
 * Right now there are 24 variants.
 * EnumFacings are patched in directly with no field ref, along with shouldApplyDiffuseLighting and hasTintIndex
 */
public final class BakedQuadClassFactory {

    // Called prior to transforming BakedQuadFactory
    public static void predefineBakedQuadClasses() {
        LoliLogger.instance.info("Predefining BakedQuad variants.");
        for (String face : new String[] { "Down", "Up", "North", "South", "West", "East" }) {
            for (String lighting : new String[] { "NoDiffuseLighting", "DiffuseLighting" }) {
                for (String tint : new String[] { "NoTint", "Tint" }) {

                    // cba making comprehensible names
                    final String className = "ifuckinglovelolis/bakedquads/" + face + lighting + tint;
                    final String classDescriptor = "L" + className + ";";
                    final boolean hasTint = tint.equals("Tint");

                    // TODO: Line number nodes aren't needed, removal?

                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

                    MethodVisitor methodVisitor;

                    /* public class that extends BakedQuad with no interfaces */
                    writer.visit(Opcodes.V1_8, ACC_PUBLIC | ACC_SUPER, className, null, "net/minecraft/client/renderer/block/model/BakedQuad", null);

                    /* We only insert 'byte tintIndex' field when the class denotes a tinted BakedQuad */
                    if (hasTint) {
                        writer.visitField(ACC_PRIVATE | ACC_FINAL, "tintIndex", "B", null, null).visitEnd();
                    }

                    // public ClassConstructor(int[] vertexData, @Optional int tintIndex, TextureAtlasSprite sprite, VertexFormat format) {
                    //     super(vertexData, sprite, format);
                    //     @Optional this.tintIndex = (byte) tintIndex;
                    // }
                    {
                        // Signature differentiates because there isn't a 4 high stack when tintIndex is excluded
                        String signature = hasTint ? "([IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/vertex/VertexFormat;)V" : "([ILnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/vertex/VertexFormat;)V";
                        methodVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", signature, null, null);
                        methodVisitor.visitCode();
                        Label l0 = new Label();
                        methodVisitor.visitLabel(l0);
                        methodVisitor.visitLineNumber(hasTint ? 17 : 15, l0);
                        // Load into the ctor!
                        methodVisitor.visitVarInsn(ALOAD, 0); // Load this
                        methodVisitor.visitVarInsn(ALOAD, 1); // Load int[]
                        methodVisitor.visitVarInsn(ALOAD, hasTint ? 3 : 2); // Load TextureAtlasSprite (order shifted if hasTint)
                        methodVisitor.visitVarInsn(ALOAD, hasTint ? 4 : 3); // Load VertexFormat (order shifted if hasTint)
                        // Call super(); <= this is a must need as the ClassWriter won't pass the JVM class verification
                        methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/minecraft/client/renderer/block/model/BakedQuad", "<init>", "([ILnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/vertex/VertexFormat;)V", false);
                        Label l1 = new Label();
                        methodVisitor.visitLabel(l1);
                        methodVisitor.visitLineNumber(hasTint ? 18 : 16, l1); // Line number changes when we have a tint variation
                        if (hasTint) {
                            // If it is a tint variant, grab this.tintIndex so we can PUTFIELD
                            methodVisitor.visitVarInsn(ALOAD, 0);
                            methodVisitor.visitVarInsn(ILOAD, 2);
                            methodVisitor.visitInsn(I2B); // Cast byte to int tintIndex (this is performed in the soft patch as well)
                            methodVisitor.visitFieldInsn(PUTFIELD, className, "tintIndex", "B"); // PUTFIELD to this.tintIndex
                        }
                        if (!hasTint) {
                            // Return early, if it is a tint variant, we have another label to create
                            methodVisitor.visitInsn(RETURN); // Return nothing, since this is a ctor
                        }
                        Label l2 = new Label();
                        methodVisitor.visitLabel(l2);
                        if (hasTint) {
                            // Visit number line when it is a tint variant
                            methodVisitor.visitLineNumber(19, l2);
                            methodVisitor.visitInsn(RETURN); // Finally return here
                            Label l3 = new Label(); // Extra label
                            methodVisitor.visitLabel(l3);
                            // Specify correct label and shift variable order
                            methodVisitor.visitLocalVariable("this", classDescriptor, null, l0, l3, 0);
                            methodVisitor.visitLocalVariable("vertexDataIn", "[I", null, l0, l3, 1);
                            methodVisitor.visitLocalVariable("tintIndexIn", "I", null, l0, l3, 2);
                            methodVisitor.visitLocalVariable("spriteIn", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, l0, l3, 3);
                            methodVisitor.visitLocalVariable("format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, l0, l3, 4);
                        } else {
                            methodVisitor.visitLocalVariable("this", classDescriptor, null, l0, l2, 0);
                            methodVisitor.visitLocalVariable("vertexDataIn", "[I", null, l0, l2, 1);
                            methodVisitor.visitLocalVariable("spriteIn", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, l0, l2, 2);
                            methodVisitor.visitLocalVariable("format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, l0, l2, 3);
                        }
                        methodVisitor.visitMaxs(0, 0); // COMPUTE_MAXS flag ignores any visitMaxs
                        methodVisitor.visitEnd();
                    }
                    
                    // public EnumFacing getFace() {
                    //     return EnumFacing.DOWN;
                    // }
                    {
                        methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "getFace" : "func_178210_d", "()Lnet/minecraft/util/EnumFacing;", null, null);
                        methodVisitor.visitCode();
                        Label l0 = new Label();
                        methodVisitor.visitLabel(l0);
                        methodVisitor.visitLineNumber(hasTint ? 23 : 20, l0);
                        // Grab the static enum field in EnumFacing
                        methodVisitor.visitFieldInsn(GETSTATIC, "net/minecraft/util/EnumFacing", face.toUpperCase(Locale.ENGLISH), "Lnet/minecraft/util/EnumFacing;");
                        methodVisitor.visitInsn(ARETURN); // Return the EnumFacing specified
                        Label l1 = new Label();
                        methodVisitor.visitLabel(l1);
                        methodVisitor.visitLocalVariable("this", classDescriptor, null, l0, l1, 0);
                        methodVisitor.visitMaxs(0, 0);
                        methodVisitor.visitEnd(); // COMPUTE_MAXS flag ignores any visitMaxs
                    }

                    // public boolean shouldApplyDiffuseLighting() {
                    //     return true;
                    // }
                    {
                        methodVisitor = writer.visitMethod(ACC_PUBLIC, "shouldApplyDiffuseLighting", "()Z", null, null);
                        methodVisitor.visitCode();
                        Label l0 = new Label();
                        methodVisitor.visitLabel(l0);
                        methodVisitor.visitLineNumber(hasTint ? 28 : 25, l0);
                        methodVisitor.visitInsn(lighting.equals("DiffuseLighting") ? ICONST_1 : ICONST_0); // ICONST_1 = true; ICONST_0 = false;
                        methodVisitor.visitInsn(IRETURN); // IRETURN covers boolean returns too!
                        Label l1 = new Label();
                        methodVisitor.visitLabel(l1);
                        methodVisitor.visitLocalVariable("this", classDescriptor, null, l0, l1, 0);
                        methodVisitor.visitMaxs(0, 0); // COMPUTE_MAXS flag ignores any visitMaxs
                        methodVisitor.visitEnd();
                    }

                    // public TextureAtlasSprite getSprite() {
                    //     return sprite;
                    // }
                    {
                        methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "getSprite" : "func_187508_a", "()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", null, null);
                        methodVisitor.visitCode();
                        Label l0 = new Label();
                        methodVisitor.visitLabel(l0);
                        methodVisitor.visitLineNumber(hasTint ? 33 : 30, l0);
                        methodVisitor.visitVarInsn(ALOAD, 0); // Load this, so GETFIELD has the context to grab the variable
                        // Still the GETFIELD instruction specifies the current class even though the variable resides in the superclass
                        methodVisitor.visitFieldInsn(GETFIELD, className, LoliLoadingPlugin.isDeobf ? "sprite" : "field_187509_d", "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;");
                        methodVisitor.visitInsn(ARETURN); // Return a reference
                        Label l1 = new Label();
                        methodVisitor.visitLabel(l1);
                        methodVisitor.visitLocalVariable("this", classDescriptor, null, l0, l1, 0);
                        methodVisitor.visitMaxs(0, 0); // COMPUTE_MAXS flag ignores any visitMaxs
                        methodVisitor.visitEnd();
                    }

                    // public int[] getVertexData() {
                    //     return vertexData;
                    // }
                    {
                        methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "getVertexData" : "func_178209_a", "()[I", null, null);
                        methodVisitor.visitCode();
                        Label l0 = new Label();
                        methodVisitor.visitLabel(l0);
                        methodVisitor.visitLineNumber(hasTint ? 38 : 35, l0);
                        methodVisitor.visitVarInsn(ALOAD, 0); // Load this, so GETFIELD has the context to grab the variable
                        // Still the GETFIELD instruction specifies the current class even though the variable resides in the superclass
                        methodVisitor.visitFieldInsn(GETFIELD, className, LoliLoadingPlugin.isDeobf ? "vertexData" : "field_178215_a", "[I");
                        methodVisitor.visitInsn(ARETURN); // Return a reference
                        Label l1 = new Label();
                        methodVisitor.visitLabel(l1);
                        methodVisitor.visitLocalVariable("this", classDescriptor, null, l0, l1, 0);
                        methodVisitor.visitMaxs(0, 0); // COMPUTE_MAXS flag ignores any visitMaxs
                        methodVisitor.visitEnd();
                    }

                    // public boolean hasTintIndex() {
                    //     return true;
                    // }

                    // public boolean hasTintIndex() {
                    //     return this.tintIndex != -1;
                    // }
                    {
                        methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "hasTintIndex" : "func_178212_b", "()Z", null, null);
                        methodVisitor.visitCode();
                        Label l0 = new Label();
                        methodVisitor.visitLabel(l0);
                        methodVisitor.visitLineNumber(hasTint ? 43 : 40, l0);
                        methodVisitor.visitInsn(hasTint ? ICONST_1 : ICONST_0);
                        methodVisitor.visitInsn(IRETURN); // Return true if hasTint, false if !hasTint
                        Label l1 = new Label();
                        methodVisitor.visitLabel(l1);
                        methodVisitor.visitLocalVariable("this", classDescriptor, null, l0, l1, 0);
                        methodVisitor.visitMaxs(0, 0); // COMPUTE_MAXS flag ignores any visitMaxs
                        methodVisitor.visitEnd();
                    }

                    // public int getTintIndex() {
                    //     return this.tintIndex;
                    // }
                    {
                        methodVisitor = writer.visitMethod(ACC_PUBLIC, LoliLoadingPlugin.isDeobf ? "getTintIndex" : "func_178211_c", "()I", null, null);
                        methodVisitor.visitCode();
                        Label l0 = new Label();
                        methodVisitor.visitLabel(l0);
                        methodVisitor.visitLineNumber(hasTint ? 48 : 45, l0);
                        if (hasTint) {
                            // When it is the tint variant, load this.tintIndex and return it
                            methodVisitor.visitVarInsn(ALOAD, 0);
                            methodVisitor.visitFieldInsn(GETFIELD, className, "tintIndex", "B");
                        } else {
                            methodVisitor.visitInsn(ICONST_M1); // Otherwise load default constant -1
                        }
                        methodVisitor.visitInsn(IRETURN); // Return the tintIndex (default -1 which means no tint)
                        Label l1 = new Label();
                        methodVisitor.visitLabel(l1);
                        methodVisitor.visitLocalVariable("this", classDescriptor, null, l0, l1, 0);
                        methodVisitor.visitMaxs(0, 0); // COMPUTE_MAXS flag ignores any visitMaxs
                        methodVisitor.visitEnd();
                    }

                    // public VertexFormat getFormat() {
                    //     return this.format;
                    // }
                    {
                        methodVisitor = writer.visitMethod(ACC_PUBLIC, "getFormat", "()Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, null);
                        methodVisitor.visitCode();
                        Label l0 = new Label();
                        methodVisitor.visitLabel(l0);
                        methodVisitor.visitLineNumber(50, l0);
                        methodVisitor.visitVarInsn(ALOAD, 0); // Load this, so GETFIELD has the context to grab the variable
                        // Still the GETFIELD instruction specifies the current class even though the variable resides in the superclass
                        methodVisitor.visitFieldInsn(GETFIELD, className, "format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;");
                        methodVisitor.visitInsn(ARETURN); // Return a reference
                        Label l1 = new Label();
                        methodVisitor.visitLabel(l1);
                        methodVisitor.visitLocalVariable("this", classDescriptor, null, l0, l1, 0);
                        methodVisitor.visitMaxs(0, 0); // COMPUTE_MAXS flag ignores any visitMaxs
                        methodVisitor.visitEnd();
                    }
                    writer.visitEnd(); // Tell the Writer the class description ends here

                    try {
                        byte[] classBytes = writer.toByteArray();
                        /*
                        if (face.equals("South") && lighting.equals("DiffuseLighting") && tint.equals("NoTint")) {
                            try (FileOutputStream stream = new FileOutputStream(new File(Launch.minecraftHome, "LolNoTint.class"))) {
                                stream.write(classBytes);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                         */
                        // Trivial story below:
                        //
                        // Annoying. ASM library doesn't exist in LaunchClassLoader, since its loaded in by sun.misc.AppClassLoader
                        // ClassWriter calls getClass().getClassLoader().loadClass() which gets the AppClassLoader instead...
                        // Which means classes have to load into both LaunchClassLoader and AppClassLoader.
                        // Fortunately, no references should be kept so AppClassLoader should discard it some GC runs later.
                        //
                        // Solution found:
                        // No need to COMPUTE_FRAMES!

                        Class clazz = LoliReflector.defineClass(Launch.classLoader, className.replace('/', '.'), classBytes);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    }
}
