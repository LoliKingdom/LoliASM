package zone.rong.loliasm.common.stripitemstack.mixins;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract boolean hasTagCompound();
    @Shadow private NBTTagCompound stackTagCompound;

    @Unique private static final Cache<ItemStack, Pair<Block, Boolean>> canPlaceCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();
    @Unique private static final Cache<ItemStack, Pair<Block, Boolean>> canDestroyCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();

    @Unique private static Map<ItemStack, EntityItemFrame> itemFrames;

    /**
     * @author Rongmario
     * @reason Use the global map
     */
    @Overwrite
    public boolean isOnItemFrame() {
        return itemFrames != null && itemFrames.containsKey(this);
    }

    /**
     * @author Rongmario
     * @reason Use the global map
     */
    @Overwrite
    public void setItemFrame(EntityItemFrame frame) {
        if (itemFrames == null) {
            itemFrames = new Reference2ReferenceOpenHashMap<>();
        }
        itemFrames.put((ItemStack) (Object) this, frame);
    }

    /**
     * @author Rongmario
     * @reason Use the global map
     */
    @Nullable
    @Overwrite
    public EntityItemFrame getItemFrame() {
        return itemFrames == null ? null : itemFrames.get(this);
    }

    /**
     * @author Rongmario
     * @reason Use the global map
     *
     * TODO: not have a pair value cache but just have a Block value
     */
    @Overwrite
    public boolean canPlaceOn(Block blockIn) {
        Pair<Block, Boolean> placeInfo = canPlaceCache.getIfPresent(this);
        if (placeInfo != null && placeInfo.getLeft() == blockIn) {
            return placeInfo.getRight();
        }
        if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanPlaceOn", 9)) {
            NBTTagList nbttaglist = this.stackTagCompound.getTagList("CanPlaceOn", 8);
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                Block block = Block.getBlockFromName(nbttaglist.getStringTagAt(i));
                if (block == blockIn) {
                    canPlaceCache.put((ItemStack) (Object) this, Pair.of(blockIn, true));
                    return true;
                }
            }
        }
        canPlaceCache.put((ItemStack) (Object) this, Pair.of(blockIn, false));
        return false;
    }

    /**
     * @author Rongmario
     * @reason Use the global map
     *
     * TODO: not have a pair value cache but just have a Block value
     */
    @Overwrite
    public boolean canDestroy(Block blockIn) {
        Pair<Block, Boolean> destroyInfo = canDestroyCache.getIfPresent(this);
        if (destroyInfo != null && destroyInfo.getLeft() == blockIn) {
            return destroyInfo.getRight();
        }
        if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanDestroy", 9)) {
            NBTTagList nbttaglist = this.stackTagCompound.getTagList("CanDestroy", 8);
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                Block block = Block.getBlockFromName(nbttaglist.getStringTagAt(i));
                if (block == blockIn) {
                    canDestroyCache.put((ItemStack) (Object) this, Pair.of(blockIn, true));
                    return true;
                }
            }
        }
        canDestroyCache.put((ItemStack) (Object) this, Pair.of(blockIn, false));
        return false;
    }

}
