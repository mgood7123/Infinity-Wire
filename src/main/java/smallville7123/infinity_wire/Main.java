package smallville7123.infinity_wire;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
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
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod(
        modid = Main.MOD_ID,
        name = Main.MOD_NAME,
        version = Main.VERSION,
        clientSideOnly = true,
        canBeDeactivated = true,
        acceptedMinecraftVersions = Main.MC_VERSION
)
public class Main {

    public static final String MOD_ID = "infinity_wire";
    public static final String MOD_NAME = "Infinity Wire";
    public static final String VERSION = "1.0";
    public static final String MC_VERSION = "[1.12.2]";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final String INFINITE_WIRE_BLOCK_ID = "infinity_wire_block";
    private static final String INFINITE_WIRE_ITEM_ID = "infinity_wire_item";
    private static final String INFINITE_WIRE_ITEMBLOCK_ID = "infinity_wire_itemblock";


    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static Main INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
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
     * Forge will automatically look up and bind items to the fields in this class
     * based on their registry name.
     */
    @ObjectHolder(MOD_ID)
    public static class Registered {
        @ObjectHolder(INFINITE_WIRE_BLOCK_ID)
        public static final Block InfinityWire_Block = null;

        @ObjectHolder(INFINITE_WIRE_ITEM_ID)
        public static final Item InfinityWire_Item = null;
    }

    /**
     * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
     */
    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {

        // Block will always fire first

        /**
         * Listen for the register event for creating custom blocks
         */
        @SubscribeEvent
        public static void addBlocks(RegistryEvent.Register<Block> event) {
            LOGGER.info("Registering blocks");

            final Block[] blocks = {
                    new InfinityWire_BLOCK().setRegistryName(MOD_ID, INFINITE_WIRE_BLOCK_ID).setTranslationKey(MOD_ID + "." + "infinity_wire"),
            };

            event.getRegistry().registerAll(blocks);

            LOGGER.info("Registered blocks");
        }

        // Item will always fire second

        /**
         * Listen for the register event for creating custom items
         */
        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event) {
            LOGGER.info("Registering items");
            final Item[] items = {
                    new InfinityWire_ITEM().setRegistryName(MOD_ID, INFINITE_WIRE_ITEM_ID).setTranslationKey(MOD_ID + "." + "infinity_wire"),
                    new ItemBlock(Registered.InfinityWire_Block).setRegistryName(Registered.InfinityWire_Block.getRegistryName()).setTranslationKey(MOD_ID + "." + "infinity_wire"),
            };

            event.getRegistry().registerAll(items);

            LOGGER.info("Registered items");
        }

        /**
         * Listen for the register event for mapping custom block models
         */
        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
            LOGGER.info("Registering models");
            registerBlockModel(Registered.InfinityWire_Block);
            registerItemModel(Registered.InfinityWire_Item, 0);
            LOGGER.info("Registered models");
        }

        private static void registerBlockModel(Block block) {
            ModelLoader.setCustomStateMapper(block, new DefaultStateMapper());
        }

        private static void registerItemModel(Item item, int meta) {
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
