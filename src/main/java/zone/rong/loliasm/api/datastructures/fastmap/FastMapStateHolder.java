package zone.rong.loliasm.api.datastructures.fastmap;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.properties.IProperty;

public interface FastMapStateHolder {

    FastMap getStateMap();

    void setStateMap(FastMap newValue);

    int getStateIndex();

    void setStateIndex(int newValue);

    ImmutableMap<IProperty<?>, Comparable<?>> getVanillaPropertyMap();

    void replacePropertyMap(ImmutableMap<IProperty<?>, Comparable<?>> newMap);

}
