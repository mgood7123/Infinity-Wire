package smallville7123.infinity_wire.mod.registry;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import smallville7123.infinity_wire.mod.init.ModItems;
import smallville7123.infinity_wire.mod.init.RenderHandler;

import static smallville7123.infinity_wire.mod.init.RenderHandler.registerModel;

/**
 * <em><b>Copyright (c) 2018 Ocelot5836.</b></em>
 *
 * <br>
 * </br>
 *
 * Handles all the registry in the mod.
 *
 * @author Ocelot5836
 */
public class Registry {

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ModItems.getItems());
    }

    @SubscribeEvent
    public void registerRenders(ModelRegistryEvent event) {
        RenderHandler.registerMetaItemRenders();
        for (int i = 0; i < ModItems.getItems().length; i++) {
            Item item = ModItems.getItems()[i];
            if (item != null && !item.getHasSubtypes()) {
                registerModel(item);
            }
        }
    }
}