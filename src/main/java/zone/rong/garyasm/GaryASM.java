package zone.rong.garyasm;

import codechicken.asm.ClassHierarchyManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import zone.rong.garyasm.api.mixins.RegistrySimpleExtender;
import zone.rong.garyasm.config.GaryConfig;
import zone.rong.garyasm.core.GaryLoadingPlugin;
import zone.rong.garyasm.proxy.CommonProxy;

import java.util.*;
import java.util.function.BiConsumer;

@Mod(modid = "garyasm", name = "GaryASM", version = GaryLoadingPlugin.VERSION, dependencies = "required-after:mixinbooter@[4.2,);after:jei;after:spark@[1.5.2]")
public class GaryASM {

    @SidedProxy(modId = "garyasm", clientSide = "zone.rong.garyasm.proxy.ClientProxy", serverSide = "zone.rong.garyasm.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static List<RegistrySimpleExtender> simpleRegistryInstances = new ArrayList<>();

    public static BiConsumer<TileEntity, NBTTagCompound> customTileDataConsumer;

    static {
        if (GaryConfig.instance.cleanupChickenASMClassHierarchyManager && GaryReflector.doesClassExist("codechicken.asm.ClassHierarchyManager")) {
            // EXPERIMENTAL: As far as I know, this functionality of ChickenASM isn't actually used by any coremods that depends on ChickenASM
            GaryLogger.instance.info("Replacing ClassHierarchyManager::superclasses with a dummy map.");
            ClassHierarchyManager.superclasses = new HashMap() {
                @Override
                public Object put(Object key, Object value) {
                    return value;
                }
            };
        }
    }

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        proxy.construct(event);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.throwIncompatibility();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.loadComplete(event);
    }

}
