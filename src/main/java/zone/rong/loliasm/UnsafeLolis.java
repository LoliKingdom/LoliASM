package zone.rong.loliasm;

import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeLolis {

    private static final Unsafe $ = UnsafeAccess.UNSAFE;

    public static void removeFMLSecurityManager() {
        LoliLogger.instance.warn("Detaching FMLSecurityManager.");
        Field out = LoliReflector.getField(System.class, "out");
        Field err = LoliReflector.getField(System.class, "err");
        long errOffset = $.staticFieldOffset(err);
        long offset = errOffset + (errOffset - $.staticFieldOffset(out));
        $.putObjectVolatile($.staticFieldBase(err), offset, null);
        if (System.getSecurityManager() != null) {
            LoliLogger.instance.warn("Failed to detach FMLSecurityManager.");
        }
    }

    private UnsafeLolis() { }

}
