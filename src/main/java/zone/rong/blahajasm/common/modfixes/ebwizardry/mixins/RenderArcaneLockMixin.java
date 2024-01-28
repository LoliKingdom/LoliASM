package zone.rong.blahajasm.common.modfixes.ebwizardry.mixins;

import electroblob.wizardry.client.renderer.tileentity.RenderArcaneLock;
import electroblob.wizardry.util.GeometryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.blahajasm.common.modfixes.ebwizardry.ArcaneLocks;

import java.util.Iterator;

@Mixin(value = RenderArcaneLock.class, remap = false)
public abstract class RenderArcaneLockMixin {

    @Shadow @Final private static ResourceLocation[] TEXTURES;

    @Shadow private static void drawFace(BufferBuilder b, Vec3d tl, Vec3d tr, Vec3d bl, Vec3d br, float u1, float v1, float u2, float v2) {
        throw new AssertionError();
    }

    /**
     * @author Rongmario
     * @reason Optimization
     */
    @Overwrite
    @SubscribeEvent
    public static void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        if (ArcaneLocks.ARCANE_LOCKED_TILES.isEmpty()) {
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = null;
        EntityPlayer player = Minecraft.getMinecraft().player;
        Vec3d origin = null;
        for (Iterator<TileEntity> iter = ArcaneLocks.ARCANE_LOCKED_TILES.iterator(); iter.hasNext();) {
            TileEntity lockedTile = iter.next();
            if (lockedTile == null || lockedTile.isInvalid()) {
                iter.remove();
                continue;
            }
            if (origin == null) {
                origin = player.getPositionEyes(event.getPartialTicks());
            }
            if (lockedTile.getDistanceSq(origin.x, origin.y, origin.z) <= lockedTile.getMaxRenderDistanceSquared()) {
                if (buffer == null) {
                    buffer = tessellator.getBuffer();
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableLighting();
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
                    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GlStateManager.translate(-origin.x, -origin.y + player.getEyeHeight(), -origin.z);
                    GlStateManager.color(1, 1, 1, 1);
                    Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURES[(player.ticksExisted % (TEXTURES.length * 2)) / 2]);
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                }
                Vec3d[] vertices = GeometryUtils.getVertices(lockedTile.getRenderBoundingBox().grow(0.05).offset(lockedTile.getPos()));
                drawFace(buffer, vertices[0], vertices[1], vertices[3], vertices[2], 0, 0, 1, 1); // Bottom
                drawFace(buffer, vertices[6], vertices[7], vertices[2], vertices[3], 0, 0, 1, 1); // South
                drawFace(buffer, vertices[5], vertices[6], vertices[1], vertices[2], 0, 0, 1, 1); // East
                drawFace(buffer, vertices[4], vertices[5], vertices[0], vertices[1], 0, 0, 1, 1); // North
                drawFace(buffer, vertices[7], vertices[4], vertices[3], vertices[0], 0, 0, 1, 1); // West
                drawFace(buffer, vertices[5], vertices[4], vertices[6], vertices[7], 0, 0, 1, 1); // Top
            }
        }
        if (buffer != null) {
            tessellator.draw();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }
    }

}
