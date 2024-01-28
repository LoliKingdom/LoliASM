package zone.rong.blahajasm.common.modfixes.xu2.mixins;

import com.rwtema.extrautils2.compatibility.CompatClientHelper;
import com.rwtema.extrautils2.compatibility.TESRCompat;
import com.rwtema.extrautils2.render.IVertexBuffer;
import com.rwtema.extrautils2.tile.TileCrafter;
import com.rwtema.extrautils2.tile.XUTile;
import com.rwtema.extrautils2.tile.tesr.XUTESRBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.blahajasm.common.modfixes.xu2.TileCrafterExtension;
import zone.rong.blahajasm.config.BlahajConfig;

import javax.annotation.Nonnull;

@Mixin(value = XUTESRBase.class, remap = false)
public abstract class XUTESRBaseMixin<T extends XUTile> extends TESRCompat<T> {

    @Shadow public abstract void preRender(T te, int destroyStage);
    @Shadow public abstract void postRender(T te, int destroyStage);
    @Shadow public abstract void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage, IVertexBuffer renderer);

    @Shadow protected abstract int getDrawMode(T te);
    @Shadow protected abstract VertexFormat getVertexFormat(T te);

    @Override
    @SideOnly(Side.CLIENT)
    public void render(@Nonnull T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (BlahajConfig.instance.fixXU2CrafterCrash && te instanceof TileCrafter) {
            ((TileCrafterExtension) te).renderAlt(x, y, z);
        } else {
            Tessellator tessellator = Tessellator.getInstance();
            IVertexBuffer vertexBuffer = CompatClientHelper.wrap(tessellator.getBuffer());
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();

            if (Minecraft.isAmbientOcclusionEnabled()) {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
            } else {
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }


            preRender(te, destroyStage);
            vertexBuffer.begin(getDrawMode(te), getVertexFormat(te));
            renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, vertexBuffer);
            vertexBuffer.setTranslation(0, 0, 0);
            tessellator.draw();
            postRender(te, destroyStage);
            RenderHelper.enableStandardItemLighting();
        }
    }

}
