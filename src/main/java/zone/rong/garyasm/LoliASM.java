package zone.rong.loliasm;

import codechicken.asm.ClassHierarchyManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import zone.rong.loliasm.api.mixins.RegistrySimpleExtender;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.core.LoliLoadingPlugin;
import zone.rong.loliasm.proxy.CommonProxy;

import java.util.*;
import java.util.function.BiConsumer;

@Mod(modid = "loliasm", name = "LoliASM", version = LoliLoadingPlugin.VERSION, dependencies = "required-after:mixinbooter@[4.2,);after:jei;after:spark@[1.5.2]")
public class LoliASM {

    @SidedProxy(modId = "loliasm", clientSide = "zone.rong.loliasm.proxy.ClientProxy", serverSide = "zone.rong.loliasm.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static List<RegistrySimpleExtender> simpleRegistryInstances = new ArrayList<>();

    public static BiConsumer<TileEntity, NBTTagCompound> customTileDataConsumer;

    static {
        if (LoliConfig.instance.cleanupChickenASMClassHierarchyManager && LoliReflector.doesClassExist("codechicken.asm.ClassHierarchyManager")) {
            // EXPERIMENTAL: As far as I know, this functionality of ChickenASM isn't actually used by any coremods that depends on ChickenASM
            LoliLogger.instance.info("Replacing ClassHierarchyManager::superclasses with a dummy map.");
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
