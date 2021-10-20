package zone.rong.loliasm.common.forgefixes.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.LoliReflector;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodType;
import java.util.function.Function;

@Mixin(value = EntityEntry.class, remap = false)
public class EntityEntryMixin {

    @Shadow private Class<? extends Entity> cls;
    @Shadow Function<World, ? extends Entity> factory;

    /**
     * @author Rongmario
     * @reason LambdaMetafactory constructed Function::apply instead of doing Constructor::newInstance
     */
    @Overwrite
    protected void init() {
        try {
            CallSite callSite = LambdaMetafactory.metafactory(
                    LoliReflector.LOOKUP,
                    "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    LoliReflector.resolveCtor(this.cls, World.class),
                    MethodType.methodType(this.cls, World.class));
            this.factory = (Function) callSite.getTarget().invokeExact();
        } catch (Throwable t) {
            LoliLogger.instance.error("LoliASM could not establish a faster Entity constructor replacement for {}", this.cls, t);
        }
    }

}
