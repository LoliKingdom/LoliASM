package zone.rong.loliasm.bakedquad.variants;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import zone.rong.loliasm.patches.visualization.BakedQuad;

@Deprecated
@MethodsReturnNonnullByDefault
public class Untinted {

    public static class BakedQuad_DownFace_ApplyDiffuseLighting extends BakedQuad {

        public BakedQuad_DownFace_ApplyDiffuseLighting(int[] vertexDataIn, TextureAtlasSprite spriteIn, VertexFormat format) {
            super(vertexDataIn, spriteIn, format);
        }

        @Override
        public EnumFacing getFace() {
            return EnumFacing.DOWN;
        }

        @Override
        public boolean shouldApplyDiffuseLighting() {
            return true;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        @Override
        public int[] getVertexData() {
            return vertexData;
        }

        @Override
        public boolean hasTintIndex() {
            return false;
        }

        @Override
        public int getTintIndex() {
            return -1;
        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }

    public static class BakedQuad_UpFace_ApplyDiffuseLighting extends BakedQuad {

        public BakedQuad_UpFace_ApplyDiffuseLighting(int[] vertexDataIn, TextureAtlasSprite spriteIn, VertexFormat format) {
            super(vertexDataIn, spriteIn, format);
        }

        @Override
        public EnumFacing getFace() {
            return EnumFacing.UP;
        }

        @Override
        public boolean shouldApplyDiffuseLighting() {
            return true;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        @Override
        public int[] getVertexData() {
            return vertexData;
        }

        @Override
        public boolean hasTintIndex() {
            return false;
        }

        @Override
        public int getTintIndex() {
            return -1;
        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }

    public static class BakedQuad_NorthFace_ApplyDiffuseLighting extends BakedQuad {

        public BakedQuad_NorthFace_ApplyDiffuseLighting(int[] vertexDataIn, TextureAtlasSprite spriteIn, VertexFormat format) {
            super(vertexDataIn, spriteIn, format);
        }

        @Override
        public EnumFacing getFace() {
            return EnumFacing.NORTH;
        }

        @Override
        public boolean shouldApplyDiffuseLighting() {
            return true;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        @Override
        public int[] getVertexData() {
            return vertexData;
        }

        @Override
        public boolean hasTintIndex() {
            return false;
        }

        @Override
        public int getTintIndex() {
            return -1;
        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }

    public static class BakedQuad_SouthFace_ApplyDiffuseLighting extends BakedQuad {

        public BakedQuad_SouthFace_ApplyDiffuseLighting(int[] vertexDataIn, TextureAtlasSprite spriteIn, VertexFormat format) {
            super(vertexDataIn, spriteIn, format);
        }

        @Override
        public EnumFacing getFace() {
            return EnumFacing.SOUTH;
        }

        @Override
        public boolean shouldApplyDiffuseLighting() {
            return true;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        @Override
        public int[] getVertexData() {
            return vertexData;
        }

        @Override
        public boolean hasTintIndex() {
            return false;
        }

        @Override
        public int getTintIndex() {
            return -1;
        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }

    public static class BakedQuad_WestFace_ApplyDiffuseLighting extends BakedQuad {

        public BakedQuad_WestFace_ApplyDiffuseLighting(int[] vertexDataIn, TextureAtlasSprite spriteIn, VertexFormat format) {
            super(vertexDataIn, spriteIn, format);
        }

        @Override
        public EnumFacing getFace() {
            return EnumFacing.WEST;
        }

        @Override
        public boolean shouldApplyDiffuseLighting() {
            return true;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        @Override
        public int[] getVertexData() {
            return vertexData;
        }

        @Override
        public boolean hasTintIndex() {
            return false;
        }

        @Override
        public int getTintIndex() {
            return -1;
        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }

    public static class BakedQuad_EastFace_ApplyDiffuseLighting extends BakedQuad {

        public BakedQuad_EastFace_ApplyDiffuseLighting(int[] vertexDataIn, TextureAtlasSprite spriteIn, VertexFormat format) {
            super(vertexDataIn, spriteIn, format);
        }

        @Override
        public EnumFacing getFace() {
            return EnumFacing.EAST;
        }

        @Override
        public boolean shouldApplyDiffuseLighting() {
            return true;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        @Override
        public int[] getVertexData() {
            return vertexData;
        }

        @Override
        public boolean hasTintIndex() {
            return false;
        }

        @Override
        public int getTintIndex() {
            return -1;
        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }

    public static class BakedQuad_DownFace_DontApplyDiffuseLighting extends BakedQuad {

        public BakedQuad_DownFace_DontApplyDiffuseLighting(int[] vertexDataIn, TextureAtlasSprite spriteIn, VertexFormat format) {
            super(vertexDataIn, spriteIn, format);
        }

        @Override
        public EnumFacing getFace() {
            return EnumFacing.DOWN;
        }

        @Override
        public boolean shouldApplyDiffuseLighting() {
            return false;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        @Override
        public int[] getVertexData() {
            return vertexData;
        }

        @Override
        public boolean hasTintIndex() {
            return false;
        }

        @Override
        public int getTintIndex() {
            return -1;
        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }

    public static class BakedQuad_UpFace_DontApplyDiffuseLighting extends BakedQuad {

        public BakedQuad_UpFace_DontApplyDiffuseLighting(int[] vertexDataIn, TextureAtlasSprite spriteIn, VertexFormat format) {
            super(vertexDataIn, spriteIn, format);
        }

        @Override
        public EnumFacing getFace() {
            return EnumFacing.UP;
        }

        @Override
        public boolean shouldApplyDiffuseLighting() {
            return false;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        @Override
        public int[] getVertexData() {
            return vertexData;
        }

        @Override
        public boolean hasTintIndex() {
            return false;
        }

        @Override
        public int getTintIndex() {
            return -1;
        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }

    public static class BakedQuad_NorthFace_DontApplyDiffuseLighting extends BakedQuad {

        public BakedQuad_NorthFace_DontApplyDiffuseLighting(int[] vertexDataIn, TextureAtlasSprite spriteIn, VertexFormat format) {
            super(vertexDataIn, spriteIn, format);
        }

        @Override
        public EnumFacing getFace() {
            return EnumFacing.NORTH;
        }

        @Override
        public boolean shouldApplyDiffuseLighting() {
            return false;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        @Override
        public int[] getVertexData() {
            return vertexData;
        }

        @Override
        public boolean hasTintIndex() {
            return false;
        }

        @Override
        public int getTintIndex() {
            return -1;
        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }

    public static class BakedQuad_SouthFace_DontApplyDiffuseLighting extends BakedQuad {

        public BakedQuad_SouthFace_DontApplyDiffuseLighting(int[] vertexDataIn, TextureAtlasSprite spriteIn, VertexFormat format) {
            super(vertexDataIn, spriteIn, format);
        }

        @Override
        public EnumFacing getFace() {
            return EnumFacing.SOUTH;
        }

        @Override
        public boolean shouldApplyDiffuseLighting() {
            return false;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        @Override
        public int[] getVertexData() {
            return vertexData;
        }

        @Override
        public boolean hasTintIndex() {
            return false;
        }

        @Override
        public int getTintIndex() {
            return -1;
        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }

    public static class BakedQuad_WestFace_DontApplyDiffuseLighting extends BakedQuad {

        public BakedQuad_WestFace_DontApplyDiffuseLighting(int[] vertexDataIn, TextureAtlasSprite spriteIn, VertexFormat format) {
            super(vertexDataIn, spriteIn, format);
        }

        @Override
        public EnumFacing getFace() {
            return EnumFacing.WEST;
        }

        @Override
        public boolean shouldApplyDiffuseLighting() {
            return false;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        @Override
        public int[] getVertexData() {
            return vertexData;
        }

        @Override
        public boolean hasTintIndex() {
            return false;
        }

        @Override
        public int getTintIndex() {
            return -1;
        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }

    public static class BakedQuad_EastFace_DontApplyDiffuseLighting extends BakedQuad {

        public BakedQuad_EastFace_DontApplyDiffuseLighting(int[] vertexDataIn, TextureAtlasSprite spriteIn, VertexFormat format) {
            super(vertexDataIn, spriteIn, format);
        }

        @Override
        public EnumFacing getFace() {
            return EnumFacing.EAST;
        }

        @Override
        public boolean shouldApplyDiffuseLighting() {
            return false;
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        @Override
        public int[] getVertexData() {
            return vertexData;
        }

        @Override
        public boolean hasTintIndex() {
            return false;
        }

        @Override
        public int getTintIndex() {
            return -1;
        }

        @Override
        public VertexFormat getFormat() {
            return format;
        }
    }

}
