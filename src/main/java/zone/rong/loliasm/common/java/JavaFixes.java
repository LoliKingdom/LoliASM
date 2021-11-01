package zone.rong.loliasm.common.java;

import com.google.common.base.Stopwatch;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.LoliReflector;
import zone.rong.loliasm.api.LoliStringPool;

import java.lang.invoke.MethodHandle;
import java.security.*;
import java.util.Enumeration;
import java.util.HashMap;

@SuppressWarnings("all")
public class JavaFixes {

    private static final MethodHandle SECURECLASSLOADER$PDCACHE$GETTER;
    private static final MethodHandle PERMISSION$NAME$SETTER;

    static {
        MethodHandle secureClassLoader$pdcache$getter = null;
        MethodHandle permission$name$setter = null;
        try {
            secureClassLoader$pdcache$getter = LoliReflector.resolveFieldGetter(SecureClassLoader.class, "pdcache");
            permission$name$setter = LoliReflector.resolveFieldSetter(Permission.class, "name");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SECURECLASSLOADER$PDCACHE$GETTER = secureClassLoader$pdcache$getter;
        PERMISSION$NAME$SETTER = permission$name$setter;
    }

    public static final JavaFixes INSTANCE = new JavaFixes();

    private JavaFixes() {
        run();
    }


    private void run() {
        try {
            LoliStringPool.establishPool(LoliStringPool.FILE_PERMISSIONS_ID, 512);
            Stopwatch stopwatch = Stopwatch.createStarted();
            HashMap<CodeSource, ProtectionDomain> pdcache = (HashMap<CodeSource, ProtectionDomain>) SECURECLASSLOADER$PDCACHE$GETTER.invoke(Launch.classLoader);
            for (ProtectionDomain pd : pdcache.values()) {
                PermissionCollection pc = pd.getPermissions();
                if (pc != null) {
                    Enumeration<Permission> perms = pc.elements();
                    while (perms.hasMoreElements()) {
                        Permission perm = perms.nextElement();
                        PERMISSION$NAME$SETTER.invokeExact(perm, LoliStringPool.canonicalize(perm.getName()));
                    }
                }
            }
            LoliLogger.instance.info("Took {} to canonicalize Java's FilePermission caches.", stopwatch.stop());
            LoliStringPool.purgePool(LoliStringPool.FILE_PERMISSIONS_ID);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        run();
    }

}
