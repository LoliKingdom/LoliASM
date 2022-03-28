package zone.rong.loliasm.common.modfixes.xu2.mixins;

import com.rwtema.extrautils2.itemhandler.SingleStackHandler;
import com.rwtema.extrautils2.tile.TileCrafter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileCrafter.class)
public class TileCrafterMixin {

    @Shadow @Final private SingleStackHandler ghostOutput;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/state/IBlockState;"))
    private IBlockState getContextualState(Block unused) {
        ItemStack stack = this.ghostOutput.getStack();
        return Block.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getMetadata());
    }

}
