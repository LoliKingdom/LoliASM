package zone.rong.loliasm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.objectweb.asm.Type;
import zone.rong.loliasm.api.datastructures.fastmap.StateAndIndex;
import zone.rong.loliasm.api.datastructures.fastmap.StateAndKey;
import zone.rong.loliasm.api.datastructures.fastmap.FastMapStateHolder;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;

/**
 * Helper class for Reflection nonsense.
 */
public class LoliReflector {

    private static boolean isOpenJ9 = System.getProperty("java.vm.name").toLowerCase(Locale.ROOT).contains("openj9");

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    private static final MethodHandle classLoader$DefineClass = resolveMethod(ClassLoader.class, "defineClass", String.class, byte[].class, int.class, int.class);

    static {
        ClassLoader classLoader = ImmutableMap.class.getClassLoader();
        // defineClass(classLoader, LoliEntrySet.class);
        // defineClass(classLoader, LoliImmutableMap.class);
        // defineClass(classLoader, LoliIterator.class);
        defineClass(classLoader, StateAndIndex.class);
        defineClass(classLoader, StateAndKey.class);
        defineClass(classLoader, FastMapStateHolder.class);
    }

    public static <CL extends ClassLoader> Class defineClass(CL classLoader, Class clazz) {
        String name = Type.getInternalName(clazz);
        InputStream byteStream = clazz.getResourceAsStream('/' + name + ".class");
        try {
            byte[] classBytes = new byte[byteStream.available()];
            final int bytesRead = byteStream.read(classBytes);
            Preconditions.checkState(bytesRead == classBytes.length);
            // return defineClass(classLoader, name.replace('/', '.'), classBytes);
            return (Class) classLoader$DefineClass.invoke(classLoader, name.replace('/', '.'), classBytes, 0, classBytes.length);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return clazz;
    }

    public static <CL extends ClassLoader> Class defineClass(CL classLoader, String name, byte[] classBytes) {
        try {
            return (Class) classLoader$DefineClass.invokeExact(classLoader, name, classBytes, 0, classBytes.length);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

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
            if (isOpenJ9) {
                fixOpenJ9PrivateStaticFinalRestraint(field);
            }
            return lookup.unreflectGetter(field);
        } catch (Throwable e) {
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
            if (isOpenJ9) {
                fixOpenJ9PrivateStaticFinalRestraint(field);
            }
            return lookup.unreflectSetter(field);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle resolveFieldGetter(Class<?> clazz, String fieldName, String obfFieldName) {
        try {
            return lookup.unreflectGetter(ReflectionHelper.findField(clazz, fieldName, obfFieldName));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle resolveFieldSetter(Class<?> clazz, String fieldName, String obfFieldName) {
        try {
            return lookup.unreflectSetter(ReflectionHelper.findField(clazz, fieldName, obfFieldName));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void fixOpenJ9PrivateStaticFinalRestraint(Field field) throws Throwable {
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        lookup.unreflectSetter(modifiers).invokeExact(field, field.getModifiers() & ~Modifier.FINAL);
    }
}
