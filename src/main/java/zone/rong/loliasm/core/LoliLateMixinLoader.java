package zone.rong.loliasm.core;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.common.Loader;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

public class LoliLateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Lists.newArrayList(
                "mixins.bakedquadsquasher.json",
                "mixins.modfixes_immersiveengineering.json",
                "mixins.modfixes_astralsorcery.json",
                "mixins.capability_astralsorcery.json",
                "mixins.modfixes_evilcraftcompat.json",
                "mixins.modfixes_ebwizardry.json",
                "mixins.modfixes_xu2.json",
                "mixins.modfixes_b3m.json",
                "mixins.searchtree_mod.json",
                "mixins.modfixes_railcraft.json",
                "mixins.modfixes_disable_broken_particles.json",
                "mixins.modfixes_crafttweaker.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        switch (mixinConfig) {
            case "mixins.bakedquadsquasher.json":
                return LoliTransformer.squashBakedQuads;
            case "mixins.modfixes_immersiveengineering.json":
                return LoliConfig.instance.fixBlockIEBaseArrayIndexOutOfBoundsException && Loader.isModLoaded("immersiveengineering");
            case "mixins.modfixes_evilcraftcompat.json":
                return LoliConfig.instance.repairEvilCraftEIOCompat && Loader.isModLoaded("evilcraftcompat") && Loader.isModLoaded("enderio") &&
                        Loader.instance().getIndexedModList().get("enderio").getVersion().equals("5.3.70"); // Only apply on newer EIO versions where compat was broken
            case "mixins.modfixes_ebwizardry.json":
                return LoliConfig.instance.optimizeArcaneLockRendering && Loader.isModLoaded("ebwizardry");
            case "mixins.modfixes_xu2.json":
                return (LoliConfig.instance.fixXU2CrafterCrash || LoliConfig.instance.disableXU2CrafterRendering) && Loader.isModLoaded("extrautils2");
            case "mixins.searchtree_mod.json":
                return LoliConfig.instance.replaceSearchTreeWithJEISearching && Loader.isModLoaded("jei");
            case "mixins.modfixes_astralsorcery.json":
                return LoliConfig.instance.optimizeAmuletRelatedFunctions && Loader.isModLoaded("astralsorcery");
            case "mixins.capability_astralsorcery.json":
                return LoliConfig.instance.fixAmuletHolderCapability && Loader.isModLoaded("astralsorcery");
            case "mixins.modfixes_b3m.json":
                return LoliConfig.instance.resourceLocationCanonicalization && Loader.isModLoaded("B3M"); // Stupid
            case "mixins.modfixes_railcraft.json":
                return LoliConfig.instance.efficientHashing && Loader.isModLoaded("railcraft");
            case "mixins.modfixes_disable_broken_particles.json":
                return LoliConfig.instance.disableBrokenParticles;
            case "mixins.modfixes_crafttweaker.json":
                boolean optimizeMap = LoliConfig.instance.optimizeNBTTagCompoundBackingMap;
                int mapThreshold = LoliConfig.instance.optimizeNBTTagCompoundMapThreshold;
                boolean canonicalizeString = LoliConfig.instance.nbtBackingMapStringCanonicalization;
                return ((optimizeMap && mapThreshold > 0) || canonicalizeString) && LoliConfig.instance.optimizeCraftTweakerNBTConverter && Loader.isModLoaded("crafttweaker");
        }
        return false;
    }

}
