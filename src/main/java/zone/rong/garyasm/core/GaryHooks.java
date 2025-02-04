package zone.rong.garyasm.core;

import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import zone.rong.garyasm.api.GaryStringPool;
import zone.rong.garyasm.bakedquad.BakedQuadFactory;
import zone.rong.garyasm.bakedquad.SupportingBakedQuad;
import zone.rong.garyasm.config.GaryConfig;

import java.util.Set;

@SuppressWarnings("unused")
public class GaryHooks {

    public static <K> ObjectArraySet<K> createArraySet() {
        return new ObjectArraySet<>();
    }

    public static <K> ObjectOpenHashSet<K> createHashSet() {
        return new ObjectOpenHashSet<>();
    }

    public static <K> ReferenceOpenHashSet<K> createReferenceSet() {
        return new ReferenceOpenHashSet<>();
    }

    public static <K, V> Object2ObjectArrayMap<K, V> createArrayMap() {
        return new Object2ObjectArrayMap<>();
    }

    public static <K, V> Object2ObjectLinkedOpenHashMap<K, V> createLinkedMap() {
        return new Object2ObjectLinkedOpenHashMap<>();
    }

    public static <K, V> Object2ObjectOpenHashMap<K, V> createHashMap() {
        return new Object2ObjectOpenHashMap<>();
    }

    public static <K, V> Reference2ObjectOpenHashMap<K, V> createReferenceMap() {
        return new Reference2ObjectOpenHashMap<>();
    }

    private static Set<Class<?>> classesThatCallBakedQuadCtor;
    private static Set<Class<?>> classesThatExtendBakedQuad;

    public static void inform(Class<?> clazz) {
        if (clazz == SupportingBakedQuad.class || clazz == BakedQuadFactory.class) {
            return;
        }
        if (classesThatCallBakedQuadCtor == null) {
            classesThatCallBakedQuadCtor = new ReferenceOpenHashSet<>();
        }
        if (classesThatCallBakedQuadCtor.add(clazz)) {
            GaryConfig.instance.editClassesThatCallBakedQuadCtor(clazz);
        }
        if (BakedQuad.class.isAssignableFrom(clazz)) {
            if (classesThatExtendBakedQuad == null) {
                classesThatExtendBakedQuad = new ReferenceOpenHashSet<>();
            }
            if (classesThatExtendBakedQuad.add(clazz)) {
                GaryConfig.instance.editClassesThatExtendBakedQuad(clazz);
            }
        }
    }

    public static void modCandidate$override$addClassEntry(ModCandidate modCandidate, String name, Set<String> foundClasses, Set<String> packages, ASMDataTable table) {
        String className = name.substring(0, name.lastIndexOf('.'));
        foundClasses.add(className);
        className = className.replace('/','.');
        int pkgIdx = className.lastIndexOf('.');
        if (pkgIdx > -1) {
            String pkg = GaryStringPool.canonicalize(className.substring(0, pkgIdx));
            packages.add(pkg);
            table.registerPackage(modCandidate, pkg);
        }
    }

    public static String asmData$redirect$CtorStringsToIntern(String string) {
        return string == null ? null : GaryStringPool.canonicalize(string);
    }

    public static String nbtTagString$override$ctor(String data) {
        return GaryStringPool.canonicalize(data);
    }
}
