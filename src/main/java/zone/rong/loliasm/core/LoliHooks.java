package zone.rong.loliasm.core;

import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.api.StringPool;
import zone.rong.loliasm.bakedquad.SupportingBakedQuad;
import zone.rong.loliasm.config.LoliConfig;

import java.util.Set;

@SuppressWarnings("unused")
public class LoliHooks {

    /*
    private static final MethodHandle STRING_BACKING_CHAR_ARRAY_GETTER = LoliReflector.resolveFieldGetter(String.class, "value");
    private static final char[] EMPTY_CHAR_ARRAY;

    static {
        char[] array;
        try {
            array = (char[]) STRING_BACKING_CHAR_ARRAY_GETTER.invokeExact("");
        } catch (Throwable throwable) {
            array = new char[0];
        }
        EMPTY_CHAR_ARRAY = array;
    }
     */

    public static <K> ObjectArraySet<K> createArraySet() {
        return new ObjectArraySet<>();
    }

    public static <K> ObjectOpenHashSet<K> createHashSet() {
        return new ObjectOpenHashSet<>();
    }

    public static <K, V> Object2ObjectArrayMap<K, V> createArrayMap() {
        return new Object2ObjectArrayMap<>();
    }

    public static <K, V> Object2ObjectOpenHashMap<K, V> createHashMap() {
        return new Object2ObjectOpenHashMap<>();
    }

    private static Set<Class<?>> classesThatCallBakedQuadCtor;
    private static Set<Class<?>> classesThatExtendBakedQuad;

    public static void inform(Class<?> clazz) {
        if (classesThatCallBakedQuadCtor == null) {
            classesThatCallBakedQuadCtor = new ReferenceOpenHashSet<>();
        }
        if (classesThatCallBakedQuadCtor.add(clazz)) {
            LoliConfig.instance.editClassesThatCallBakedQuadCtor(clazz);
        }
        if (clazz != SupportingBakedQuad.class && BakedQuad.class.isAssignableFrom(clazz)) {
            if (classesThatExtendBakedQuad == null) {
                classesThatExtendBakedQuad = new ReferenceOpenHashSet<>();
            }
            if (classesThatExtendBakedQuad.add(clazz)) {
                LoliConfig.instance.editClassesThatExtendBakedQuad(clazz);
            }
        }
    }

    public static void modCandidate$override$addClassEntry(ModCandidate modCandidate, String name, Set<String> foundClasses, Set<String> packages, ASMDataTable table) {
        String className = name.substring(0, name.lastIndexOf('.'));
        foundClasses.add(className);
        className = className.replace('/','.');
        int pkgIdx = className.lastIndexOf('.');
        if (pkgIdx > -1) {
            String pkg = StringPool.canonicalize(className.substring(0, pkgIdx));
            packages.add(pkg);
            table.registerPackage(modCandidate, pkg);
        }
    }

    public static /*char[]*/ String nbtTagString$override$ctor(String data) {
        /*
        if (data == null) {
            throw new NullPointerException("Null string not allowed");
        }
         */
        return StringPool.canonicalize(data);
        /*
        try {
            return (char[]) STRING_BACKING_CHAR_ARRAY_GETTER.invokeExact(data);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return EMPTY_CHAR_ARRAY;
        }
         */
    }

}
