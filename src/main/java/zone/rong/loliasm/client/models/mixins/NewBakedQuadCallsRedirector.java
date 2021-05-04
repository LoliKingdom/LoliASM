package zone.rong.loliasm.client.models.mixins;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zone.rong.loliasm.bakedquad.BakedQuadFactory;

@Mixin(FaceBakery.class)
public class NewBakedQuadCallsRedirector {

    @SuppressWarnings("all")
    @Redirect(method = "*", at = @At(value = "NEW", target = "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZLnet/minecraft/client/renderer/vertex/VertexFormat;)Lnet/minecraft/client/renderer/block/model/BakedQuad;"))
    private BakedQuad redirect(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn, boolean applyDiffuseLighting, VertexFormat format) {
        return BakedQuadFactory.create(vertexDataIn, tintIndexIn, faceIn, spriteIn, applyDiffuseLighting, format);
    }

    @SuppressWarnings("all")
    @Redirect(method = "*", at = @At(value = "NEW", target = "([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Z)Lnet/minecraft/client/renderer/block/model/BakedQuad;"))
    private BakedQuad redirectDeprecated(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn, boolean applyDiffuseLighting) {
        return BakedQuadFactory.create(vertexDataIn, tintIndexIn, faceIn, spriteIn, applyDiffuseLighting, DefaultVertexFormats.ITEM);
    }

}
