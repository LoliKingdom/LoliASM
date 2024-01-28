package zone.rong.blahajasm.client.sprite.ondemand.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.blahajasm.client.sprite.ondemand.IAnimatedSpritePrimer;
import zone.rong.blahajasm.client.sprite.ondemand.IBufferPrimerConfigurator;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements IBufferPrimerConfigurator {

    @Shadow private int drawMode;
    @Shadow private int vertexCount;
    @Shadow private VertexFormat vertexFormat;
    @Shadow private int vertexFormatIndex;
    @Shadow private VertexFormatElement vertexFormatElement;
    @Shadow private ByteBuffer byteBuffer;
    @Shadow public boolean isDrawing;

    @Shadow public abstract void nextVertexFormatIndex();
    @Shadow protected abstract int getBufferSize();

    @Unique private IAnimatedSpritePrimer primer;

    @Unique private int vertexCountOfQuad;
    @Unique private float minUOfQuad = Float.POSITIVE_INFINITY, minVOfQuad = Float.POSITIVE_INFINITY;

    @Override
    public void setPrimer(IAnimatedSpritePrimer primer) {
        this.primer = primer;
    }

    /**
     * @author Rongmario
     * @reason Capture tex(double u, double v)
     */
    @Overwrite
    public BufferBuilder tex(double u, double v) {
        int i = this.vertexCount * this.vertexFormat.getSize() + this.vertexFormat.getOffset(this.vertexFormatIndex);
        switch (this.vertexFormatElement.getType()) {
            case FLOAT:
                float fu = (float) u;
                float fv = (float) v;
                this.byteBuffer.putFloat(i, fu);
                this.byteBuffer.putFloat(i + 4, fv);
                hookTexture(fu, fv);
                break;
            case UINT:
            case INT:
                this.byteBuffer.putInt(i, (int) u);
                this.byteBuffer.putInt(i + 4, (int) v);
                break;
            case USHORT:
            case SHORT:
                this.byteBuffer.putShort(i, (short) ((int) v));
                this.byteBuffer.putShort(i + 2, (short) ((int) u));
                break;
            case UBYTE:
            case BYTE:
                this.byteBuffer.put(i, (byte) ((int) v));
                this.byteBuffer.put(i + 1, (byte) ((int) u));
        }
        this.nextVertexFormatIndex();
        return (BufferBuilder) (Object) this;
    }

    /**
     * @author Rongmario
     * @reason Reset {@link IAnimatedSpritePrimer#PRIMED}
     */
    @Overwrite
    public void finishDrawing() {
        if (!this.isDrawing) {
            throw new IllegalStateException("Not building!");
        } else {
            this.isDrawing = false;
            this.byteBuffer.position(0);
            this.byteBuffer.limit(this.getBufferSize() * 4);
            if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
                IAnimatedSpritePrimer.PRIMED.set(false);
            }
        }
    }

    @Override
    public void hookTexture(float fu, float fv) {
        if (primer != null && drawMode == GL11.GL_QUADS && IAnimatedSpritePrimer.PRIMED.get()) {
            if (++vertexCountOfQuad == 4) {
                vertexCountOfQuad = 0;
                primer.addAnimatedSprite(minUOfQuad, minVOfQuad);
                minUOfQuad = Float.MAX_VALUE;
                minVOfQuad = Float.MAX_VALUE;
            }
            if (minUOfQuad > fu) {
                minUOfQuad = fu;
            }
            if (minVOfQuad > fv) {
                minVOfQuad = fv;
            }
        }
    }
}
