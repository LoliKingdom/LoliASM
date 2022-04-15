package zone.rong.loliasm.common.modfixes.ebwizardry.mixins;

import electroblob.wizardry.spell.ArcaneLock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.common.modfixes.ebwizardry.ArcaneLocks;

import java.lang.ref.WeakReference;

@SideOnly(Side.CLIENT)
@Mixin(TileEntity.class)
public class TileEntityMixin {

    @Shadow private NBTTagCompound customTileData;

    @Unique private boolean hasArcaneLock = false;

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void afterReadingFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        if (this.customTileData.hasUniqueId(ArcaneLock.NBT_KEY)) {
            hasArcaneLock = true;
            ArcaneLocks.ARCANE_LOCKED_TILES.add(new WeakReference<>((TileEntity) (Object) this));
        } else if (hasArcaneLock) {
            hasArcaneLock = false;
            ArcaneLocks.ARCANE_LOCKED_TILES.rem(new WeakReference<>((TileEntity) (Object) this));
        }
    }

}
