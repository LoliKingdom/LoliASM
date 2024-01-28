package zone.rong.loliasm.client.screenshot.mixins;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ScreenShotHelper;
import org.lwjgl.BufferUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

@Mixin(ScreenShotHelper.class)
public class ScreenShotHelperMixin {

    /**
     * @author Rongmario
     * @reason Keep int array + buffer on the stack
     */
    @Overwrite
    public static BufferedImage createScreenshot(int width, int height, Framebuffer framebufferIn) {
        boolean fbe = OpenGlHelper.isFramebufferEnabled();
        if (fbe) {
            width = framebufferIn.framebufferTextureWidth;
            height = framebufferIn.framebufferTextureHeight;
        }
        int i = width * height;
        IntBuffer pixelBuffer = BufferUtils.createIntBuffer(i);
        GlStateManager.glPixelStorei(3333, 1);
        GlStateManager.glPixelStorei(3317, 1);
        if (fbe) {
            GlStateManager.bindTexture(framebufferIn.framebufferTexture);
            GlStateManager.glGetTexImage(3553, 0, 32993, 33639, pixelBuffer);
        } else {
            GlStateManager.glReadPixels(0, 0, width, height, 32993, 33639, pixelBuffer);
        }
        int[] pixelValues = new int[i];
        pixelBuffer.get(pixelValues);
        TextureUtil.processPixelValues(pixelValues, width, height);
        BufferedImage bufferedimage = new BufferedImage(width, height, 1);
        bufferedimage.setRGB(0, 0, width, height, pixelValues, 0, width);
        return bufferedimage;
    }

}
