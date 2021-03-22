package zone.rong.loliasm.patches.visualization;

import com.google.common.annotations.Beta;
import me.nallar.whocalled.WhoCalled;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zone.rong.loliasm.LoliLogger;

/**
 * A modified BakedQuad class - meant to be a *DIRECT* patch.
 */
@SideOnly(Side.CLIENT)
public abstract class BakedQuad implements net.minecraftforge.client.model.pipeline.IVertexProducer {
    /**
     * Joined 4 vertex records, each stores packed data according to the VertexFormat of the quad. Vanilla minecraft
     * uses DefaultVertexFormats.BLOCK, Forge uses (usually) ITEM, use BakedQuad.getFormat() to get the correct format.
     */
    protected int[] vertexData;
    protected TextureAtlasSprite sprite;
    protected VertexFormat format;

    /**
     * @deprecated Use constructor with the format argument.
     */
    @Deprecated
    public BakedQuad(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn) {
        this(vertexDataIn, tintIndexIn, faceIn, spriteIn, true, net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM);
        Class callee = WhoCalled.$.getCallingClass(1);
        if (callee == BakedQuad.class) {
            callee = WhoCalled.$.getCallingClass(2);
        }
        LoliLogger.instance.warn("{} needs their BakedQuad calls redirecting! They are using an already deprecated constructor. Insert the string into config/loliasm.json and report to Rongmario.", callee);
    }

    @SuppressWarnings("unused")
    @Deprecated
    public BakedQuad(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn, boolean applyDiffuseLighting, net.minecraft.client.renderer.vertex.VertexFormat format) {
        this.format = format;
        this.vertexData = vertexDataIn;
        this.sprite = spriteIn;
        Class callee = WhoCalled.$.getCallingClass(1);
        if (callee == BakedQuad.class) {
            callee = WhoCalled.$.getCallingClass(2);
        }
        LoliLogger.instance.warn("{} needs their BakedQuad calls redirecting! Insert the string into config/loliasm.json and report to Rongmario.", callee);
    }

    // For UnpackedBakedQuad
    @Beta
    public BakedQuad(int[] vertexData, TextureAtlasSprite sprite, VertexFormat format) {
        this.vertexData = vertexData;
        this.sprite = sprite;
        this.format = format;
    }

    @Beta
    public BakedQuad() { }

    public TextureAtlasSprite getSprite() {
        return this.sprite;
    }

    public int[] getVertexData() {
        return this.vertexData;
    }

    public abstract boolean hasTintIndex();

    public abstract int getTintIndex();

    public abstract EnumFacing getFace();

    @Override
    public void pipe(net.minecraftforge.client.model.pipeline.IVertexConsumer consumer) {
        net.minecraftforge.client.model.pipeline.LightUtil.putBakedQuad(consumer, (net.minecraft.client.renderer.block.model.BakedQuad) (Object) this);
    }

    public net.minecraft.client.renderer.vertex.VertexFormat getFormat() {
        return format;
    }

    public abstract boolean shouldApplyDiffuseLighting();
}

/*
package net.minecraft.client.renderer.block.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class BakedQuad implements net.minecraftforge.client.model.pipeline.IVertexProducer {

    protected int[] vertexData;
    protected byte tintIndex;
    protected EnumFacing face;
    protected TextureAtlasSprite sprite;


    @Deprecated
    public BakedQuad(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn) {
        this(vertexDataIn, tintIndexIn, faceIn, spriteIn, true, net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM);
    }

    public BakedQuad(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn, TextureAtlasSprite spriteIn, boolean applyDiffuseLighting, net.minecraft.client.renderer.vertex.VertexFormat format) {
        this.format = format;
        this.applyDiffuseLighting = applyDiffuseLighting;
        this.vertexData = vertexDataIn;
        this.tintIndex = (byte) tintIndexIn;
        this.face = faceIn;
        this.sprite = spriteIn;
    }

    public BakedQuad() { }

    public TextureAtlasSprite getSprite() {
        return this.sprite;
    }

    public int[] getVertexData() {
        return this.vertexData;
    }

    public boolean hasTintIndex() {
        return this.tintIndex != -1;
    }

    public int getTintIndex() {
        return this.tintIndex;
    }

    public EnumFacing getFace() {
        return this.face;
    }

    protected net.minecraft.client.renderer.vertex.VertexFormat format;
    protected boolean applyDiffuseLighting;

    @Override
    public void pipe(net.minecraftforge.client.model.pipeline.IVertexConsumer consumer) {
        net.minecraftforge.client.model.pipeline.LightUtil.putBakedQuad(consumer, this);
    }

    public net.minecraft.client.renderer.vertex.VertexFormat getFormat() {
        return format;
    }

    public boolean shouldApplyDiffuseLighting() {
        return applyDiffuseLighting;
    }
}

 */