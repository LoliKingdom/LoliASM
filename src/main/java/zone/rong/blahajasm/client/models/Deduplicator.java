package zone.rong.blahajasm.client.models;

import com.google.common.collect.*;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;
import scala.collection.mutable.MultiMap;
import zone.rong.blahajasm.BlahajLogger;
import zone.rong.blahajasm.BlahajReflector;
import zone.rong.blahajasm.api.HashingStrategies;
import zone.rong.blahajasm.api.LoliStringPool;

import java.lang.invoke.MethodHandle;
import java.util.*;

/**
 * A direct competitor with Foamfix's deduplication solution.
 *
 * TODO
 */
@SuppressWarnings("rawtypes")
public class Deduplicator {

    private static final MethodHandle UNPACKED_DATA_GETTER = BlahajReflector.resolveFieldGetter(UnpackedBakedQuad.class, "unpackedData");
    // private static final MethodHandle UNPACKED_DATA_SETTER = BlahajReflector.resolveFieldSetter(UnpackedBakedQuad.class, "unpackedData");

    private static final MethodHandle SIMPLE_BAKED_MODEL_FACE_QUADS_GETTER = BlahajReflector.resolveFieldGetter(SimpleBakedModel.class, "faceQuads", "field_177561_b");
    private static final MethodHandle SIMPLE_BAKED_MODEL_FACE_QUADS_SETTER = BlahajReflector.resolveFieldSetter(SimpleBakedModel.class, "faceQuads", "field_177561_b");
    private static final MethodHandle PERSPECTIVE_MAP_WRAPPER_TRANSFORMS_GETTER = BlahajReflector.resolveFieldGetter(PerspectiveMapWrapper.class, "transforms");
    private static final MethodHandle PERSPECTIVE_MAP_WRAPPER_TRANSFORMS_SETTER = BlahajReflector.resolveFieldSetter(PerspectiveMapWrapper.class, "transforms");
    private static final MethodHandle BAKED_ITEM_MODEL_TRANSFORMS_GETTER = BlahajReflector.resolveFieldGetter(BakedItemModel.class, "transforms");
    private static final MethodHandle BAKED_ITEM_MODEL_TRANSFORMS_SETTER = BlahajReflector.resolveFieldSetter(BakedItemModel.class, "transforms");
    private static final MethodHandle ITEM_OVERRIDE_LIST_OVERRIDES_GETTER = BlahajReflector.resolveFieldGetter(ItemOverrideList.class, "overrides", "field_188023_b");
    private static final MethodHandle ITEM_OVERRIDE_LIST_OVERRIDES_SETTER = BlahajReflector.resolveFieldSetter(ItemOverrideList.class, "overrides", "field_188023_b");
    private static final MethodHandle BLOCK_PART_FACE_TEXTURE_GETTER = BlahajReflector.resolveFieldGetter(BlockPartFace.class, "texture", "field_178242_d");
    private static final MethodHandle BLOCK_PART_FACE_TEXTURE_SETTER = BlahajReflector.resolveFieldSetter(BlockPartFace.class, "texture", "field_178242_d");

    // Used to check instanceof ImmutableEnumMap which is package-private
    private static final ImmutableMap<Placeholder, Object> immutableEnumMap;

    static {
        EnumMap<Placeholder, Placeholder> placeholderEnumMap = new EnumMap<>(Placeholder.class);
        placeholderEnumMap.put(Placeholder.ONE, Placeholder.ONE);
        placeholderEnumMap.put(Placeholder.TWO, Placeholder.TWO);
        placeholderEnumMap.put(Placeholder.THREE, Placeholder.THREE);
        immutableEnumMap = Maps.immutableEnumMap(placeholderEnumMap);
    }

    private final ObjectOpenCustomHashSet<Object> identityCanonicals = new ObjectOpenCustomHashSet<>(HashingStrategies.IDENTITY_OBJECT_HASH);
    private final ObjectOpenCustomHashSet<Object> genericCanonicals = new ObjectOpenCustomHashSet<>(HashingStrategies.GENERIC_OBJECT_HASH);
    /*
    @SuppressWarnings("unchecked")
    private final Reference2ObjectOpenHashMap<Optional, Optional> javaOptionalCache = new Reference2ObjectOpenHashMap();
    @SuppressWarnings("unchecked")
    private final Reference2ObjectOpenHashMap<com.google.common.base.Optional, com.google.common.base.Optional> guavaOptionalCache = new Reference2ObjectOpenHashMap();
     */
    private final ObjectOpenCustomHashSet<float[]> floatArrayCache = new ObjectOpenCustomHashSet<>(FloatArrays.HASH_STRATEGY);
    private final ObjectOpenCustomHashSet<float[][]> float2dArrayCache = new ObjectOpenCustomHashSet<>(HashingStrategies.FLOAT_2D_ARRAY_HASH);
    private final ObjectOpenCustomHashSet<Object> genericObjectCache = new ObjectOpenCustomHashSet<>(HashingStrategies.IDENTITY_OBJECT_HASH);
    private final ObjectOpenCustomHashSet<ItemCameraTransforms> itemCameraTransformsCache = new ObjectOpenCustomHashSet<>(HashingStrategies.ITEM_CAMERA_TRANSFORMS_HASH);

    @SuppressWarnings("unchecked")
    public final Object deduplicate(Object o) {
        if (o == null) {
            return null;
        }
        Object n = genericCanonicals.addOrGet(o);
        if (n != o) {
            return o;
        }
        if (o instanceof IBakedModel) {
            if (o instanceof SimpleBakedModel) {
                /* TODO: SimpleBakedModel$Builder can transform BakedQuads => BakedQuadRetextureds if the ctor with IBakedModel is used.
                    Add a small check in the builder method to check if the 2 sprites are identical before transforming.
                    This isn't a big deal atm since BlahajASM optimizes BakedQuadRetextureds as well.
                 */
                trim(((IBakedModel) o).getQuads(null, null, 0L)); // Trim generalQuads
                for (EnumFacing facing : EnumFacing.VALUES) {
                    trim(((IBakedModel) o).getQuads(null, facing, 0L)); // Trim faceQuads
                }
                try {
                    Map<EnumFacing, List<BakedQuad>> faceQuads = (Map<EnumFacing, List<BakedQuad>>) SIMPLE_BAKED_MODEL_FACE_QUADS_GETTER.invokeExact((SimpleBakedModel) o);
                    if (!(faceQuads instanceof EnumMap)) {
                        BlahajLogger.instance.debug("Found a non-EnumMap faceQuads instance! Preparing to transform");
                        // If faceQuads isn't an EnumMap, we replace it with an EnumMap
                        SIMPLE_BAKED_MODEL_FACE_QUADS_SETTER.invokeExact((SimpleBakedModel) o, new EnumMap<>(faceQuads));
                    }
                } catch (Throwable t) {
                    BlahajLogger.instance.throwing(t);
                }
                return o;
            } else if (o instanceof PerspectiveMapWrapper) {
                try {
                    Object transforms = PERSPECTIVE_MAP_WRAPPER_TRANSFORMS_GETTER.invoke(o);
                    if (transforms != null) {
                        Object newTransforms = canonicalize(transforms);
                        if (transforms != newTransforms) {
                            BlahajLogger.instance.debug("Canonized PerspectiveMapWrapper#transforms successfully.");
                            PERSPECTIVE_MAP_WRAPPER_TRANSFORMS_SETTER.invoke((PerspectiveMapWrapper) o, newTransforms);
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        return o;
    }

    /*
    public final Object deduplicate(Object o) {
        if (o == null || !canonicals.add(o)) {
            return o;
        }
        if (o instanceof IBakedModel) {
            if (o instanceof SimpleBakedModel) {
                trim(((IBakedModel) o).getQuads(null, null, 0L)); // Trim generalQuads
                for (EnumFacing facing : EnumFacing.VALUES) {
                    trim(((IBakedModel) o).getQuads(null, facing, 0L)); // Trim faceQuads
                }
            } else if (o instanceof PerspectiveMapWrapper) {
                try {
                    Object transforms = PERSPECTIVE_MAP_WRAPPER_TRANSFORMS_GETTER.invokeExact((PerspectiveMapWrapper) o);
                    if (transforms != null) {
                        Object newTransforms = canonicalize(transforms);
                        if (transforms != newTransforms) {
                            PERSPECTIVE_MAP_WRAPPER_TRANSFORMS_SETTER.invokeExact((PerspectiveMapWrapper) o, newTransforms);
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else if (o instanceof BakedItemModel) {
                try {
                    Object transforms = BAKED_ITEM_MODEL_TRANSFORMS_GETTER.invokeExact((BakedItemModel) o);
                    if (transforms != null) {
                        Object newTransforms = canonicalize(transforms);
                        if (transforms != newTransforms) {
                            BAKED_ITEM_MODEL_TRANSFORMS_SETTER.invokeExact((BakedItemModel) o, newTransforms);
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } else if (o instanceof BlockPartFace) { // Canonize String => StringPool
            BlockPartFace bpf = (BlockPartFace) o;
            bpf.blockFaceUV.uvs = (float[]) canonicalize(bpf.blockFaceUV.uvs);
            try {
                Object texture = BLOCK_PART_FACE_TEXTURE_GETTER.invokeExact(bpf);
                if (texture != null) {
                    Object newTexture = canonicalize(texture);
                    if (texture != newTexture) {
                        BLOCK_PART_FACE_TEXTURE_SETTER.invokeExact(bpf, newTexture);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return o;
        } else if (o instanceof UnpackedBakedQuad) {
            try {
                canonicalize(UNPACKED_DATA_GETTER.invokeExact((UnpackedBakedQuad) o));
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return o;
        } else if (o instanceof ResourceLocation || o instanceof TRSRTransformation || o instanceof Vec3d || o instanceof Vec3i || o instanceof ItemCameraTransforms) {
            return canonicalize(o);
        } else if (o instanceof ItemOverrideList && o != ItemOverrideList.NONE) {
            try {
                List list = (List) ITEM_OVERRIDE_LIST_OVERRIDES_GETTER.invokeExact((ItemOverrideList) o);
                if (list.isEmpty()) {
                    if (o instanceof AnimationItemOverrideList) {
                        ITEM_OVERRIDE_LIST_OVERRIDES_SETTER.invokeExact((ItemOverrideList) o, (List) ImmutableList.of());
                    } else {
                        return ItemOverrideList.NONE;
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return o;
        } else if (o instanceof Multimap) {
            if (o instanceof ImmutableMultimap || o instanceof SortedSetMultimap) {
                for (Object value : ((Multimap) o).values()) {
                    deduplicate(value);
                }
            } else {
                for (Object key : ((Multimap) o).keySet()) {
                    List l = new ArrayList(((Multimap) o).values());
                    for (int i = 0; i < l.size(); i++) {
                        l.set(i, deduplicate(l.get(i)));
                    }
                    ((Multimap) o).replaceValues(key, l);
                }
            }
            return o;
        } else if (o instanceof Map) {
            for (Object key : ((Map) o).keySet()) {
                deduplicate(key);
            }
            if (o instanceof SortedMap) {
                for (Object values : ((Map) o).values()) {
                    deduplicate(values);
                }
            } else if (o instanceof ImmutableMap) {
                ImmutableMap im = (ImmutableMap) o;
                ImmutableMap.Builder newMap = (o instanceof ImmutableBiMap) ? ImmutableBiMap.builder() : ImmutableMap.builder();
                boolean deduplicated = false;
                for (Object key : im.keySet()) {
                    Object a = im.get(key);
                    Object b = deduplicate(a);
                    newMap.put(key, b != null ? b : a);
                    if (b != null && b != a) {
                        deduplicated = true;
                    }
                }
                return deduplicated ? newMap.build() : o;
            } else {
                try {
                    for (Object key : ((Map) o).keySet()) {
                        Object value = ((Map) o).get(key);
                        Object valueD = deduplicate(value);
                        if (valueD != null && value != valueD)
                            ((Map) o).put(key, valueD);
                    }
                } catch (UnsupportedOperationException e) {
                    e.printStackTrace();
                }
            }
            return o;
        } else if (o instanceof Collection) {
            if (o instanceof List) {
                if (o instanceof ImmutableList) {
                    ImmutableList il = (ImmutableList) o;
                    ImmutableList.Builder builder = ImmutableList.builder();
                    boolean deduplicated = false;
                    for (int i = 0; i < il.size(); i++) {
                        Object a = il.get(i);
                        Object b = deduplicate(a);
                        builder.add(b != null ? b : a);
                        if (b != null && b != a)
                            deduplicated = true;
                    }
                    if (deduplicated) {
                        return builder.build();
                    }
                } else {
                    List l = (List) o;
                    try {
                        for (int i = 0; i < l.size(); i++) {
                            l.set(i, deduplicate(l.get(i)));
                        }
                    } catch (UnsupportedOperationException e) {
                        e.printStackTrace();
                    }
                }
                return o;
            } else if (o instanceof ImmutableSet) {
                if (!(o instanceof ImmutableSortedSet)) {
                    ImmutableSet.Builder builder = new ImmutableSet.Builder();
                    for (Object o1 : ((Set) o)) {
                        builder.add(deduplicate(o1));
                    }
                    o = builder.build();
                } else {
                    for (Object o1 : ((Set) o)) {
                        deduplicate(o1);
                    }
                }
                return o;
            } else if (o instanceof Set && !(o instanceof SortedSet)) {
                // Stub
            } else {
                for (Object o1 : ((Collection) o)) {
                    deduplicate(o1);
                }
                return o;
            }
        }
        return o;
    }
     */

    private Object canonicalize(Object o) {
        Object n = o;
        if (o instanceof float[]) {
            n = floatArrayCache.addOrGet((float[]) o);
        } else if (o instanceof float[][]) {
            float[][] o2 = float2dArrayCache.addOrGet((float[][]) o);
            if (o == o2) {
                for (int i = 0; i < o2.length; i++) {
                    o2[i] = (float[]) canonicalize(o2[i]);
                }
            } else {
                n = o2;
            }
        } else if (o instanceof float[][][]) {
            float[][][] o2 = (float[][][]) o;
            for (int i = 0; i < o2.length; i++) {
                o2[i] = (float[][]) canonicalize(o2[i]);
            }
        } else if (o instanceof Map) {
            if (o instanceof ImmutableMap && ((Map) o).isEmpty() && n != ImmutableMap.of()) {
                n = ImmutableMap.of();
            } else if (o.getClass() != immutableEnumMap.getClass() && !((Map) o).isEmpty() && ((Map) o).keySet().toArray()[0] instanceof Enum) {
                n = new EnumMap<>((Map) o);
            }
        } else if (o instanceof Collection || o instanceof MultiMap || o instanceof ResourceLocation || o instanceof Vec3d || o instanceof Vec3i || o instanceof TRSRTransformation) {
            n = genericObjectCache.addOrGet(o);
        } else if (o instanceof ItemCameraTransforms) {
            n = itemCameraTransformsCache.addOrGet((ItemCameraTransforms) o);
        } else if (o instanceof String) {
            n = LoliStringPool.canonicalize((String) o);
        }
        if (n != o) {
            BlahajLogger.instance.info("Deduplicated {}@{} => {}@{} successfully", o.getClass().getName(), Integer.toHexString(o.hashCode()), n.getClass().getName(), Integer.toHexString(n.hashCode()));
        }
        return n;
    }

    private void trim(Object o) {
        if (o instanceof ArrayList) {
            ((ArrayList) o).trimToSize();
        }
    }

    private enum Placeholder {
        ONE,
        TWO,
        THREE;
    }

}
