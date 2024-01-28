package zone.rong.loliasm.common.modfixes.xu2.mixins;

import com.google.common.collect.Lists;
import com.rwtema.extrautils2.backend.ModifyingBakedModel;
import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.compatibility.ICompatPerspectiveAwareModel;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.itemhandler.SingleStackHandler;
import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.tile.TileCrafter;
import com.rwtema.extrautils2.tile.tesr.ITESRHook;
import com.rwtema.extrautils2.utils.MCTimer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.loliasm.common.modfixes.xu2.TileCrafterExtension;
import zone.rong.loliasm.config.LoliConfig;

import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = TileCrafter.class, remap = false)
public abstract class TileCrafterMixin extends TileEntity implements ITESRHook, TileCrafterExtension {

    @Shadow @Final private SingleStackHandler ghostOutput;

    @Override
    @SideOnly(Side.CLIENT)
    public void preRender(int destroyStage) {
        if (!LoliConfig.instance.disableXU2CrafterRendering) {
            GlStateManager.blendFunc(770, 1);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.4F);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderAlt(double x, double y, double z) {
        ItemStack stack = ghostOutput.getStack();
        if (StackHelper.isNull(stack)) {
            return;
        }
        BlockPos upPos = pos.up();
        IBlockState upState = world.getBlockState(upPos);
        if (upState.isOpaqueCube() || upState.isSideSolid(world, upPos, EnumFacing.DOWN)) {
            return; // Don't render
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem renderItem = mc.getRenderItem();
        IBakedModel model = renderItem.getItemModelWithOverrides(stack, null, null);
        GlStateManager.translate(0.5F, model.isGui3d() ? 1.05F : 1.15F, 0.5F);
        GlStateManager.translate(x, y, z);
        GlStateManager.scale(0.9F, 0.9F, 0.9F);
        GlStateManager.rotate((MCTimer.renderTimer / 64) * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.pushMatrix();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.color(1, 1, 1, 0.4F);
        model = ForgeHooksClient.handleCameraTransforms(model, TransformType.GROUND, false);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        if (model.isBuiltInRenderer()) {
            stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
        } else {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.ITEM);
            for (EnumFacing enumfacing : EnumFacing.VALUES) {
                renderItem.renderQuads(bufferbuilder, model.getQuads(null, enumfacing, 0L), -1, stack);
            }
            renderItem.renderQuads(bufferbuilder, model.getQuads(null, null, 0L), -1, stack);
            tessellator.draw();
        }
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IBlockAccess world, BlockPos pos, double x, double y, double z, float partialTicks, int destroyStage, IVertexBuffer buffer, BlockRendererDispatcher dispatcher) {
        if (!LoliConfig.instance.disableXU2CrafterRendering) {
            ItemStack stack = ghostOutput.getStack();
            if (StackHelper.isNull(stack)) {
                return;
            }
            IBakedModel duplicateModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
            IBakedModel finalModel = duplicateModel.getOverrides().handleItemState(duplicateModel, stack, this.world, null);
            if (finalModel instanceof ICompatPerspectiveAwareModel) {
                Pair<? extends IBakedModel, Matrix4f> pair = finalModel.handlePerspective(ItemCameraTransforms.TransformType.GROUND);
                if (pair.getLeft() != null) {
                    finalModel = pair.getLeft();
                }
            }
            finalModel = ModifyingBakedModel.create(finalModel, (original, base, state1, side, rand) -> {
                float t = MCTimer.renderTimer / 64;
                float c = MathHelper.cos(t);
                float s = MathHelper.sin(t);
                ArrayList<BakedQuad> list = Lists.newArrayListWithExpectedSize(original.size());
                for (BakedQuad bakedQuad : original) {
                    int[] data = Arrays.copyOf(bakedQuad.getVertexData(), 28);
                    for (int i = 0; i < 28; i += 7) {
                        float ax = Float.intBitsToFloat(data[i]) - 0.5F;
                        float ay = Float.intBitsToFloat(data[i + 1]);
                        float az = Float.intBitsToFloat(data[i + 2]) - 0.5F;
                        data[i] = Float.floatToRawIntBits(0.5F + (ax * c - az * s) * 0.25F);
                        data[i + 1] = Float.floatToRawIntBits(0.05F + ay * 0.25F);
                        data[i + 2] = Float.floatToRawIntBits(0.5F + (ax * s + az * c) * 0.25F);
                    }
                    list.add(new BakedQuad(data, bakedQuad.getTintIndex(), bakedQuad.getFace(), bakedQuad.getSprite(), bakedQuad.shouldApplyDiffuseLighting(), bakedQuad.getFormat()));
                }
                return list;
            });
            // Fix: Blocks.AIR.getDefaultState() => Block.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getMetadata())
            dispatcher.getBlockModelRenderer().renderModel(world, finalModel, Blocks.AIR.getDefaultState(), pos.up(), CompatClientHelper.unwrap(buffer), false);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void postRender(int destroyStage) {
        if (!LoliConfig.instance.disableXU2CrafterRendering) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

}
