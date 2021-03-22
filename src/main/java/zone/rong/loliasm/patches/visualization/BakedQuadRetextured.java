package zone.rong.loliasm.patches.visualization;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public class BakedQuadRetextured extends BakedQuad {

    private final BakedQuad quad;

    public BakedQuadRetextured(BakedQuad quad, TextureAtlasSprite textureIn) {
        super(quad.vertexData.clone(), textureIn, quad.getFormat());
        this.quad = quad;
        for (int i = 0; i < 4; ++i) {
            int j = format.getIntegerSize() * i;
            int uvIndex = format.getUvOffsetById(0) / 4;
            this.vertexData[j + uvIndex] = Float.floatToRawIntBits(textureIn.getInterpolatedU(quad.getSprite().getUnInterpolatedU(Float.intBitsToFloat(this.vertexData[j + uvIndex]))));
            this.vertexData[j + uvIndex + 1] = Float.floatToRawIntBits(textureIn.getInterpolatedV(quad.getSprite().getUnInterpolatedV(Float.intBitsToFloat(this.vertexData[j + uvIndex + 1]))));
        }
    }

    @Override
    public boolean hasTintIndex() {
        return quad.hasTintIndex();
    }

    @Override
    public int getTintIndex() {
        return quad.getTintIndex();
    }

    @Override
    public EnumFacing getFace() {
        return quad.getFace();
    }

    @Override
    public boolean shouldApplyDiffuseLighting() {
        return quad.shouldApplyDiffuseLighting();
    }
}
