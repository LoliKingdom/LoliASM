package zone.rong.blahajasm.common.crashes;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Set;

/**
 * Objects that implement and register with this will be able to reset to a fresh or even earlier state after a crash.
 */
public interface IStateful {

    Set<WeakReference<IStateful>> INSTANCES = new ReferenceArraySet<>();

    static void resetAll() {
        Iterator<WeakReference<IStateful>> iterator = INSTANCES.iterator();
        while (iterator.hasNext()) {
            IStateful reference = iterator.next().get();
            if (reference != null) {
                reference.resetState();
            } else {
                iterator.remove();
            }
        }
    }

    default void register() {
        INSTANCES.add(new WeakReference<>(this));
    }

    void resetState();

}
