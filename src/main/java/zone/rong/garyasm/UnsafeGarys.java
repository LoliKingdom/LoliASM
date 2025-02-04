package zone.rong.garyasm;

import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeGarys {

    private static final Unsafe $ = UnsafeAccess.UNSAFE;

    public static void removeFMLSecurityManager() {
        GaryLogger.instance.warn("Detaching FMLSecurityManager.");
        Field out = GaryReflector.getField(System.class, "out");
        Field err = GaryReflector.getField(System.class, "err");
        long errOffset = $.staticFieldOffset(err);
        long offset = errOffset + (errOffset - $.staticFieldOffset(out));
        $.putObjectVolatile($.staticFieldBase(err), offset, null);
        if (System.getSecurityManager() != null) {
            GaryLogger.instance.warn("Failed to detach FMLSecurityManager.");
        }
    }

    private UnsafeGarys() { }

}
