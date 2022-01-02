package smallville7123.modid_infinity_wire.client.redstone;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smallville7123.modid_infinity_wire.Main;

import java.util.HashMap;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

public class RedstonePowerManagement {
    class State {
        BlockState blockState;
        int power;
        boolean isPowerSource;

        public State(BlockState blockState) {
            this.blockState = blockState;
            power = 0;
            isPowerSource = false;
        }
    }

    HashMap<BlockPos, State> blockMap = new HashMap<>();

    enum Status {
        Placed, Removed, NeighborChanged, None
    }
    Status status;

    void calculatePowerMap(World world, BlockPos blockPos, BlockState blockState) {
        Main.LOGGER.info("calculatePowerMap: block position: " + blockPos);
        Main.LOGGER.info("calculatePowerMap: block name: " + blockState.getBlock().getRegistryName());
        State state = blockMap.get(blockPos);
        world.setBlock(blockPos, blockState.setValue(RedstoneWireBlock.POWER, state.power), 2);
    }

    void update(World world, BlockPos blockPos, BlockState blockState) {
        if (status == Status.NeighborChanged) {
            if (!blockState.canSurvive(world, blockPos)) {
                Block.dropResources(blockState, world, blockPos);
                world.removeBlock(blockPos, false);
            }
        }
        calculatePowerMap(world, blockPos, blockState);
        status = Status.None;
    }

    public void onPlace(World world, BlockPos blockPos, BlockState blockState) {
        blockMap.put(blockPos, new State(blockState));
        status = Status.Placed;
        update(world, blockPos, blockState);
    }

    public void onRemove(World world, BlockPos blockPos, BlockState blockState) {
        blockMap.remove(blockPos);
        status = Status.Removed;
        update(world, blockPos, blockState);
    }

    public void neighborChanged(World world, BlockPos blockPos, BlockState blockState) {
        status = Status.NeighborChanged;
        update(world, blockPos, blockState);
    }
}
