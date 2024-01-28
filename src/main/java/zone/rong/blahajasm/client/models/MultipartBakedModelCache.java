package zone.rong.loliasm.client.models;

import com.google.common.base.Predicate;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;

import java.util.Map;

/**
 * TODO: Not used at the moment, will be activated with the Deduplicator when that is complete
 */
public class MultipartBakedModelCache {

    public static int total = 0;
    public static int unique = 0;

    private static final Map<Map<Predicate<IBlockState>, IBakedModel>, MultipartBakedModel> KNOWN_MULTIPART_MODELS = new Object2ObjectOpenHashMap<>(32);

    public static MultipartBakedModel makeMultipartModel(Map<Predicate<IBlockState>, IBakedModel> selectors) {
        MultipartBakedModel multipartBakedModel = KNOWN_MULTIPART_MODELS.get(selectors);
        total++;
        if (multipartBakedModel == null) {
            unique++;
            KNOWN_MULTIPART_MODELS.put(selectors, multipartBakedModel = new MultipartBakedModel(selectors));
        }
        return multipartBakedModel;
        // return KNOWN_MULTIPART_MODELS.computeIfAbsent(selectors, MultipartBakedModel::new);
    }

    public static void destroyCache() {
        KNOWN_MULTIPART_MODELS.clear();
    }

}
