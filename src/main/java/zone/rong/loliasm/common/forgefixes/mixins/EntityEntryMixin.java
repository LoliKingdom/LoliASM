package zone.rong.loliasm.common.forgefixes.mixins;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.LoliReflector;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.function.Function;

@Mixin(value = EntityEntry.class, remap = false)
public class EntityEntryMixin {

    @Unique private static Map<ClassLoader, MethodHandles.Lookup> altLookups;

    @Shadow private Class<? extends Entity> cls;
    @Shadow Function<World, ? extends Entity> factory;

    /**
     * @author Rongmario
     * @reason LambdaMetafactory constructed Function::apply instead of doing Constructor::newInstance
     */
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void captureInit(CallbackInfo ci) {
        try {
            MethodHandles.Lookup lookup = LoliReflector.LOOKUP;
            ClassLoader classLoader = this.cls.getClassLoader();
            if (classLoader != Launch.classLoader) {
                if (altLookups == null) {
                    altLookups = new Reference2ReferenceArrayMap<>();
                }
                lookup = altLookups.get(classLoader);
                if (lookup == null) {
                    LoliLogger.instance.warn("Building a new MethodHandle::Lookup for ClassLoader: {} with intentions of building a faster Entity constructor replacement", classLoader);
                    altLookups.put(classLoader, lookup = LoliReflector.getCtor(MethodHandles.Lookup.class, Class.class).newInstance(this.cls));
                }
            }
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    LoliReflector.resolveCtor(this.cls, World.class),
                    MethodType.methodType(this.cls, World.class));
            this.factory = (Function) callSite.getTarget().invokeExact();
            ci.cancel();
        } catch (Throwable t) {
            LoliLogger.instance.warn("Could not establish a faster Entity constructor replacement for {}", this.cls, t);
        }
    }

}
