package zone.rong.loliasm.client.models.bucket.mixins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
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
import zone.rong.loliasm.client.models.bucket.LoliBakedDynBucket;

import javax.annotation.Nullable;
import javax.vecmath.Quat4f;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.minecraftforge.client.model.ItemTextureQuadConverter.convertTexture;
import static net.minecraftforge.client.model.ItemTextureQuadConverter.genQuad;
import static zone.rong.loliasm.bakedquad.BakedQuadFactory.*;
import static zone.rong.loliasm.client.models.bucket.LoliBakedDynBucket.*;

@Mixin(value = ModelDynBucket.class, remap = false)
public abstract class ModelDynBucketMixin implements IModel {

    @Shadow @Final private static float NORTH_Z_COVER;
    @Shadow @Final private static float SOUTH_Z_COVER;
    @Shadow @Final private static float NORTH_Z_FLUID;
    @Shadow @Final private static float SOUTH_Z_FLUID;

    @Shadow @Final private boolean flipGas;
    @Shadow @Final @Nullable private Fluid fluid;
    @Shadow @Final @Nullable private ResourceLocation baseLocation;
    @Shadow @Final @Nullable private ResourceLocation liquidLocation;
    @Shadow @Final @Nullable private ResourceLocation coverLocation;

    @Shadow @Final private boolean tint;

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        ImmutableMap<TransformType, TRSRTransformation> transformMap = PerspectiveMapWrapper.getTransforms(state);
        boolean flipped = false;
        if (flipGas && fluid != null && fluid.isLighterThanAir()) {
            flipped = true;
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
            Map<ResourceLocation, Pair<ResourceLocation, BakedQuad[]>> map = flipped ? flippedBaseQuads : baseQuads;
            Pair<ResourceLocation, BakedQuad[]> baseQuad = map.get(baseLocation);
            if (baseQuad == null) {
                IBakedModel model = new ItemLayerModel(ImmutableList.of(baseLocation)).bake(state, format, bakedTextureGetter);
                map.put(baseLocation, baseQuad = Pair.of(new ResourceLocation(model.getParticleTexture().getIconName()), model.getQuads(null, null, 0).toArray(new BakedQuad[0])));
            }
            builder.add(baseQuad.getRight());
            particleSprite = bakedTextureGetter.apply(baseQuad.getLeft());
        }
        if (liquidLocation != null && fluidSprite != null) {
            TextureAtlasSprite liquid = bakedTextureGetter.apply(liquidLocation);
            builder.add(Stream.of(convertTexture(format, transform, liquid, fluidSprite, NORTH_Z_FLUID, EnumFacing.NORTH, tint ? fluid.getColor() : 0xFFFFFFFF, 1),
                            convertTexture(format, transform, liquid, fluidSprite, SOUTH_Z_FLUID, EnumFacing.SOUTH, tint ? fluid.getColor() : 0xFFFFFFFF, 1))
                            .flatMap(Collection::stream)
                            .map(ubq -> create(ubq.getVertexData(), ubq.getTintIndex(), ubq.getFace(), ubq.getSprite(), ubq.shouldApplyDiffuseLighting(), ubq.getFormat()))
                    .toArray(BakedQuad[]::new));
            particleSprite = fluidSprite;
        }
        if (coverLocation != null) {
            TextureAtlasSprite cover = bakedTextureGetter.apply(coverLocation);
            Map<ResourceLocation, BakedQuad[]> map = flipped ? flippedCoverQuads : coverQuads;
            BakedQuad[] quads = map.get(coverLocation);
            if (quads == null) {
                map.put(coverLocation, quads = Stream.of(genQuad(format, transform, 0, 0, 16, 16, NORTH_Z_COVER, cover, EnumFacing.NORTH, 0xFFFFFFFF, 2),
                                        genQuad(format, transform, 0, 0, 16, 16, SOUTH_Z_COVER, cover, EnumFacing.SOUTH, 0xFFFFFFFF, 2))
                        .map(ubq -> create(ubq.getVertexData(), ubq.getTintIndex(), ubq.getFace(), ubq.getSprite(), ubq.shouldApplyDiffuseLighting(), ubq.getFormat()))
                        .toArray(BakedQuad[]::new));
            }
            builder.add(quads);
            if (particleSprite == null) {
                particleSprite = cover;
            }
        }
        return new LoliBakedDynBucket((ModelDynBucket) (Object) this, builder.build(), particleSprite, format, transformMap, transform.isIdentity());
    }
}
