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
                "mixins.modfixes_xu2.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        if ("mixins.bakedquadsquasher.json".equals(mixinConfig)) {
            return LoliTransformer.squashBakedQuads;
        }
        if ("mixins.modfixes_immersiveengineering.json".equals(mixinConfig)) {
            return LoliConfig.instance.fixBlockIEBaseArrayIndexOutOfBoundsException && Loader.isModLoaded("immersiveengineering");
        }
        if ("mixins.modfixes_evilcraftcompat.json".equals(mixinConfig)) {
            return LoliConfig.instance.repairEvilCraftEIOCompat && Loader.isModLoaded("evilcraftcompat");
        }
        if ("mixins.modfixes_ebwizardry.json".equals(mixinConfig)) {
            return LoliConfig.instance.optimizeArcaneLockRendering && Loader.isModLoaded("ebwizardry");
        }
        if ("mixins.modfixes_xu2.json".equals(mixinConfig)) {
            return (LoliConfig.instance.fixXU2CrafterCrash || LoliConfig.instance.disableXU2CrafterRendering) && Loader.isModLoaded("extrautils2");
        }
        if (Loader.isModLoaded("astralsorcery")) {
            if ("mixins.modfixes_astralsorcery.json".equals(mixinConfig)) {
                return LoliConfig.instance.optimizeAmuletRelatedFunctions;
            }
            if ("mixins.capability_astralsorcery.json".equals(mixinConfig)) {
                return LoliConfig.instance.fixAmuletHolderCapability;
            }
        }
        return false;
    }

}
