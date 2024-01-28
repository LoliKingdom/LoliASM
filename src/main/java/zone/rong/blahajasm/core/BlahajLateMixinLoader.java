package zone.rong.blahajasm.core;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.common.Loader;
import zone.rong.blahajasm.config.BlahajConfig;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;

public class BlahajLateMixinLoader implements ILateMixinLoader {

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
                return BlahajTransformer.squashBakedQuads;
            case "mixins.modfixes_immersiveengineering.json":
                return BlahajConfig.instance.fixBlockIEBaseArrayIndexOutOfBoundsException && Loader.isModLoaded("immersiveengineering");
            case "mixins.modfixes_evilcraftcompat.json":
                return BlahajConfig.instance.repairEvilCraftEIOCompat && Loader.isModLoaded("evilcraftcompat") && Loader.isModLoaded("enderio") &&
                        Loader.instance().getIndexedModList().get("enderio").getVersion().equals("5.3.70"); // Only apply on newer EIO versions where compat was broken
            case "mixins.modfixes_ebwizardry.json":
                return BlahajConfig.instance.optimizeArcaneLockRendering && Loader.isModLoaded("ebwizardry");
            case "mixins.modfixes_xu2.json":
                return (BlahajConfig.instance.fixXU2CrafterCrash || BlahajConfig.instance.disableXU2CrafterRendering) && Loader.isModLoaded("extrautils2");
            case "mixins.searchtree_mod.json":
                return BlahajConfig.instance.replaceSearchTreeWithJEISearching && Loader.isModLoaded("jei");
            case "mixins.modfixes_astralsorcery.json":
                return BlahajConfig.instance.optimizeAmuletRelatedFunctions && Loader.isModLoaded("astralsorcery");
            case "mixins.capability_astralsorcery.json":
                return BlahajConfig.instance.fixAmuletHolderCapability && Loader.isModLoaded("astralsorcery");
            case "mixins.modfixes_b3m.json":
                return BlahajConfig.instance.resourceLocationCanonicalization && Loader.isModLoaded("B3M"); // Stupid
        }
        return false;
    }

}
