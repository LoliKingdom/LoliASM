package zone.rong.loliasm;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Helper class for Reflection nonsense.
 */
public class LoliReflector {

    public static final MethodHandle classLoader$DefineClass;

    static {
        MethodHandle defineClassHandle = null;
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
            defineClassHandle = MethodHandles.lookup().unreflect(defineClass);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        classLoader$DefineClass = defineClassHandle;
    }

}
