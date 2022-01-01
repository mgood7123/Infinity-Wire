package smallville7123.modid_infinity_wire;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Main.MODID)
public class Main
{
    public static final String MODID = "modid_infinity_wire";

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public Main() {
        // Get an instance of the mod event bus
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Get an instance of the event registrar that is used to bind events to the mod event bus
        // this is a separate class to allow us to use `safeRunWhenOn` instead of the deprecated
        // `runWhenOn` method on the DistExecuter.
        final ClientSideOnlyModEventRegister clientSideOnlyModEventRegister = new ClientSideOnlyModEventRegister(modEventBus);

        registerCommonEvents(modEventBus);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> clientSideOnlyModEventRegister::registerClientOnlyEvents);
    }

    /**
     * Register common events for both dedicated servers and clients. This method is safe to call directly.
     */
    public void registerCommonEvents(IEventBus eventBus) {
        eventBus.register(smallville7123.modid_infinity_wire.client.redstone.StartupCommon.class);
    }
}
