package zone.rong.loliasm.client.sprite.mixins;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import zone.rong.loliasm.client.sprite.IAnimatedSpritePrimer;
import zone.rong.loliasm.client.sprite.ICompiledChunkExpander;

import java.util.BitSet;
import java.util.List;

@Mixin(BlockModelRenderer.class)
public class BlockModelRendererMixin {

    @SuppressWarnings("all")
    @Inject(method = "renderQuadsSmooth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BlockModelRenderer;fillQuadBounds" +
            "(Lnet/minecraft/block/state/IBlockState;[ILnet/minecraft/util/EnumFacing;[FLjava/util/BitSet;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void sendAnimatedSprites$smooth(IBlockAccess blockAccessIn, IBlockState stateIn, BlockPos posIn, BufferBuilder buffer, List<BakedQuad> list, float[] quadBounds, BitSet bitSet, Object aoFace, CallbackInfo ci, Vec3d vec3d, double d0, double d1, double d2, int i, int j, BakedQuad bakedquad) {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread() && bakedquad.getSprite().hasAnimationMetadata()) {
            CompiledChunk chunk = IAnimatedSpritePrimer.CURRENT_COMPILED_CHUNK.get();
            if (chunk != CompiledChunk.DUMMY) {
                ((ICompiledChunkExpander) chunk).resolve(bakedquad.getSprite());
            }
        }
    }

    @SuppressWarnings("all")
    @Inject(method = "renderQuadsFlat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BufferBuilder;addVertexData([I)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void sendAnimatedSprites$flat(IBlockAccess blockAccessIn, IBlockState stateIn, BlockPos posIn, int brightnessIn, boolean ownBrightness, BufferBuilder buffer, List<BakedQuad> list, BitSet bitSet, CallbackInfo ci, Vec3d vec3, double d0, double d1, double d2, int i, int j, BakedQuad bakedquad) {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread() && bakedquad.getSprite().hasAnimationMetadata()) {
            CompiledChunk chunk = IAnimatedSpritePrimer.CURRENT_COMPILED_CHUNK.get();
            if (chunk != CompiledChunk.DUMMY) {
                ((ICompiledChunkExpander) chunk).resolve(bakedquad.getSprite());
            }
        }
    }

}
