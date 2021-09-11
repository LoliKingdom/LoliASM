package zone.rong.loliasm.client.models.bucket.mixins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.Fluid;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.loliasm.bakedquad.BakedQuadFactory;
import zone.rong.loliasm.client.models.bucket.LoliBakedDynBucket;

import javax.annotation.Nullable;
import javax.vecmath.Quat4f;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(value = ModelDynBucket.class, remap = false)
public abstract class ModelDynBucketMixin implements IModel {

    @Shadow @Final private static float NORTH_Z_COVER;
    @Shadow @Final private static float SOUTH_Z_COVER;
    @Shadow @Final private static float NORTH_Z_FLUID;
    @Shadow @Final private static float SOUTH_Z_FLUID;

    @Unique private static final Reference2ObjectOpenHashMap<TextureAtlasSprite, Int2ObjectMap<Collection<BakedQuad>>> partQuads = new Reference2ObjectOpenHashMap<>();

    @Shadow @Final private boolean flipGas;
    @Shadow @Final @Nullable private Fluid fluid;
    @Shadow @Final @Nullable private ResourceLocation baseLocation;
    @Shadow @Final @Nullable private ResourceLocation liquidLocation;
    @Shadow @Final @Nullable private ResourceLocation coverLocation;

    @Unique private ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transformMap;
    @Unique private Pair<TextureAtlasSprite, List<BakedQuad>> bucketInfo;

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        if (transformMap == null) {
            transformMap = Maps.immutableEnumMap(PerspectiveMapWrapper.getTransforms(state));
        }
        if (flipGas && fluid != null && fluid.isLighterThanAir()) {
            state = new ModelStateComposition(state, TRSRTransformation.blockCenterToCorner(new TRSRTransformation(null, new Quat4f(0, 0, 1, 0), null, null)));
        }
        TRSRTransformation transform = state.apply(Optional.empty()).orElse(TRSRTransformation.identity());
        TextureAtlasSprite fluidSprite = null;
        TextureAtlasSprite particleSprite = null;
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
        if (fluid != null) {
            fluidSprite = bakedTextureGetter.apply(fluid.getStill());
        }
        if (baseLocation != null) {
            if (bucketInfo == null) {
                IBakedModel model = (new ItemLayerModel(ImmutableList.of(baseLocation))).bake(state, format, bakedTextureGetter);
                bucketInfo = Pair.of(model.getParticleTexture(), model.getQuads(null, null, 0));
            }
            builder.addAll(bucketInfo.getRight());
            particleSprite = bucketInfo.getLeft();
        }
        if (liquidLocation != null && fluidSprite != null) {
            TextureAtlasSprite liquid = bakedTextureGetter.apply(liquidLocation);
            Collection<BakedQuad> parts;
            Int2ObjectMap<Collection<BakedQuad>> tintIndex = partQuads.computeIfAbsent(fluidSprite, k -> new Int2ObjectArrayMap<>(1));
            parts = tintIndex.get(fluid.getColor());
            if (parts == null) {
                tintIndex.put(fluid.getColor(), parts = Stream.of(ItemTextureQuadConverter.convertTexture(format, transform, liquid, fluidSprite, NORTH_Z_FLUID, EnumFacing.NORTH, fluid.getColor(), 1), ItemTextureQuadConverter.convertTexture(format, transform, liquid, fluidSprite, SOUTH_Z_FLUID, EnumFacing.SOUTH, fluid.getColor(), 1))
                        .flatMap(Collection::stream)
                        .map(ubq -> BakedQuadFactory.create(ubq.getVertexData(), ubq.getTintIndex(), ubq.getFace(), ubq.getSprite(), ubq.shouldApplyDiffuseLighting(), ubq.getFormat()))
                        .collect(Collectors.toList()));
            }
            builder.addAll(parts);
            particleSprite = fluidSprite;
        }
        if (coverLocation != null) {
            TextureAtlasSprite cover = bakedTextureGetter.apply(coverLocation);
            Int2ObjectMap<Collection<BakedQuad>> innerMap = partQuads.get(cover);
            if (innerMap == null) {
                partQuads.put(cover, innerMap = Int2ObjectMaps.singleton(0xFFFFFFFF, Lists.newArrayList(ItemTextureQuadConverter.genQuad(format, transform, 0, 0, 16, 16, NORTH_Z_COVER, cover, EnumFacing.NORTH, 0xFFFFFFFF, 2), ItemTextureQuadConverter.genQuad(format, transform, 0, 0, 16, 16, SOUTH_Z_COVER, cover, EnumFacing.SOUTH, 0xFFFFFFFF, 2))
                        .stream()
                        .map(ubq -> BakedQuadFactory.create(ubq.getVertexData(), ubq.getTintIndex(), ubq.getFace(), ubq.getSprite(), ubq.shouldApplyDiffuseLighting(), ubq.getFormat()))
                        .collect(Collectors.toList())));
            }
            builder.addAll(innerMap.get(0xFFFFFFFF));
            if (particleSprite == null) {
                particleSprite = cover;
            }
        }
        return new LoliBakedDynBucket((ModelDynBucket) (Object) this, builder.build(), particleSprite, format, transformMap, transform.isIdentity());
    }
}
