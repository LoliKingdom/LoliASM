package zone.rong.blahajasm.common.crashes;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.MinecraftDummyContainer;
import net.minecraftforge.fml.common.ModContainer;
import zone.rong.blahajasm.BlahajLogger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

// TODO: grab packages from ASMDataTable?
public class ModIdentifier {

    public static Set<ModContainer> identifyFromStacktrace(Throwable e) {
        Map<File, Set<ModContainer>> modMap = makeModMap();
        HashSet<String> classes = new LinkedHashSet<>();
        while (e != null) {
            for (StackTraceElement element : e.getStackTrace()) {
                classes.add(element.getClassName());
            }
            e = e.getCause();
        }
        Set<ModContainer> mods = new LinkedHashSet<>();
        for (String className : classes) {
            Set<ModContainer> classMods = identifyFromClass(className, modMap);
            if (classMods != null) {
                mods.addAll(classMods);
            }
        }
        return mods;
    }

    public static Set<ModContainer> identifyFromClass(String className) {
        return identifyFromClass(className, makeModMap());
    }

    private static Set<ModContainer> identifyFromClass(String className, Map<File, Set<ModContainer>> modMap) {
        // Skip identification for Mixin, one's mod copy of the library is shared with all other mods
        if (className.startsWith("org.spongepowered.asm.mixin.") || className.startsWith("sun.") || className.startsWith("java.")) {
            return Collections.emptySet();
        }
        // Get the URL of the class
        final String untrasformedName = untransformName(Launch.classLoader, className);
        URL url = Launch.classLoader.getResource(untrasformedName.replace('.', '/') + ".class");
        BlahajLogger.instance.debug(className + " = " + untrasformedName + " = " + url);
        if (url == null) {
            BlahajLogger.instance.warn("Failed to identify " + className + " (untransformed name: " + untrasformedName + ")");
            return Collections.emptySet();
        }
        // Get the mod containing that class
        try {
            if (url.getProtocol().equals("jar")) {
                url = new URL(url.getFile().substring(0, url.getFile().indexOf('!')));
            }
            return modMap.get(new File(url.toURI()).getCanonicalFile());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<File, Set<ModContainer>> makeModMap() {
        Map<File, Set<ModContainer>> modMap = new Object2ObjectOpenHashMap<>();
        for (ModContainer mod : Loader.instance().getModList()) {
            if (mod instanceof MinecraftDummyContainer || mod instanceof FMLContainer) {
                continue;
            }
            try {
                modMap.computeIfAbsent(mod.getSource().getCanonicalFile(), k -> new ObjectArraySet<>()).add(mod);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return modMap;
    }

    private static String untransformName(LaunchClassLoader launchClassLoader, String className) {
        try {
            Method untransformNameMethod = LaunchClassLoader.class.getDeclaredMethod("untransformName", String.class);
            untransformNameMethod.setAccessible(true);
            return (String) untransformNameMethod.invoke(launchClassLoader, className);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
