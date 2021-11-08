package zone.rong.loliasm.core;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.chars.Char2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMaps;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import mezz.jei.suffixtree.GeneralizedSuffixTree;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import zone.rong.loliasm.LoliASM;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.api.LoliStringPool;
import zone.rong.loliasm.bakedquad.SupportingBakedQuad;
import zone.rong.loliasm.config.LoliConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class LoliHooks {

    /*
    private static final MethodHandle STRING_BACKING_CHAR_ARRAY_GETTER = LoliReflector.resolveFieldGetter(String.class, "value");
    private static final char[] EMPTY_CHAR_ARRAY;

    static {
        char[] array;
        try {
            array = (char[]) STRING_BACKING_CHAR_ARRAY_GETTER.invokeExact("");
        } catch (Throwable throwable) {
            array = new char[0];
        }
        EMPTY_CHAR_ARRAY = array;
    }
     */

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

    public static <K, V> Object2ObjectOpenHashMap<K, V> createHashMap() {
        return new Object2ObjectOpenHashMap<>();
    }

    public static <K, V> Reference2ObjectOpenHashMap<K, V> createReferenceMap() {
        return new Reference2ObjectOpenHashMap<>();
    }

    private static Set<Class<?>> classesThatCallBakedQuadCtor;
    private static Set<Class<?>> classesThatExtendBakedQuad;

    public static void inform(Class<?> clazz) {
        if (clazz == SupportingBakedQuad.class) {
            return;
        }
        if (classesThatCallBakedQuadCtor == null) {
            classesThatCallBakedQuadCtor = new ReferenceOpenHashSet<>();
        }
        if (classesThatCallBakedQuadCtor.add(clazz)) {
            LoliConfig.instance.editClassesThatCallBakedQuadCtor(clazz);
        }
        if (BakedQuad.class.isAssignableFrom(clazz)) {
            if (classesThatExtendBakedQuad == null) {
                classesThatExtendBakedQuad = new ReferenceOpenHashSet<>();
            }
            if (classesThatExtendBakedQuad.add(clazz)) {
                LoliConfig.instance.editClassesThatExtendBakedQuad(clazz);
            }
        }
    }

    public static void modCandidate$override$addClassEntry(ModCandidate modCandidate, String name, Set<String> foundClasses, Set<String> packages, ASMDataTable table) {
        String className = name.substring(0, name.lastIndexOf('.'));
        foundClasses.add(className);
        className = className.replace('/','.');
        int pkgIdx = className.lastIndexOf('.');
        if (pkgIdx > -1) {
            String pkg = LoliStringPool.canonicalize(className.substring(0, pkgIdx));
            packages.add(pkg);
            table.registerPackage(modCandidate, pkg);
        }
    }

    public static String asmData$redirect$CtorStringsToIntern(String string) {
        return string == null ? null : LoliStringPool.canonicalize(string);
    }

    public static /*char[]*/ String nbtTagString$override$ctor(String data) {
        /*
        if (data == null) {
            throw new NullPointerException("Null string not allowed");
        }
         */
        return LoliStringPool.canonicalize(data);
        /*
        try {
            return (char[]) STRING_BACKING_CHAR_ARRAY_GETTER.invokeExact(data);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return EMPTY_CHAR_ARRAY;
        }
         */
    }

}
