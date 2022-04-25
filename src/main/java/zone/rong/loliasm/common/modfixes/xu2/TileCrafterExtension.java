package zone.rong.loliasm.common.modfixes.xu2;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface TileCrafterExtension {

    @SideOnly(Side.CLIENT)
    void renderAlt(double x, double y, double z);

}
