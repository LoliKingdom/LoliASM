package zone.rong.loliasm.common.registries.mixins;

import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.registry.RegistrySimple;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;
import zone.rong.loliasm.LoliASM;
import zone.rong.loliasm.api.mixins.RegistrySimpleExtender;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

@Mixin(RegistrySimple.class)
public class RegistrySimpleMixin<K, V> implements RegistrySimpleExtender {

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Mutable @Final protected Map<K, V> registryObjects;

    /**
     * @author Rongmario
     * @reason Better map structure
     */
    @Overwrite
    protected Map<K, V> createUnderlyingMap() {
        if (LoliASM.simpleRegistryInstances != null) {
            LoliASM.simpleRegistryInstances.add(this);
        }
        return new Object2ReferenceOpenHashMap<>();
    }

    /**
     * @author Rongmario
     * @reason Remove values reference
     */
    @Overwrite
    public void putObject(K key, V value) {
        Validate.notNull(key);
        Validate.notNull(value);
        if (this.registryObjects.containsKey(key)) {
            LOGGER.debug("Adding duplicate key '{}' to registry", key);
        }
        this.registryObjects.put(key, value);
    }

    /**
     * @author Rongmario
     * @reason Remove values reference + use streams to gather a random object
     */
    @Nullable
    @Overwrite
    public V getRandomObject(Random random) {
        return this.registryObjects.values().stream().skip(random.nextInt(this.registryObjects.size())).findFirst().get();
    }

    @Override
    public void clearUnderlyingMap() {
        this.registryObjects.clear();
    }

    @Override
    public void trim() {
        if (this.registryObjects instanceof Object2ReferenceOpenHashMap) {
            if (this.registryObjects.size() > 32) {
                ((Object2ReferenceOpenHashMap<K, V>) this.registryObjects).trim();
            } else {
                this.registryObjects = new Object2ReferenceArrayMap<>(this.registryObjects);
            }
        }
    }

}
