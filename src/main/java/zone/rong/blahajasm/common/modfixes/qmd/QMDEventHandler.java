package zone.rong.blahajasm.common.modfixes.qmd;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zone.rong.blahajasm.BlahajReflector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QMDEventHandler {

    private static final List<WeakReference<Entity>> beamsInWorld = new ArrayList<>(1);
    private static final Class<?> projectileClass = BlahajReflector.getClass("lach_01298.qmd.entity.EntityBeamProjectile").get();

    @SubscribeEvent
    public static void onBeamSpawn(EntityJoinWorldEvent event) {
        if (projectileClass.isInstance(event.getEntity())) {
            beamsInWorld.add(new WeakReference<>(event.getEntity()));
        }
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if (beamsInWorld.isEmpty()) {
            return;
        }
        Iterator<WeakReference<Entity>> iterator = beamsInWorld.listIterator();
        float partialTicks = event.getPartialTicks();
        double bx = 0D, by = 0D, bz = 0D, rx = 0D, ry = 0D, rz = 0D;
        float br = 0F;
        boolean calculated = false;
        while (iterator.hasNext()) {
            Entity entity = iterator.next().get();
            if (entity == null || entity.isDead) {
                iterator.remove();
            } else {
                RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
                Entity renderEntity = renderManager.renderViewEntity;
                if (renderEntity.getDistanceSq(entity) <= (128 * 128)) {
                    if (!calculated) {
                        bx = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
                        by = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
                        bz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
                        br = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
                        rx = renderEntity.lastTickPosX + (renderEntity.posX - renderEntity.lastTickPosX) * (double) partialTicks;
                        ry = renderEntity.lastTickPosY + (renderEntity.posY - renderEntity.lastTickPosY) * (double) partialTicks;
                        rz = renderEntity.lastTickPosZ + (renderEntity.posZ - renderEntity.lastTickPosZ) * (double) partialTicks;
                        calculated = true;
                    }
                    renderManager.getEntityClassRenderObject(entity.getClass()).doRender(entity, bx - rx, by - ry, bz - rz, br, partialTicks);
                }
            }
        }
    }

}
