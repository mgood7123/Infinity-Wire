package smallville7123.infinity_wire;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod(
        modid = InfinityWire.MOD_ID,
        name = InfinityWire.MOD_NAME,
        version = InfinityWire.VERSION
)
public class InfinityWire {

    public static Logger logger;
    public static final String MOD_ID = "infinity_wire";
    public static final String MOD_NAME = "Infinity Wire";
    public static final String VERSION = "1.0";

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static InfinityWire INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {

    }

    /**
     * Forge will automatically look up and bind blocks to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Blocks {
        public static final InfinityWire_BLOCK InfinityWire_Block = null;
    }

    /**
     * Forge will automatically look up and bind items to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Items {
        public static final ItemBlock InfinityWire_ItemBlock = null;
        public static final InfinityWire_ITEM InfinityWire_Item = null;
    }

    /**
     * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
     */
    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {
        /**
         * Listen for the register event for creating custom items
         */
        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event) {
            logger.info("Registering items");
            event.getRegistry().register(new InfinityWire_ITEM().setRegistryName(MOD_ID, "InfinityWire_Item").setTranslationKey("infinity_wire.infinityWire"));
            event.getRegistry().register(new ItemBlock(Blocks.InfinityWire_Block).setRegistryName(Blocks.InfinityWire_Block.getRegistryName()));
            logger.info("Registered items");
        }

        /**
         * Listen for the register event for creating custom blocks
         */
        @SubscribeEvent
        public static void addBlocks(RegistryEvent.Register<Block> event) {
            logger.info("Registering blocks");
            event.getRegistry().register(new InfinityWire_BLOCK().setRegistryName(MOD_ID, "InfinityWire_Block").setTranslationKey("infinity_wire.infinityWire"));
            logger.info("Registered blocks");
        }

        /**
         * Listen for the register event for mapping custom block models
         */
        @SubscribeEvent
        public void onModelRegistry(ModelRegistryEvent event) {
            logger.info("Registering models");

            ModelResourceLocation modelResourceLocationItem = new ModelResourceLocation("infinity_wire:infinitywire_item", "inventory");
            ModelLoader.setCustomModelResourceLocation(Items.InfinityWire_Item, 0, modelResourceLocationItem);

            logger.info("Registered models");
        }
    }
}
