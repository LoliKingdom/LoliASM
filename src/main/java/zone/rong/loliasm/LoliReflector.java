package zone.rong.loliasm;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Helper class for Reflection nonsense.
 */
public class LoliReflector {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public static MethodHandle resolveCtor(Class<?> clazz, Class<?>... args) {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor(args);
            if (!ctor.isAccessible()) {
                ctor.setAccessible(true);
            }
            return lookup.unreflectConstructor(ctor);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle resolveMethod(Class<?> clazz, String methodName, Class<?>... args) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, args);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return lookup.unreflect(method);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle resolveMethod(Class<?> clazz, String methodName, String obfMethodName, Class<?>... args) {
        try {
            return lookup.unreflect(ReflectionHelper.findMethod(clazz, methodName, obfMethodName, args));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle resolveFieldGetter(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return MethodHandles.lookup().unreflectGetter(field);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle resolveFieldSetter(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle resolveFieldGetter(Class<?> clazz, String fieldName, String obfFieldName) {
        try {
            return MethodHandles.lookup().unreflectGetter(ReflectionHelper.findField(clazz, fieldName, obfFieldName));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle resolveFieldSetter(Class<?> clazz, String fieldName, String obfFieldName) {
        try {
            return MethodHandles.lookup().unreflectSetter(ReflectionHelper.findField(clazz, fieldName, obfFieldName));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

}
