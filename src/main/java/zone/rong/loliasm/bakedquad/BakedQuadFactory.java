package zone.rong.loliasm.bakedquad;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import zone.rong.loliasm.core.LoliTransformer;

/**
 * This class aids the dispatches of BakedQuad instances. The create method is removed/patched in {@link LoliTransformer}
 */
@SuppressWarnings("unused")
public final class BakedQuadFactory {

    // This is to avoid creating a synthetic EnumFacing SwitchMap at compile time
    // The SwitchMap is literally just an int array or enum ordinal positions
    private static final int[] pseudoSwitchMap = new int[] { 0, 1, 2, 3, 4, 5 };

    // TODO:
    /*
    private static final ObjectOpenCustomHashSet<int[]> DUPES = new ObjectOpenCustomHashSet<>(1024, IntArrays.HASH_STRATEGY);

    public static BakedQuad canonize(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn, boolean applyDiffuseLighting, VertexFormat format) {
        return create(DUPES.addOrGet(vertexDataIn), tintIndexIn, faceIn, spriteIn, applyDiffuseLighting, format);
    }
     */

    /**
     * This method is here as a placeholder. The contents of this method has to be dynamically written at runtime.
     */
    public static BakedQuad create(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn, boolean applyDiffuseLighting, VertexFormat format) {
        return new BakedQuad(vertexDataIn, tintIndexIn, faceIn, spriteIn, applyDiffuseLighting, format);
    }

    private BakedQuadFactory() { }

}
