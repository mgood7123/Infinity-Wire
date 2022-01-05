package smallville7123.modid_infinity_wire.client.redstone;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction8;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import smallville7123.modid_infinity_wire.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class RedstonePowerManagement extends WorldSavedData implements Supplier {
    public static final String NAME = "RedstonePowerManagement";

    public RedstonePowerManagement()
    {
        super(Main.MODID);
    }

    public RedstonePowerManagement(String name)
    {
        super(name);
    }

    public static RedstonePowerManagement forWorld(ServerWorld world)
    {
        DimensionSavedDataManager storage = world.getDataStorage();
        Supplier<RedstonePowerManagement> sup = new RedstonePowerManagement();
        RedstonePowerManagement saver = (RedstonePowerManagement) storage.computeIfAbsent(sup, Main.MODID);

        if (saver == null)
        {
            saver = new RedstonePowerManagement();
            storage.set(saver);
        }
        return saver;
    }

    public static RedstonePowerManagement getFromWorld(IWorld world) {
        if (!world.isClientSide() && world instanceof ServerWorld) {
            return RedstonePowerManagement.forWorld((ServerWorld) world);
        }
        return null;
    }

    @Override
    public Object get()
    {
        return this;
    }

    @Override
    public void load(CompoundNBT nbt) {
        blockMap.clear();
        ListNBT blockPosArray = nbt.getList(NAME + "_blockPos", Constants.NBT.TAG_COMPOUND);
        ListNBT blockStateArray = nbt.getList(NAME + "_blockState", Constants.NBT.TAG_COMPOUND);
        int[] powerArray = nbt.getIntArray(NAME + "_blockMap_Int");
        int[] isPowerSourceArray = nbt.getIntArray(NAME + "_blockMap_Bool");
        ListNBT powerSourcesArray = nbt.getList(NAME + "_blockMap_A", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < blockPosArray.size(); i++) {
            State tmp = new State(null);
            tmp.blockState = NBTUtil.readBlockState(blockStateArray.getCompound(i));
            tmp.power = powerArray[i];
            tmp.isPowerSource = isPowerSourceArray[i] == 1;
            tmp.wasPowerSourcePlaced = false;
            tmp.wasPowerSourceRemoved = false;
            ListNBT powerSourcesList = powerSourcesArray.getList(Constants.NBT.TAG_COMPOUND);
            if (powerSourcesList.size() != 0) {
                tmp.powerSources = new ArrayList<>();
                for (int i1 = 0; i1 < powerSourcesList.size(); i1++) {
                    tmp.powerSources.add(
                            NBTUtil.readBlockPos(
                                    powerSourcesList.getCompound(i)
                            )
                    );
                }
            }
            blockMap.put(
                    NBTUtil.readBlockPos(
                            blockPosArray.getCompound(i)
                    ),
                    tmp
            );
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        ListNBT blockPosArray = new ListNBT();
        for (BlockPos blockPos : blockMap.keySet()) {
            blockPosArray.add(NBTUtil.writeBlockPos(blockPos));
        }
        ListNBT blockStateArray = new ListNBT();
        ListNBT powerSourcesList = new ListNBT();
        ArrayList<Integer> powerArray = new ArrayList<>();
        ArrayList<Integer> isPowerSourceArray = new ArrayList<>();
        for (State value : blockMap.values()) {
            blockStateArray.add(NBTUtil.writeBlockState(value.blockState));
            powerArray.add(value.power);
            if (value.powerSources != null) {
                ListNBT powerSourcesArray = new ListNBT();
                for (BlockPos blockPos : value.powerSources) {
                    powerSourcesArray.add(NBTUtil.writeBlockPos(blockPos));
                }
                powerSourcesList.add(powerSourcesArray);
            }
            isPowerSourceArray.add(value.isPowerSource ? 1 : 0);
        }
        nbt.put(NAME + "_blockPos", blockPosArray);
        nbt.put(NAME + "_blockState", blockStateArray);
        nbt.putIntArray(NAME + "_blockMap_Int", powerArray);
        nbt.putIntArray(NAME + "_blockMap_Bool", isPowerSourceArray);
        nbt.put(NAME + "_blockMap_A", powerSourcesList);
        return nbt;
    }

    static class State {
        BlockState blockState;
        int power;
        boolean isPowerSource;
        boolean wasPowerSourcePlaced;
        boolean wasPowerSourceRemoved;
        ArrayList<BlockPos> powerSources;

        public State(BlockState blockState) {
            this.blockState = blockState;
            power = 0;
            isPowerSource = false;
            wasPowerSourcePlaced = false;
            wasPowerSourceRemoved = false;
            powerSources = null;
        }

        @Override
        public String toString() {
            return "State{" +
                    "block name=" + getBlockName(blockState) +
                    ", blockState=" + blockState +
                    ", blockState=" + power +
                    ", power=" + power +
                    ", isPowerSource=" + isPowerSource +
                    ", wasPowerSourcePlaced=" + wasPowerSourcePlaced +
                    ", wasPowerSourceRemoved=" + wasPowerSourceRemoved +
                    ", powerSources=" + powerSources +
                    '}';
        }
    }

    HashMap<BlockPos, State> blockMap = new HashMap<>();

    enum Status {
        Placed, Removed, NeighborChanged, None
    }

    static String getBlockName(Block pBlock) {
        ResourceLocation registryName = pBlock.getRegistryName();
        return registryName == null ? null : registryName.toString();
    }

    static String getBlockName(BlockState pState) {
        return pState == null ? null : getBlockName(pState.getBlock());
    }

    static String getBlockName(World pLevel, BlockPos pPos) {
        return ((pLevel != null) && (pPos != null)) ? getBlockName(pLevel.getBlockState(pPos)) : null;
    }

    public static Direction getDirection(BlockPos from, BlockPos to) {
        BlockPos diff = to.subtract(from);
        for (Direction dir : Direction.values()) {
            if(dir.getNormal().equals(diff)) {
                return dir;
            }
        }
        return null;
    }

    public static Direction getDirectionFrom(BlockPos origin, BlockPos target) {
        return getDirection(origin, target);
    }

    public static Direction getDirectionTo(BlockPos origin, BlockPos target) {
        return getDirectionFrom(target, origin);
    }

    int getSignalFrom(World world, BlockPos blockPos, BlockState neighborBlockState, Direction directionFrom) {
        return neighborBlockState.getSignal(world, blockPos, directionFrom);
    }


    int getSignalFrom(World world, BlockPos blockPos, BlockPos neighborBlockPos, BlockState neighborBlockState) {
        return neighborBlockState.getSignal(world, blockPos, getDirectionFrom(blockPos, neighborBlockPos));
    }

    // https://github.com/MinecraftForge/MinecraftForge/issues/7409
    public static boolean isAir(BlockState blockState) {
        return blockState.getMaterial() == Material.AIR;
    }

    // https://github.com/MinecraftForge/MinecraftForge/issues/7409
    public static boolean isAir(World world, BlockPos blockPos) {
        return isAir(world.getBlockState(blockPos));
    }

    enum Stage {
        Calculating, Applying, None
    }

    Stage stage = Stage.None;

    void preCalculatePowerMap(World world, BlockPos blockPos, BlockState blockState, BlockPos neighborBlockPos, BlockState neighborBlockState, Status status) {
        Main.LOGGER.info("preCalculatePowerMap: block map: " + blockMap);
        Main.LOGGER.info("preCalculatePowerMap: block position: " + blockPos);
        Main.LOGGER.info("preCalculatePowerMap: block name: " + getBlockName(blockState));
        if (status == Status.Placed) {
            Main.LOGGER.info("preCalculatePowerMap: state exists, should update block map");
            State existing = blockMap.get(blockPos);
            if (existing == null) {
                Main.LOGGER.info("preCalculatePowerMap: block does not exist in map, creating state");
                blockMap.put(blockPos, new State(blockState));
            } else {
                Main.LOGGER.info("preCalculatePowerMap: state exists, block exists in map, updating state with blockstate");
                existing.blockState = blockState;
                while (blockMap.containsKey(blockPos)) {
                    blockMap.remove(blockPos);
                }
                blockMap.put(blockPos, existing);
            }
        } else if (status == Status.Removed) {
            Main.LOGGER.info("preCalculatePowerMap: no state exists, should update block map");
            blockMap.remove(blockPos);
        } else if (status == Status.NeighborChanged) {
            // we know the neighbor has changed
            Main.LOGGER.info("preCalculatePowerMap: neighbor block position: " + neighborBlockPos);
            Main.LOGGER.info("preCalculatePowerMap: neighbor block name: " + getBlockName(neighborBlockState));
            if (isAir(neighborBlockState)) {
                Main.LOGGER.info("preCalculatePowerMap: neighbor removed");
                blockMap.remove(neighborBlockPos);
                State ss = blockMap.get(blockPos);
                if (ss != null) {
                    ss.wasPowerSourceRemoved = true;
                    ss.powerSources.remove(neighborBlockPos);
                    if (ss.powerSources.size() == 0) {
                        ss.powerSources = null;
                    }
                }
            } else {
                Main.LOGGER.info("preCalculatePowerMap: neighbor placed/updated");
                Main.LOGGER.info("preCalculatePowerMap: neighborBlockState.isSignalSource() = " + neighborBlockState.isSignalSource());
                int signal = getSignalFrom(world, blockPos, neighborBlockPos, neighborBlockState);

                Main.LOGGER.info("preCalculatePowerMap: neighbor signal: " + signal);
                if (signal != 0) {
                    // do we really need a list of power sources?
                    State existing = blockMap.get(neighborBlockPos);
                    if (existing == null) {
                        State s = new State(neighborBlockState);
                        s.isPowerSource = true;
                        s.power = signal;
                        blockMap.put(neighborBlockPos, s);
                    } else {
                        existing.blockState = neighborBlockState;
                        existing.power = signal;
                        while (blockMap.containsKey(neighborBlockPos)) {
                            blockMap.remove(neighborBlockPos);
                        }
                        blockMap.put(neighborBlockPos, existing);
                    }
                    State ss = blockMap.get(blockPos);
                    if (ss != null) {
                        ss.power = signal;
                        ss.wasPowerSourcePlaced = true;
                        if (ss.powerSources == null) {
                            ss.powerSources = new ArrayList<>();
                        }
                        ss.powerSources.add(neighborBlockPos);
                    }
                }
            }
        }
    }

    void calculatePowerMap(World world, BlockPos blockPos, BlockState blockState, BlockPos neighborBlockPos, BlockState neighborBlockState, Status status) {
        preCalculatePowerMap(world, blockPos, blockState, neighborBlockPos, neighborBlockState, status);
        Main.LOGGER.info("calculatePowerMap: block map: " + blockMap);
        blockMap.forEach((blockPos1, state) -> {
            Main.LOGGER.info("calculatePowerMap: block position: " + blockPos1);
            Main.LOGGER.info("calculatePowerMap: block state: " + state);
            if (state.wasPowerSourceRemoved) {
                state.wasPowerSourcePlaced = false;
                state.power = 0;
            }
        });
        Main.LOGGER.info("calculatePowerMap: final block map: " + blockMap);
    }

    void applyPowerMap(World world) {
        blockMap.forEach((blockPos1, state) -> {
            if (!state.isPowerSource) {
                Main.LOGGER.info("applyPowerMap: setting block pos " + blockPos1 + "(" + getBlockName(state.blockState) + ") to power " + state.power);
                world.setBlock(blockPos1, state.blockState.setValue(RedstoneWireBlock.POWER, state.power), 2);
            }
        });
    }

    boolean update(World world, BlockPos blockPos, BlockState blockState, BlockPos neighborBlockPos, BlockState neighborBlockState, Status status) {
        Main.LOGGER.info("update: status: " + status);
        if (status == Status.NeighborChanged) {
            if (!blockState.canSurvive(world, blockPos)) {
                Block.dropResources(blockState, world, blockPos);
                world.removeBlock(blockPos, false);
            }
        }
        stage = Stage.Calculating;
        calculatePowerMap(world, blockPos, blockState, neighborBlockPos, neighborBlockState, status);
        stage = Stage.Applying;
        applyPowerMap(world);
        // always update save even if no change actually happens
        stage = Stage.None;
        return true;
    }

    boolean update(World world, BlockPos blockPos, BlockState blockState, Status status) {
        return update(world, blockPos, blockState, null, null, status);
    }

    public boolean onPlace(World world, BlockPos blockPos, BlockState blockState) {
        return update(world, blockPos, blockState, Status.Placed);
    }

    public boolean onRemove(World world, BlockPos blockPos, BlockState blockState) {
        return update(world, blockPos, blockState, Status.Removed);
    }

    public boolean neighborChanged(World world, BlockPos blockPos, BlockState blockState, BlockPos neighborBlockPos) {
        return update(world, blockPos, blockState, neighborBlockPos, world.getBlockState(neighborBlockPos), Status.NeighborChanged);
    }
}
