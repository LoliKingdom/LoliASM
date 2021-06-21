package zone.rong.loliasm.client.models.bucket.mixins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.common.model.TRSRTransformation;

public class LoliBakedDynBucket extends BakedItemModel {

    public LoliBakedDynBucket(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms, ItemOverrideList overrides, boolean untransformed) {
        super(quads, particle, transforms, overrides, untransformed);
    }

}
