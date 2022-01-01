package smallville7123.modid_infinity_wire;

import net.minecraftforge.eventbus.api.IEventBus;

/*
    This class is required to make sure that we don't accidentally try to load any client-side-only classes
      on a dedicated server.
    It is a rather convoluted way of doing it, but I haven't found a simpler way to do it which is robust
 */

public class ClientSideOnlyModEventRegister {
    private final IEventBus eventBus;

    /**
     * @param eventBus an instance of the mod event bus
     */
    public ClientSideOnlyModEventRegister(IEventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Register client only events. This method must only be called when it is certain that the mod is
     * is executing code on the client side and not the dedicated server.
     */
    public void registerClientOnlyEvents() {
        eventBus.register(smallville7123.modid_infinity_wire.client.redstone.StartupClientOnly.class);
    }
}
