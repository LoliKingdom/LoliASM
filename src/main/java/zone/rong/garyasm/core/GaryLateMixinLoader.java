package zone.rong.garyasm.core;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.common.Loader;
import zone.rong.garyasm.config.GaryConfig;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

public class GaryLateMixinLoader implements ILateMixinLoader {

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
                "mixins.searchtree_mod.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        switch (mixinConfig) {
            case "mixins.bakedquadsquasher.json":
                return GaryTransformer.squashBakedQuads;
            case "mixins.modfixes_immersiveengineering.json":
                return GaryConfig.instance.fixBlockIEBaseArrayIndexOutOfBoundsException && Loader.isModLoaded("immersiveengineering");
            case "mixins.modfixes_evilcraftcompat.json":
                return GaryConfig.instance.repairEvilCraftEIOCompat && Loader.isModLoaded("evilcraftcompat") && Loader.isModLoaded("enderio") &&
                        Loader.instance().getIndexedModList().get("enderio").getVersion().equals("5.3.70"); // Only apply on newer EIO versions where compat was broken
            case "mixins.modfixes_ebwizardry.json":
                return GaryConfig.instance.optimizeArcaneLockRendering && Loader.isModLoaded("ebwizardry");
            case "mixins.modfixes_xu2.json":
                return (GaryConfig.instance.fixXU2CrafterCrash || GaryConfig.instance.disableXU2CrafterRendering) && Loader.isModLoaded("extrautils2");
            case "mixins.searchtree_mod.json":
                return GaryConfig.instance.replaceSearchTreeWithJEISearching && Loader.isModLoaded("jei");
            case "mixins.modfixes_astralsorcery.json":
                return GaryConfig.instance.optimizeAmuletRelatedFunctions && Loader.isModLoaded("astralsorcery");
            case "mixins.capability_astralsorcery.json":
                return GaryConfig.instance.fixAmuletHolderCapability && Loader.isModLoaded("astralsorcery");
            case "mixins.modfixes_b3m.json":
                return GaryConfig.instance.resourceLocationCanonicalization && Loader.isModLoaded("B3M"); // Stupid
        }
        return false;
    }

}
