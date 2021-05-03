package zone.rong.loliasm.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import zone.rong.loliasm.LoliConfig;
import zone.rong.loliasm.LoliReflector;
import zone.rong.loliasm.api.datastructures.canonical.AutoCanonizingSet;
import zone.rong.loliasm.api.datastructures.deobf.DeobfuscatedMappingsMap;
import zone.rong.loliasm.api.datastructures.deobf.FieldDescriptionsMap;
import zone.rong.loliasm.api.StringPool;

import java.util.Map;
import java.util.Set;

public class LoliFMLCallHook implements IFMLCallHook {

    @Override
    @SuppressWarnings("unchecked")
    public Void call() {
        try {
            // This deduplicates 50% of data in deobfuscated environments
            LoliReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "classNameBiMap").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, canonizeClassNames((BiMap<String, String>) LoliReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "classNameBiMap").invokeExact(FMLDeobfuscatingRemapper.INSTANCE)));
            if (!FMLLaunchHandler.isDeobfuscatedEnvironment() && LoliConfig.getConfig().remapperMemorySaver) {
                LoliReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "rawFieldMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, DeobfuscatedMappingsMap.of((Map<String, Map<String, String>>) LoliReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "rawFieldMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE), true));
                LoliReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "rawMethodMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, DeobfuscatedMappingsMap.of((Map<String, Map<String, String>>) LoliReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "rawMethodMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE), false));
                LoliReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "fieldNameMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, DeobfuscatedMappingsMap.of((Map<String, Map<String, String>>) LoliReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "fieldNameMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE), true));
                LoliReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "methodNameMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE, DeobfuscatedMappingsMap.of((Map<String, Map<String, String>>) LoliReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "methodNameMaps").invokeExact(FMLDeobfuscatingRemapper.INSTANCE), false));
                LoliReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "fieldDescriptions").invoke(FMLDeobfuscatingRemapper.INSTANCE, new FieldDescriptionsMap((Map<String, Map<String, String>>) LoliReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "fieldDescriptions").invokeExact(FMLDeobfuscatingRemapper.INSTANCE)));
                LoliReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "negativeCacheMethods").invoke(FMLDeobfuscatingRemapper.INSTANCE, new AutoCanonizingSet<>((Set<String>) LoliReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "negativeCacheMethods").invokeExact(FMLDeobfuscatingRemapper.INSTANCE)));
                LoliReflector.resolveFieldSetter(FMLDeobfuscatingRemapper.class, "negativeCacheFields").invoke(FMLDeobfuscatingRemapper.INSTANCE, new AutoCanonizingSet<>((Set<String>) LoliReflector.resolveFieldGetter(FMLDeobfuscatingRemapper.class, "negativeCacheFields").invokeExact(FMLDeobfuscatingRemapper.INSTANCE)));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    private BiMap<String, String> canonizeClassNames(BiMap<String, String> map) {
        ImmutableBiMap.Builder<String, String> builder = ImmutableBiMap.builder();
        map.forEach((s1, s2) -> builder.put(StringPool.canonize(s1), StringPool.canonize(s2)));
        return builder.build();
    }
}
