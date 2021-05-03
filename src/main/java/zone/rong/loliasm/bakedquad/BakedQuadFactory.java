package zone.rong.loliasm.bakedquad;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import zone.rong.loliasm.core.LoliTransformer;

/**
 * This class aids the dispatches of BakedQuad instances. The create method is removed/patched in {@link LoliTransformer}
 */
public final class BakedQuadFactory {

    // This is to avoid creating a synthetic EnumFacing SwitchMap at compile time
    // The SwitchMap is literally just an int array or enum ordinal positions
    @SuppressWarnings("unused")
    private static final int[] pseudoSwitchMap = new int[] { 0, 1, 2, 3, 4, 5 };

    /**
     * This method is here as a placeholder, it will be removed at runtime. Otherwise the class would like quite blank, wouldn't it?
     * It gets replaced by a BakedQuadFactory::create that returns {@link net.minecraft.client.renderer.block.model.BakedQuad}
     */
    @SuppressWarnings("all")
    public static void create(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn, boolean applyDiffuseLighting, VertexFormat format) {
        throw new IllegalStateException("Lolis didn't come :(");
    }

    private BakedQuadFactory() { }

}
