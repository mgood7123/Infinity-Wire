package smallville7123.modid_infinity_wire.client.redstone;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import smallville7123.modid_infinity_wire.Main;

/**
 * User: The Grey Ghost
 * Date: 24/12/2014
 *
 * These methods are called during startup
 *  See MinecraftByExample class for more information
 */
public class StartupCommon
{
  public static Block redstoneWireBlock;
  public static BlockItem redstoneWireBlockItem;

  @SubscribeEvent
  public static void onBlocksRegistration(final RegistryEvent.Register<Block> blockRegisterEvent) {
    Main.LOGGER.info("Registering blocks");

    redstoneWireBlock = new RedstoneWireBlock(AbstractBlock.Properties.of(Material.DECORATION).noCollission().instabreak())
            .setRegistryName(Main.MODID, "infinity_wire");
    blockRegisterEvent.getRegistry().register(redstoneWireBlock);

    Main.LOGGER.info("Registered blocks");
  }

  @SubscribeEvent
  public static void onItemsRegistration(final RegistryEvent.Register<Item> itemRegisterEvent) {
    Main.LOGGER.info("Registering items");

    Item.Properties itemProperties = new Item.Properties().stacksTo(64).tab(ItemGroup.TAB_REDSTONE);
    redstoneWireBlockItem = new BlockItem(redstoneWireBlock, itemProperties);
    redstoneWireBlockItem.setRegistryName(redstoneWireBlock.getRegistryName());
    itemRegisterEvent.getRegistry().register(redstoneWireBlockItem);

    Main.LOGGER.info("Registered items");
  }

//  @SubscribeEvent
//  public static void onTileEntityTypeRegistration(final RegistryEvent.Register<TileEntityType<?>> event) {
//    tileEntityDataTypeMBE06 = TileEntityType.Builder
//                    .of(TileEntityRedstoneMeter::new, blockRedstoneMeter).build(null);
//                            // you probably don't need a datafixer --> null should be fine
//    tileEntityDataTypeMBE06.setRegistryName("minecraftbyexample:mbe06_tile_entity_type_registry_name");
//    event.getRegistry().register(tileEntityDataTypeMBE06);
//  }

  @SubscribeEvent
  public static void onCommonSetupEvent(FMLCommonSetupEvent event) {
    // not actually required for this example....
  }
}
