package smallville7123.modid_infinity_wire.client.redstone;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import smallville7123.modid_infinity_wire.Main;

import java.util.ArrayList;
import java.util.HashMap;

public class RedstonePowerManagement extends WorldSavedData {
    public static final String NAME = "RedstonePowerManagement";

    public RedstonePowerManagement() {
        super(NAME + "_");
    }

    @Override
    public void load(CompoundNBT p_76184_1_) {
        Main.LOGGER.info("load() called with: p_76184_1_ = [" + p_76184_1_ + "]");
        blockMap.clear();
        Main.LOGGER.info("load: p_76184_1_.size() = " + p_76184_1_.size());
        ListNBT blockPosArray = p_76184_1_.getList(NAME + "_blockPos", Constants.NBT.TAG_COMPOUND);
        ListNBT blockStateArray = p_76184_1_.getList(NAME + "_blockState", Constants.NBT.TAG_COMPOUND);
        int[] powerArray = p_76184_1_.getIntArray(NAME + "_blockMap_Int");
        int[] isPowerSourceArray = p_76184_1_.getIntArray(NAME + "_blockMap_Bool");
        for (int i = 0; i < blockPosArray.size(); i++) {
            State tmp = new State(null);
            tmp.blockState = NBTUtil.readBlockState(blockStateArray.getCompound(i));
            tmp.power = powerArray[i];
            tmp.isPowerSource = isPowerSourceArray[i] == 1;
            blockMap.put(
                    NBTUtil.readBlockPos(
                            blockPosArray.getCompound(i)
                    ),
                    tmp
            );
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT p_189551_1_) {
        Main.LOGGER.info("save() called with: p_189551_1_ = [" + p_189551_1_ + "]");
        Main.LOGGER.info("save: p_189551_1_.size() = " + p_189551_1_.size());
        ListNBT blockPosArray = new ListNBT();
        for (BlockPos blockPos : blockMap.keySet()) {
            blockPosArray.add(NBTUtil.writeBlockPos(blockPos));
        }
        ListNBT blockStateArray = new ListNBT();
        ArrayList<Integer> powerArray = new ArrayList<>();
        ArrayList<Integer> isPowerSourceArray = new ArrayList<>();
        for (State value : blockMap.values()) {
            blockStateArray.add(NBTUtil.writeBlockState(value.blockState));
            powerArray.add(value.power);
            isPowerSourceArray.add(value.isPowerSource ? 1 : 0);
        }
        p_189551_1_.put(NAME + "_blockPos", blockPosArray);
        p_189551_1_.put(NAME + "_blockState", blockStateArray);
        p_189551_1_.putIntArray(NAME + "_blockMap_Int", powerArray);
        p_189551_1_.putIntArray(NAME + "_blockMap_Bool", isPowerSourceArray);
        Main.LOGGER.info("save: p_189551_1_ = [" + p_189551_1_ + "]");
        Main.LOGGER.info("save: p_189551_1_.size() = " + p_189551_1_.size());
        return p_189551_1_;
    }

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

    void calculatePowerMap(World world, BlockPos blockPos, BlockState blockState, Status status) {
        Main.LOGGER.info("calculatePowerMap: block map: " + blockMap);
        Main.LOGGER.info("calculatePowerMap: block position: " + blockPos);
        Main.LOGGER.info("calculatePowerMap: block name: " + blockState.getBlock().getRegistryName());
        State state = blockMap.get(blockPos);
        if (state != null) {
            Main.LOGGER.info("calculatePowerMap: state exists");
            Main.LOGGER.info("calculatePowerMap: updating block state");
            world.setBlock(blockPos, blockState.setValue(RedstoneWireBlock.POWER, state.power), 2);
        } else {
            Main.LOGGER.info("calculatePowerMap: no state exists");
        }
    }

    boolean update(World world, BlockPos blockPos, BlockState blockState, Status status) {
        Main.LOGGER.info("update: status: " + status);
        Main.LOGGER.info("update: block map: " + blockMap);
        if (status == Status.NeighborChanged) {
            if (!blockState.canSurvive(world, blockPos)) {
                Block.dropResources(blockState, world, blockPos);
                world.removeBlock(blockPos, false);
            }
        }
        calculatePowerMap(world, blockPos, blockState, status);
        // always update save even if no change actually happens
        return true;
    }

    public boolean onPlace(World world, BlockPos blockPos, BlockState blockState) {
        Main.LOGGER.info("onPlace: block map before: " + blockMap);
        blockMap.put(blockPos, new State(blockState));
        Main.LOGGER.info("onPlace: block map after: " + blockMap);
        return update(world, blockPos, blockState, Status.Placed);
    }

    public boolean onRemove(World world, BlockPos blockPos, BlockState blockState) {
        Main.LOGGER.info("onRemove: block map before: " + blockMap);
        blockMap.remove(blockPos);
        Main.LOGGER.info("onRemove: block map after: " + blockMap);
        return update(world, blockPos, blockState, Status.Removed);
    }

    public boolean neighborChanged(World world, BlockPos blockPos, BlockState blockState) {
        return update(world, blockPos, blockState, Status.NeighborChanged);
    }
}
