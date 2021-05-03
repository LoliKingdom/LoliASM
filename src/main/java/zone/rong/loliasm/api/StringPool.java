package zone.rong.loliasm.api;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Locale;

@SuppressWarnings("unused")
public class StringPool {

    private static final ObjectOpenHashSet<String> POOL = new ObjectOpenHashSet<>(4096);

    public static String canonize(String string) {
        synchronized (POOL) {
            return POOL.addOrGet(string);
        }
    }

    public static String lowerCaseAndCanonize(String string) {
        synchronized (POOL) {
            return POOL.addOrGet(string.toLowerCase(Locale.ROOT));
        }
    }

}
