package zone.rong.loliasm.common.singletonevents;

import com.google.common.base.Preconditions;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

/**
 * Events will gather this and use as their new values before firing on the EventBus
 *
 * All {@link net.minecraftforge.fml.common.eventhandler.Event} implementing this must:
 *
 * 1. Have a unique field: { private EventPriority loliPriority = null; }
 * 2. Override {@link Event#getPhase()} and {@link Event#setPhase(EventPriority)}
 * 3. Point loliPriority towards those getters/setters
 * 4. Check Preconditions in {@link Event#setPhase(EventPriority)} via {@link IRefreshEvent#checkPrecondition(EventPriority, EventPriority)}
 */
public interface IRefreshEvent {

    static void checkPrecondition(EventPriority current, EventPriority next) {
        Preconditions.checkNotNull(next, "setPhase argument must not be null");
        Preconditions.checkArgument((current == null ? -1 : current.ordinal()) < next.ordinal(), "Attempted to set event phase to %s when already %s", next, current);
    }

    /**
     * We want to be as fast as we can here, no check and unsafe.
     *
     * @param data attached to the event to be fired
     */
    void refresh(Object... data);

}
