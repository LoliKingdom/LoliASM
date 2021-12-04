package zone.rong.loliasm.common.modfixes.ebwizardry;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.tileentity.TileEntity;

import java.lang.ref.WeakReference;

public class ArcaneLocks {

    public static final ObjectOpenHashSet<WeakReference<TileEntity>> ARCANE_LOCKED_TILES = new ObjectOpenHashSet<>();

}
