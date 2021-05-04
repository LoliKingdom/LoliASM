package zone.rong.loliasm.client.models;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * {@link com.google.common.base.Suppliers}
 */
public class ModelRegistry implements IRegistry<ModelResourceLocation, IBakedModel> {

    // private final Map<ModelResourceLocation, Supplier<IBakedModel>> backingMap;
    // private final Map<>

    public ModelRegistry() {
        // this.backingMap = new Object2ObjectOpenHashMap<>(64);
    }

    @Nullable
    @Override
    public IBakedModel getObject(ModelResourceLocation name) {
        return null;
    }

    @Override
    public void putObject(ModelResourceLocation key, IBakedModel value) {

    }

    @Override
    public Set<ModelResourceLocation> getKeys() {
        return null;
    }

    @Override
    public Iterator<IBakedModel> iterator() {
        return null;
    }

}
