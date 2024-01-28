package zone.rong.blahajasm;

import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeBlahaj {

    private static final Unsafe $ = UnsafeAccess.UNSAFE;

    public static void removeFMLSecurityManager() {
        BlahajLogger.instance.warn("Detaching FMLSecurityManager.");
        Field out = BlahajReflector.getField(System.class, "out");
        Field err = BlahajReflector.getField(System.class, "err");
        long errOffset = $.staticFieldOffset(err);
        long offset = errOffset + (errOffset - $.staticFieldOffset(out));
        $.putObjectVolatile($.staticFieldBase(err), offset, null);
        if (System.getSecurityManager() != null) {
            BlahajLogger.instance.warn("Failed to detach FMLSecurityManager.");
        }
    }

    private UnsafeBlahaj() { }

}
