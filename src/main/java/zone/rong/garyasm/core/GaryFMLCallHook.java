package zone.rong.garyasm.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import zone.rong.garyasm.config.GaryConfig;
import zone.rong.garyasm.GaryReflector;
import zone.rong.garyasm.api.datastructures.DummyMap;
import zone.rong.garyasm.api.datastructures.canonical.AutoCanonizingSet;
import zone.rong.garyasm.api.datastructures.deobf.DeobfuscatedMappingsMap;
import zone.rong.garyasm.api.datastructures.deobf.FieldDescriptionsMap;
import zone.rong.garyasm.api.GaryStringPool;

import java.util.Map;
import java.util.Set;

public class GaryFMLCallHook implements IFMLCallHook {

    @Override
    @SuppressWarnings("unchecked")
    public Void call() {
        try {
            DummyMap.of();
            if (GaryConfig.instance.disablePackageManifestMap) {
                GaryReflector.resolveFieldSetter(LaunchClassLoader.class, "packageManifests").invokeExact(Launch.classLoader, DummyMap.of());
                GaryReflector.resolveFieldSetter(LaunchClassLoader.class, "EMPTY").invoke(null);
            }
            if (GaryConfig.instance.optimizeFMLRemapper) {
                GaryReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "classNameBiMap").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, canonicalizeClassNames((BiMap<String, String>) GaryReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "classNameBiMap").invokeExact(FMLDeobfuscatingRemapper.INSTANCE)));
                if (!GaryLoadingPlugin.isDeobf) {
                    GaryReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "rawFieldMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, DeobfuscatedMappingsMap.of((Map<String, Map<String, String>>) GaryReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "rawFieldMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE), true));
                    GaryReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "rawMethodMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, DeobfuscatedMappingsMap.of((Map<String, Map<String, String>>) GaryReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "rawMethodMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE), false));
                    GaryReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "fieldNameMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, DeobfuscatedMappingsMap.of((Map<String, Map<String, String>>) GaryReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "fieldNameMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE), true));
                    GaryReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "methodNameMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, DeobfuscatedMappingsMap.of((Map<String, Map<String, String>>) GaryReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "methodNameMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE), false));
                    GaryReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "fieldDescriptions").invoke(FMLDeobfuscatingRemapper.INSTANCE, new FieldDescriptionsMap((Map<String, Map<String, String>>) GaryReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "fieldDescriptions").invokeExact(FMLDeobfuscatingRemapper.INSTANCE)));
                    GaryReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "negativeCacheMethods").invoke(FMLDeobfuscatingRemapper.INSTANCE, new AutoCanonizingSet<>((Set<String>) GaryReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "negativeCacheMethods").invokeExact(FMLDeobfuscatingRemapper.INSTANCE)));
                    GaryReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "negativeCacheFields").invoke(FMLDeobfuscatingRemapper.INSTANCE, new AutoCanonizingSet<>((Set<String>) GaryReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "negativeCacheFields").invokeExact(FMLDeobfuscatingRemapper.INSTANCE)));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    private BiMap<String, String> canonicalizeClassNames(BiMap<String, String> map) {
        ImmutableBiMap.Builder<String, String> builder = ImmutableBiMap.builder();
        map.forEach((s1, s2) -> builder.put(GaryStringPool.canonicalize(s1), GaryStringPool.canonicalize(s2)));
        return builder.build();
    }
}
