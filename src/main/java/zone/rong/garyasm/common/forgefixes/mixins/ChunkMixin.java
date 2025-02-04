package zone.rong.loliasm.common.forgefixes.mixins;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Mixin(Chunk.class)
public class ChunkMixin {

    @Redirect(method = "onLoad", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;", remap = false))
    private Collection<TileEntity> copiedValues(Map<BlockPos, TileEntity> map) {
        return new ArrayList<>(map.values());
    }

}
