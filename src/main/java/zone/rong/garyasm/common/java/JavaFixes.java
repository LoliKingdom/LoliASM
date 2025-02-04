package zone.rong.garyasm.common.java;

import com.google.common.base.Stopwatch;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zone.rong.garyasm.GaryLogger;
import zone.rong.garyasm.GaryReflector;
import zone.rong.garyasm.api.GaryStringPool;

import java.lang.invoke.MethodHandle;
import java.security.*;
import java.util.ConcurrentModificationException;
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
            secureClassLoader$pdcache$getter = GaryReflector.resolveFieldGetter(SecureClassLoader.class, "pdcache");
            permission$name$setter = GaryReflector.resolveFieldSetter(Permission.class, "name");
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
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            GaryStringPool.establishPool(GaryStringPool.FILE_PERMISSIONS_ID, 512);
            HashMap<CodeSource, ProtectionDomain> pdcache = (HashMap<CodeSource, ProtectionDomain>) SECURECLASSLOADER$PDCACHE$GETTER.invoke(Launch.classLoader);
            for (ProtectionDomain pd : pdcache.values()) {
                PermissionCollection pc = pd.getPermissions();
                if (pc != null) {
                    Enumeration<Permission> perms = pc.elements();
                    while (perms.hasMoreElements()) {
                        Permission perm = perms.nextElement();
                        PERMISSION$NAME$SETTER.invokeExact(perm, GaryStringPool.canonicalize(perm.getName()));
                    }
                }
            }
            GaryStringPool.purgePool(GaryStringPool.FILE_PERMISSIONS_ID);
        }
        catch (ConcurrentModificationException ignored) { } // Swallow it, we don't care enough about the CME here
        catch (Throwable t) {
            t.printStackTrace();
        }
        GaryLogger.instance.info("Took {} to canonicalize Java's FilePermission caches.", stopwatch.stop());
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        run();
    }

}
