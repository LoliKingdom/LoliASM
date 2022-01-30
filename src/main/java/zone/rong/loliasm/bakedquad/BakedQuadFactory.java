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

    public static BakedQuad canonicalize(int[] vertexData, int tintIndex, EnumFacing face, TextureAtlasSprite sprite, boolean applyDiffuseLighting, VertexFormat format) {
        return prepare(LoliVertexDataPool.canonicalize(vertexData), tintIndex, face, sprite, applyDiffuseLighting, format);
    }

    public static BakedQuad prepare(int[] vertexData, int tintIndex, EnumFacing face, TextureAtlasSprite sprite, boolean applyDiffuseLighting, VertexFormat format) {
        if (face == null) {
            // AE2 AutoRotatingModel applies null faces in certain contexts, not going to optimize these
            return new BakedQuad(vertexData, tintIndex, face, sprite, applyDiffuseLighting, format);
        }
        return create(vertexData, tintIndex, face, sprite, applyDiffuseLighting, format);
    }

    /**
     * This method is here as a placeholder. The contents of this method has to be dynamically written at runtime.
     */
    public static BakedQuad create(int[] vertexData, int tintIndex, EnumFacing face, TextureAtlasSprite sprite, boolean applyDiffuseLighting, VertexFormat format) {
        return new BakedQuad(vertexData, tintIndex, face, sprite, applyDiffuseLighting, format);
    }

    private BakedQuadFactory() { }

}
