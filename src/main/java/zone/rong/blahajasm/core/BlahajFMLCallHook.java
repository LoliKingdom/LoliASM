package zone.rong.blahajasm.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import zone.rong.blahajasm.config.BlahajConfig;
import zone.rong.blahajasm.BlahajReflector;
import zone.rong.blahajasm.api.datastructures.DummyMap;
import zone.rong.blahajasm.api.datastructures.canonical.AutoCanonizingSet;
import zone.rong.blahajasm.api.datastructures.deobf.DeobfuscatedMappingsMap;
import zone.rong.blahajasm.api.datastructures.deobf.FieldDescriptionsMap;
import zone.rong.blahajasm.api.LoliStringPool;

import java.util.Map;
import java.util.Set;

public class BlahajFMLCallHook implements IFMLCallHook {

    @Override
    @SuppressWarnings("unchecked")
    public Void call() {
        try {
            DummyMap.of();
            if (BlahajConfig.instance.disablePackageManifestMap) {
                BlahajReflector.resolveFieldSetter(LaunchClassLoader.class, "packageManifests").invokeExact(Launch.classLoader, DummyMap.of());
                BlahajReflector.resolveFieldSetter(LaunchClassLoader.class, "EMPTY").invoke(null);
            }
            if (BlahajConfig.instance.optimizeFMLRemapper) {
                BlahajReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "classNameBiMap").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, canonicalizeClassNames((BiMap<String, String>) BlahajReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "classNameBiMap").invokeExact(FMLDeobfuscatingRemapper.INSTANCE)));
                if (!BlahajLoadingPlugin.isDeobf) {
                    BlahajReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "rawFieldMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, DeobfuscatedMappingsMap.of((Map<String, Map<String, String>>) BlahajReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "rawFieldMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE), true));
                    BlahajReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "rawMethodMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, DeobfuscatedMappingsMap.of((Map<String, Map<String, String>>) BlahajReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "rawMethodMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE), false));
                    BlahajReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "fieldNameMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, DeobfuscatedMappingsMap.of((Map<String, Map<String, String>>) BlahajReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "fieldNameMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE), true));
                    BlahajReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "methodNameMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, DeobfuscatedMappingsMap.of((Map<String, Map<String, String>>) BlahajReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "methodNameMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE), false));
                    BlahajReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "fieldDescriptions").invoke(FMLDeobfuscatingRemapper.INSTANCE, new FieldDescriptionsMap((Map<String, Map<String, String>>) BlahajReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "fieldDescriptions").invokeExact(FMLDeobfuscatingRemapper.INSTANCE)));
                    BlahajReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "negativeCacheMethods").invoke(FMLDeobfuscatingRemapper.INSTANCE, new AutoCanonizingSet<>((Set<String>) BlahajReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "negativeCacheMethods").invokeExact(FMLDeobfuscatingRemapper.INSTANCE)));
                    BlahajReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "negativeCacheFields").invoke(FMLDeobfuscatingRemapper.INSTANCE, new AutoCanonizingSet<>((Set<String>) BlahajReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "negativeCacheFields").invokeExact(FMLDeobfuscatingRemapper.INSTANCE)));
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
        map.forEach((s1, s2) -> builder.put(LoliStringPool.canonicalize(s1), LoliStringPool.canonicalize(s2)));
        return builder.build();
    }
}
