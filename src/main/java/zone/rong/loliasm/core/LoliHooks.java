package zone.rong.loliasm.core;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import zone.rong.loliasm.api.StringPool;

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
