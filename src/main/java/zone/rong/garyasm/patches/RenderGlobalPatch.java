package zone.rong.garyasm.patches;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.chunk.Chunk;

@SuppressWarnings("unused")
public class RenderGlobalPatch {
    /**
     * Minecraft is transformed to call this method when it tries to do culling checks. Return a bounding box that
     * includes all entities, so culling works as it should.
     */
    public static AxisAlignedBB getCorrectBoundingBox(RenderChunk renderChunk) {
        int subChunkYPos = renderChunk.getPosition().getY();
        Chunk chunk = renderChunk.getWorld().getChunk(renderChunk.getPosition());
        int subChunkList = subChunkYPos / 16;
        /* Make sure there is actually going to be an entity list, to be on the safe side */
        if (subChunkList < 0 || subChunkList >= chunk.getEntityLists().length) {
            return renderChunk.boundingBox;
        }
        ClassInheritanceMultiMap<Entity> entityMap = chunk.getEntityLists()[subChunkList];
        if (!entityMap.isEmpty()) {
            /* Use local variables to avoid GC churn during rendering */
            double minX = renderChunk.boundingBox.minX, minY = renderChunk.boundingBox.minY, minZ = renderChunk.boundingBox.minZ;
            double maxX = renderChunk.boundingBox.maxX, maxY = renderChunk.boundingBox.maxY, maxZ = renderChunk.boundingBox.maxZ;
            for (Entity entity : entityMap) {
                AxisAlignedBB entityBox = entity.getRenderBoundingBox();
                minX = Math.min(minX, entityBox.minX - 0.5);
                minY = Math.min(minY, entityBox.minY - 0.5);
                minZ = Math.min(minZ, entityBox.minZ - 0.5);
                maxX = Math.max(maxX, entityBox.maxX + 0.5);
                maxY = Math.max(maxY, entityBox.maxY + 0.5);
                maxZ = Math.max(maxZ, entityBox.maxZ + 0.5);
            }
            return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return renderChunk.boundingBox;
    }
}
