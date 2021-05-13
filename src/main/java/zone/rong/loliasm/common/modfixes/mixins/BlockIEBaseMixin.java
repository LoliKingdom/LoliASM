package zone.rong.loliasm.common.modfixes.mixins;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockIEBase.class)
public abstract class BlockIEBaseMixin {

    @Shadow public abstract int getMetaFromState(IBlockState state);
    @Shadow(remap = false) protected EnumPushReaction[] metaMobilityFlags;

    /**
     * @author Rongmario
     * @reason Fixes ArrayIndexOutOfBoundsException
     */
    @Overwrite
    public EnumPushReaction getPushReaction(IBlockState state) {
        int meta = getMetaFromState(state);
        if (metaMobilityFlags.length <= meta || metaMobilityFlags[meta] == null) {
            return EnumPushReaction.NORMAL;
        }
        return metaMobilityFlags[meta];
    }

}
