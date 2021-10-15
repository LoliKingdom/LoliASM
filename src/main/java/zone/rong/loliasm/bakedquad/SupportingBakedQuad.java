package zone.rong.loliasm.bakedquad;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Made for classes that extends vanilla BakedQuad, since LoliASM patches a lot of it,
 * Some extending classes may use the protected variables that were removed.
 * This class is to be the bridging class that they extend on.
 */
@SideOnly(Side.CLIENT)
public class SupportingBakedQuad extends BakedQuad {

    protected final EnumFacing face;
    protected final boolean applyDiffuseLighting;
    protected final int tintIndex;

    public SupportingBakedQuad(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn, boolean applyDiffuseLighting, VertexFormat format) {
        super(vertexDataIn, tintIndexIn, faceIn, spriteIn, applyDiffuseLighting, format);
        this.face = faceIn;
        this.applyDiffuseLighting = applyDiffuseLighting;
        this.tintIndex = tintIndexIn;
    }

    public SupportingBakedQuad(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn) {
        this(vertexDataIn, tintIndexIn, faceIn, spriteIn, true, DefaultVertexFormats.ITEM);
    }

    @Override
    public EnumFacing getFace() {
        return face;
    }

    @Override
    public int getTintIndex() {
        return tintIndex;
    }

    @Override
    public boolean hasTintIndex() {
        return tintIndex != -1;
    }

    @Override
    public boolean shouldApplyDiffuseLighting() {
        return applyDiffuseLighting;
    }
}
