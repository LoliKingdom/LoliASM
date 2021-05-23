package zone.rong.loliasm.api;

import java.util.Locale;

@SuppressWarnings("unused")
public class StringPool {

    // private static final ObjectOpenHashSet<String> POOL = new ObjectOpenHashSet<>(4096);

    public static String canonicalize(String string) {
        /*
        synchronized (POOL) {
            return POOL.addOrGet(string);
        }
         */
        return string.length() == 0 ? "" : string.intern();
    }

    public static String lowerCaseAndCanonize(String string) {
        /*
        synchronized (POOL) {
            return POOL.addOrGet(string.toLowerCase(Locale.ROOT));
        }
         */
        return string.length() == 0 ? "" : string.toLowerCase(Locale.ROOT).intern();
    }

}
