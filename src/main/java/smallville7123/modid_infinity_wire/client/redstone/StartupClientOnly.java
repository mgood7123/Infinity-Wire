package smallville7123.modid_infinity_wire.client.redstone;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * User: The Grey Ghost
 * Date: 24/12/2014
 *
 * These methods are called during startup
 *  See MinecraftByExample class for more information

 */
public class StartupClientOnly
{

  @SubscribeEvent
  public static void onClientSetupEvent(FMLClientSetupEvent event) {
    RenderTypeLookup.setRenderLayer(StartupCommon.redstoneWireBlock, RenderType.cutout());
  }

  @SubscribeEvent
  public static void onColorHandlerEvent(ColorHandlerEvent.Block event)
  {
    // when vanilla wants to know what colour to render our BlockRedstoneColourLamp, it calls our
    //  IBlockColour implementation.  Because the interface only has one method, you could also use a
    //   lambda function instead (as vanilla does)
    event.getBlockColors().register(new RedstoneColor(), StartupCommon.redstoneWireBlock);
  }

  private static class RedstoneColor implements IBlockColor {
    /**
     * Returns the lamp colour for rendering, based on
     * 1) the block state
     * 2) the "tintindex" (tintindex in the block model json)
     *
     *  This is the technique used by BlockGrass to change colour in different biomes.
     *  It's also used by RedstoneWireBlock to change the redness of the wire based on the power level
     * For example:
     * the grassy dirt block,  grass.json contains
     "up":    { "uv": [ 0, 0, 16, 16 ], "texture": "#top", "cullface": "up", "tintindex": 0 },
     "down":  { "uv": [ 0, 0, 16, 16 ], "texture": "#bottom", "cullface": "down" },
     * the top of the block, "up", has tintindex 0 - its colour changes with the biome, which is calculated by a call
     *    to colorMultiplier()
     * the bottom of the block, "down", has no tintindex flag, so colorMultiplier isn't called for this face.
     *  See BlockColors.init() for examples (the handlers are all implemented as anonymous classes so your IDE may not
     *     find them with an ordinary search).
     * @param tintIndex
     * @return an RGB colour (to be multiplied by the texture colours)
     */
    @Override
    public int getColor(BlockState blockState, IBlockDisplayReader iLightReader, BlockPos blockPos, int tintIndex) {
      return RedstoneWireBlock.getColorForPower(blockState.getValue(RedstoneWireBlock.POWER));
    }
  }
}
