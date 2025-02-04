package zone.rong.garyasm.common.internal.mixins;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.garyasm.GaryASM;

@SideOnly(Side.CLIENT)
@Mixin(TileEntity.class)
public class TileEntityMixin {

    @Shadow(remap = false) private NBTTagCompound customTileData;

    @Inject(method = "readFromNBT",
            at = @At(value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/nbt/NBTTagCompound;getCompoundTag(Ljava/lang/String;)Lnet/minecraft/nbt/NBTTagCompound;",
                    ordinal = 0,
                    shift = Shift.AFTER,
                    by = 2))
    private void afterReadingFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        if (GaryASM.customTileDataConsumer != null) {
            GaryASM.customTileDataConsumer.accept((TileEntity) (Object) this, customTileData);
        }
    }

}
