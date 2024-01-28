package zone.rong.blahajasm.common.modfixes.b3m;

import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public class CapitalizedNamespaceResourceLocation extends ResourceLocation {

    public CapitalizedNamespaceResourceLocation(String namespaceIn, String pathIn) {
        super(namespaceIn, pathIn);
    }

    @Override
    public String getNamespace() {
        return super.getNamespace().toUpperCase(Locale.ROOT);
    }

}
