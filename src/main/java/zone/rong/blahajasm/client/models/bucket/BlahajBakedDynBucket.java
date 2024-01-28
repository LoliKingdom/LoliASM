package zone.rong.blahajasm.client.models.bucket;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class BlahajBakedDynBucket extends BakedItemModel {

    public static final Map<ResourceLocation, Pair<ResourceLocation, BakedQuad[]>> baseQuads = new Object2ObjectOpenHashMap<>();
    public static final Map<ResourceLocation, Pair<ResourceLocation, BakedQuad[]>> flippedBaseQuads = new Object2ObjectOpenHashMap<>();
    public static final Map<ResourceLocation, BakedQuad[]> coverQuads = new Object2ObjectOpenHashMap<>();
    public static final Map<ResourceLocation, BakedQuad[]> flippedCoverQuads = new Object2ObjectOpenHashMap<>();

    private final ModelDynBucket parent;
    private final VertexFormat format;
    private final Cache<String, IBakedModel> bucketVariants;

    public BlahajBakedDynBucket(ModelDynBucket parent, ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, VertexFormat format, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms, boolean untransformed) {
        super(quads, particle, transforms, BlahajBakedDynBucketOverrdeList.INSTANCE, untransformed);
        this.parent = parent;
        this.format = format;
        this.bucketVariants = CacheBuilder.newBuilder().weakValues().build();
    }

    private static final class BlahajBakedDynBucketOverrdeList extends ItemOverrideList {

        private static final BlahajBakedDynBucketOverrdeList INSTANCE = new BlahajBakedDynBucketOverrdeList();

        private BlahajBakedDynBucketOverrdeList() {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            FluidStack fluidStack = FluidUtil.getFluidContained(stack);
            if (fluidStack == null) {
                return originalModel;
            }
            BlahajBakedDynBucket bakedModel = (BlahajBakedDynBucket) originalModel;
            try {
                return bakedModel.bucketVariants.get(fluidStack.getFluid().getName(), () -> bake(bakedModel, fluidStack.getFluid().getName()));
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return bake(bakedModel, fluidStack.getFluid().getName());
        }

        private IBakedModel bake(BlahajBakedDynBucket bakedModel, String fluidName) {
            IModel parent = bakedModel.parent.process(ImmutableMap.of("fluid", fluidName));
            return parent.bake(new SimpleModelState(bakedModel.transforms), bakedModel.format, ModelLoader.defaultTextureGetter());
        }
    }

}
