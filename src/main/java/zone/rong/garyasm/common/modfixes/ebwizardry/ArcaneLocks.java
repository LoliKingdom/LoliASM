package zone.rong.garyasm.common.modfixes.ebwizardry;

import electroblob.wizardry.spell.ArcaneLock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

public class ArcaneLocks {

    public static final Set<TileEntity> ARCANE_LOCKED_TILES;
    public static final BiConsumer<TileEntity, NBTTagCompound> TRACK_ARCANE_TILES;

    static {
        ARCANE_LOCKED_TILES = Collections.newSetFromMap(new WeakHashMap<>());
        TRACK_ARCANE_TILES = (tile, tag) -> {
            if (tag.hasUniqueId(ArcaneLock.NBT_KEY)) {
                ARCANE_LOCKED_TILES.add(tile);
            }
            ARCANE_LOCKED_TILES.remove(tile);
        };
    }

}
