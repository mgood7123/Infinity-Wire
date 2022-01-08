package smallville7123.modid_infinity_wire.client.redstone;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import smallville7123.modid_infinity_wire.Main;
import smallville7123.modid_infinity_wire.client.redstone.utils.NonNullArrayList;
import smallville7123.modid_infinity_wire.client.redstone.utils.NonNullHashMap;
import smallville7123.modid_infinity_wire.client.redstone.utils.NonNullNonDuplicatesArrayList;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Supplier;

import static net.minecraft.client.renderer.WorldRenderer.DIRECTIONS;

public class RedstonePowerManagement extends WorldSavedData implements Supplier {
    public static final String NAME = "RedstonePowerManagement";

    static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

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
    public CompoundNBT save(CompoundNBT nbt) {
        boolean s = false;
        NBT_Managers.BlockPos block = new NBT_Managers.BlockPos();
        NBT_Managers.BlockState state = new NBT_Managers.BlockState();
        NBT_Managers.ListNBT powerSourcesList = new NBT_Managers.ListNBT();
        NBT_Managers.Integer<NonNullArrayList<Integer>> powerArray = new NBT_Managers.Integer<>(NonNullArrayList::new);
        NBT_Managers.Boolean<NonNullArrayList<Integer>> isPowerSourceArray = new NBT_Managers.Boolean<>(NonNullArrayList::new);
        try {
            for (BlockPos blockPos : blockMap.keySet()) {
                block.add(blockPos);
            }
            blockMap.values().forEach(value -> {
                state.add(value.blockState);
                powerArray.add(value.power);
                powerSourcesList.addBlockPosList(value.powerSources);
                isPowerSourceArray.add(value.isPowerSource);
            });
            s = true;
        } catch (Exception e) {
            Main.LOGGER.error("save: failed to save block map:\n" + getStackTrace(e));
        }
        if (s) {
            block.put(nbt, NAME + "_blockPos");
            state.put(nbt, NAME + "_blockState");
            powerArray.put(nbt, NAME + "_blockMap_Power");
            isPowerSourceArray.put(nbt, NAME + "_blockMap_IsPowerSourceArray");
            powerSourcesList.put(nbt, NAME + "_blockMap_PowerSources");
        }
        return nbt;
    }

    @Override
    public void load(CompoundNBT nbt) {
        blockMap.clear();
        try {
            NBT_Managers.BlockPos block = new NBT_Managers.BlockPos();
            NBT_Managers.BlockState state = new NBT_Managers.BlockState();
            NBT_Managers.ListNBT powerSourcesArray = new NBT_Managers.ListNBT();
            NBT_Managers.Integer<NonNullArrayList<Integer>> powerArray = new NBT_Managers.Integer<>(NonNullArrayList::new);
            NBT_Managers.Boolean<NonNullArrayList<Integer>> isPowerSourceArray = new NBT_Managers.Boolean<>(NonNullArrayList::new);
            block.get(nbt, NAME + "_blockPos");
            state.get(nbt, NAME + "_blockState");
            powerArray.get(nbt, NAME + "_blockMap_Power");
            powerSourcesArray.get(nbt, NAME + "_blockMap_PowerSources");
            isPowerSourceArray.get(nbt, NAME + "_blockMap_IsPowerSourceArray");
            for (int i = 0; i < block.size(); i++) {
                State tmp = new State(state.read(i));
                tmp.power = powerArray.read(i);
                tmp.isPowerSource = isPowerSourceArray.read(i);
                tmp.powerSources = powerSourcesArray.readBlockPosList(NonNullNonDuplicatesArrayList::new, i);
                tmp.wasPowerSourcePlaced = false;
                tmp.wasPowerSourceRemoved = false;
                blockMap.put(block.read(i), tmp);
            }
        } catch (Exception e) {
            Main.LOGGER.error("load: failed to load block map:\n" + getStackTrace(e));
        }
    }

    static class State {
        BlockState blockState;
        int power;
        boolean isPowerSource;
        boolean wasPowerSourcePlaced;
        boolean wasPowerSourceRemoved;
        NonNullNonDuplicatesArrayList<BlockPos> powerSources;

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

    NonNullHashMap<BlockPos, State> blockMap = new NonNullHashMap<>();

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

    static List<Block> getSideSignalBlacklist() {
        return Collections.unmodifiableList(Arrays.asList(
            StartupCommon.redstoneWireBlock,
            Blocks.REDSTONE_WIRE
        ));
    }

    boolean isBlacklistedFromSideSignal(Block block) {
        for (Block block1 : getSideSignalBlacklist()) {
            if (block == block1) {
                return true;
            }
        }
        return false;
    }

    int getSignalFrom(World world, BlockPos blockPos, BlockPos neighborBlockPos, BlockState neighborBlockState) {
        Main.LOGGER.info("getSignalFrom() called with: blockPos = [" + blockPos + "], block [ " + getBlockName(world, blockPos) + " ], neighborBlockPos = [" + neighborBlockPos + "], neighborBlockState = [" + neighborBlockState + "], neighbor block [ " + getBlockName(neighborBlockState) + " ],");
        if (isAir(neighborBlockState)) {
            // air cannot propagate signals
            return 0;
        }
        Direction directionFrom = getDirectionFrom(blockPos, neighborBlockPos);
        if (neighborBlockState.isSignalSource()) {
            Main.LOGGER.info("getSignalFrom: neighbor block is a signal source");
            int signal = neighborBlockState.getSignal(world, neighborBlockPos, directionFrom);
            Main.LOGGER.info("getSignalFrom: signal = " + signal);
            return signal;
        }
        Main.LOGGER.info("getSignalFrom: neighbor block is not a signal source");

        // NEED TO TRACK POWER STATE OF THIS BLOCK!
        int signal = 0;
        int[] signals = new int[DIRECTIONS.length];
        int signal_h = 0;

        for(Direction direction : DIRECTIONS) {
            BlockPos x = neighborBlockPos.relative(direction);

            if (x.equals(blockPos)) {
                Main.LOGGER.info("preCalculatePowerMap: skipping own block: " + x);
                continue;
            }
            Block b = world.getBlockState(x).getBlock();
            if (isBlacklistedFromSideSignal(b)) {
                Main.LOGGER.info("preCalculatePowerMap: skipping blacklisted block: " + getBlockName(b));
                continue;
            }

            int j = world.getSignal(x, direction);
            Main.LOGGER.info("preCalculatePowerMap: neighborBlockPos j = " + j + ", direction = " + direction + ", x = " + x);
            if (j == 0) {
                State existing_ = blockMap.get(x);
                if (existing_ != null) {
//                    if (isBlacklistedFromSideSignal(b)) {
//                        Main.LOGGER.info("preCalculatePowerMap: neighbor block power source block does exist in map, not removing because it is blacklisted");
//                    } else {
                        Main.LOGGER.info("preCalculatePowerMap: neighbor block power source block does exist in map, removing");
                        blockMap.remove(x);
//                    }
                    State existing = blockMap.get(neighborBlockPos);
                    if (existing == null) {
                        Main.LOGGER.error("preCalculatePowerMap: neighbor block does not exist in map");
                    } else {
                        Main.LOGGER.info("preCalculatePowerMap: neighbor block does exist in map, updating state");
                        if (existing.powerSources == null) {
                            existing.power = 0;
                        } else {
                            existing.powerSources.remove(x);
                            if (existing.powerSources.size() == 0) {
                                existing.powerSources = null;
                                existing.power = 0;
                            } else {
                                // recalculate power
                                int tmp = 0;
                                for (int i = 0; i < signal_h; i++) {
                                    tmp = Math.max(signals[i], tmp);
                                }
                                existing.power = tmp;
                            }
                        }
                        signal = existing.power;
                        signals[signal_h] = signal;
                        signal_h++;
                    }
                }
            } else {
                // j != 0
                State existing = blockMap.get(neighborBlockPos);
                if (existing == null) {
                    Main.LOGGER.info("preCalculatePowerMap: neighbor block does not exist in map, creating state");
                    existing = new State(neighborBlockState);
                    existing.isPowerSource = true;
                    existing.power = j;
                    if (existing.powerSources == null) {
                        existing.powerSources = new NonNullNonDuplicatesArrayList<>();
                        existing.powerSources.add(x);
                    } else {
                        if (!existing.powerSources.contains(x)) {
                            existing.powerSources.add(x);
                        }
                    }
                    blockMap.put(neighborBlockPos, existing);
                } else {
                    Main.LOGGER.info("preCalculatePowerMap: neighbor block does exist in map, updating state");
                    existing.power = Math.max(existing.power, j);
                    existing.isPowerSource = true;
                    if (!existing.powerSources.contains(x)) {
                        existing.powerSources.add(x);
                    }
                    blockMap.replace(neighborBlockPos, existing);
                }

                signal = existing.power;
                signals[signal_h] = signal;
                signal_h++;

                State existing_ = blockMap.get(x);
                if (existing_ == null) {
                    BlockState blockState = world.getBlockState(x);
//                    Block b = blockState.getBlock();
//                    if (isBlacklistedFromSideSignal(b)) {
//                        Main.LOGGER.info("preCalculatePowerMap: neighbor block does exist in map, but it is blacklisted, not updating state");
//                    } else {
                        Main.LOGGER.info("preCalculatePowerMap: neighbor block power source block does not exist in map, creating state");
                        State s = new State(blockState);
                        s.isPowerSource = true;
                        s.power = j;
                        blockMap.put(x, s);
//                    }
                } else {
                    BlockState blockState = world.getBlockState(x);
//                    Block b = blockState.getBlock();
//                    if (isBlacklistedFromSideSignal(b)) {
//                        Main.LOGGER.info("preCalculatePowerMap: neighbor block does exist in map, but it is blacklisted, not updating state");
//                    } else {
                        Main.LOGGER.info("preCalculatePowerMap: neighbor block power source block does exist in map, updating state");
                        existing_.blockState = blockState;
                        existing_.power = j;
                        blockMap.replace(x, existing_);
//                    }
                }
            }
        }
        return signal;
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
        Main.LOGGER.info("preCalculatePowerMap: block map: (" + blockMap.size() + " block" + (blockMap.size() == 1 ? "" : "s") + ") " + blockMap);
        Main.LOGGER.info("preCalculatePowerMap: block position: " + blockPos);
        Main.LOGGER.info("preCalculatePowerMap: block name: " + getBlockName(blockState));
        Main.LOGGER.info("preCalculatePowerMap: neighbor block position: " + neighborBlockPos);
        Main.LOGGER.info("preCalculatePowerMap: neighbor block name: " + getBlockName(neighborBlockState));
        if (status == Status.Placed) {
            Main.LOGGER.info("preCalculatePowerMap: state exists, should update block map");
            State existing = blockMap.get(blockPos);
            if (existing == null) {
                Main.LOGGER.info("preCalculatePowerMap: block does not exist in map, creating state");
                State s = new State(blockState);

                for(Direction direction : DIRECTIONS) {
                    BlockPos x = blockPos.relative(direction);
                    int j = getSignalFrom(world, blockPos, x, world.getBlockState(x));
                    Main.LOGGER.info("preCalculatePowerMap: j = " + j + ", direction = " + direction + ", x = " + getBlockName(world, x));
                    if (j != 0) {
                        State existing_ = blockMap.get(x);
                        if (existing_ == null) {
                            State s_ = new State(world.getBlockState(x));
                            s_.isPowerSource = true;
                            s_.power = j;
                            blockMap.put(x, s_);
                        } else {
                            existing_.blockState = world.getBlockState(x);
                            existing_.power = j;
                            blockMap.replace(x, existing_);
                        }
                        s.power = Math.max(s.power, j);
                        s.wasPowerSourcePlaced = true;
                        BlockPos p = neighborBlockPos == null ? x : neighborBlockPos;
                        if (s.powerSources == null) {
                            s.powerSources = new NonNullNonDuplicatesArrayList<>();
                            s.powerSources.add(p);
                        } else {
                            if (!existing.powerSources.contains(p)) {
                                existing.powerSources.add(x);
                            }
                        }
                    }
                }
                blockMap.put(blockPos, s);
            } else {
                Main.LOGGER.info("preCalculatePowerMap: state exists, block exists in map, updating state with blockstate");
                existing.blockState = blockState;
                blockMap.replace(blockPos, existing);
            }
        } else if (status == Status.Removed) {
            Main.LOGGER.info("preCalculatePowerMap: no state exists, should update block map");
            State state = blockMap.get(blockPos);
            if (state.powerSources == null) {
                Main.LOGGER.info("preCalculatePowerMap: block " + getBlockName(state.blockState) + " " + blockPos + " contains no power sources, removing");
                blockMap.remove(blockPos);
            } else {
                Main.LOGGER.info("preCalculatePowerMap: block " + getBlockName(state.blockState) + " " + blockPos + " contains power sources");
                for (BlockPos powerSource : state.powerSources) {
                    State powerState = blockMap.get(powerSource);
                    if (powerState == null) {
                        Main.LOGGER.error("preCalculatePowerMap: power source " + getBlockName(world, powerSource) + " " + powerSource + " for block " + getBlockName(state.blockState) + " " + blockPos + " does not exist in power map");
                    } else {
                        Main.LOGGER.info("preCalculatePowerMap: power source " + getBlockName(powerState.blockState) + " " + powerSource + " for block " + getBlockName(state.blockState) + " " + blockPos + " does exist in power map");
                        if (powerState.powerSources == null) {
                            Main.LOGGER.info("preCalculatePowerMap: power source " + getBlockName(powerState.blockState) + " " + powerSource + " for block " + getBlockName(state.blockState) + " " + blockPos + " contains no power sources, removing");
                            blockMap.remove(powerSource);
                        } else {
                            Main.LOGGER.info("preCalculatePowerMap: checking for adjacent power sources for " + getBlockName(powerState.blockState) + " " + powerSource);
                            for (BlockPos source : powerState.powerSources) {
                                State power = blockMap.get(source);
                                if (power == null) {
                                    Main.LOGGER.error("preCalculatePowerMap: power source " + getBlockName(world, source) + " " + source + " for block " + getBlockName(powerState.blockState) + " " + powerSource + " does not exist in power map");
                                } else {
                                    Main.LOGGER.info("preCalculatePowerMap: power source " + getBlockName(power.blockState) + " " + source + " for block " + getBlockName(powerState.blockState) + " " + powerSource + " does exist in power map");
                                    blockMap.remove(source);
                                }
                            }
                            Main.LOGGER.info("preCalculatePowerMap: removed adjacent power sources for " + getBlockName(powerState.blockState) + " " + powerSource);
                            Main.LOGGER.info("preCalculatePowerMap: removing " + getBlockName(powerState.blockState) + " " + powerSource);
                            blockMap.remove(powerSource);
                        }
                    }
                }
            }
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
                    if (ss.powerSources != null) {
                        ss.powerSources.remove(neighborBlockPos);
                        if (ss.powerSources.size() == 0) {
                            ss.powerSources = null;
                        }
                    }
                }
            } else {
                Main.LOGGER.info("preCalculatePowerMap: neighborBlockState.isSignalSource() = " + neighborBlockState.isSignalSource());
                int signal = getSignalFrom(world, blockPos, neighborBlockPos, neighborBlockState);
                Main.LOGGER.info("preCalculatePowerMap: neighbor signal: " + signal);
                if (signal == 0) {
                    Main.LOGGER.info("preCalculatePowerMap: neighbor removed");
                    Main.LOGGER.info("preCalculatePowerMap: removing neighbor state in block map");
                    blockMap.remove(neighborBlockPos);
                    State ss = blockMap.get(blockPos);
                    if (ss != null) {
                        ss.wasPowerSourceRemoved = true;
                        if (ss.powerSources != null) {
                            ss.powerSources.remove(neighborBlockPos);
                            if (ss.powerSources.size() == 0) {
                                ss.powerSources = null;
                            }
                        }
                    }
                } else {
                    Main.LOGGER.info("preCalculatePowerMap: neighbor placed/updated");
                    State existing = blockMap.get(neighborBlockPos);
                    boolean n = false;
                    if (existing == null) {
                        Main.LOGGER.info("preCalculatePowerMap: creating neighbor state in block map");
                        existing = new State(neighborBlockState);
                        existing.isPowerSource = true;
                        existing.power = signal;
                        blockMap.put(neighborBlockPos, existing);
                        n = true;
                    } else if (existing.isPowerSource) {
                        Main.LOGGER.info("preCalculatePowerMap: updating neighbor state in block map");
                        existing.blockState = neighborBlockState;
                        existing.power = signal;
                        blockMap.replace(neighborBlockPos, existing);
                        n = true;
                    }
                    if (n) {
                        State ss = blockMap.get(blockPos);
                        if (ss != null) {
                            Main.LOGGER.info("preCalculatePowerMap: updating block state in block map");
                            ss.power = Math.max(ss.power, signal);
                            Main.LOGGER.info("preCalculatePowerMap: block state signal: " + ss.power);
                            ss.wasPowerSourcePlaced = true;
                            if (ss.powerSources == null) {
                                ss.powerSources = new NonNullNonDuplicatesArrayList<>();
                                ss.powerSources.add(neighborBlockPos);
                            } else {
                                if (!ss.powerSources.contains(neighborBlockPos)) {
                                    ss.powerSources.add(neighborBlockPos);
                                }
                            }
                        } else {
                            Main.LOGGER.info("preCalculatePowerMap: block state does not exist in block map");
                        }
                    }
                }
            }
        }
    }

    void calculatePowerMap(World world, BlockPos blockPos, BlockState blockState, BlockPos neighborBlockPos, BlockState neighborBlockState, Status status) {
        preCalculatePowerMap(world, blockPos, blockState, neighborBlockPos, neighborBlockState, status);
        Main.LOGGER.info("calculatePowerMap: block map: (" + blockMap.size() + " block" + (blockMap.size() == 1 ? "" : "s") + ") " + blockMap);
        blockMap.forEach((blockPos1, state) -> {
            Main.LOGGER.info("calculatePowerMap: block position: " + blockPos1);
            Main.LOGGER.info("calculatePowerMap: block state: " + state);
            if (state.wasPowerSourcePlaced) {
                state.wasPowerSourcePlaced = false;
            }
            if (state.wasPowerSourceRemoved) {
                state.wasPowerSourceRemoved = false;
                if (state.powerSources == null) {
                    state.power = 0;
                }
            }
        });
        Main.LOGGER.info("calculatePowerMap: final block map: (" + blockMap.size() + " block" + (blockMap.size() == 1 ? "" : "s") + ") " + blockMap);
    }

    void applyPowerMap(World world) {
        Main.LOGGER.info("applyPowerMap: stage: " + stage);
        blockMap.forEach((blockPos1, state) -> {
            if (!state.isPowerSource) {
                Main.LOGGER.info("applyPowerMap: setting block pos " + blockPos1 + "(" + getBlockName(state.blockState) + ") to power " + state.power);
                world.setBlock(blockPos1, state.blockState.setValue(RedstoneWireBlock.POWER, state.power), 2);
            }
        });
    }

    boolean update(World world, BlockPos blockPos, BlockState blockState, BlockPos neighborBlockPos, BlockState neighborBlockState, Status status) {
        try {
            if (stage != Stage.None) {
                return false;
            }
            Main.LOGGER.info("update: status: " + status);
            if (status == Status.NeighborChanged) {
                if (!blockState.canSurvive(world, blockPos)) {
                    Block.dropResources(blockState, world, blockPos);
                    world.removeBlock(blockPos, false);
                }
            }
            Main.LOGGER.info("update: stage BEFORE: " + stage);
            stage = Stage.Calculating;
            Main.LOGGER.info("update: stage AFTER : " + stage);
            calculatePowerMap(world, blockPos, blockState, neighborBlockPos, neighborBlockState, status);
            Main.LOGGER.info("update: stage BEFORE: " + stage);
            stage = Stage.Applying;
            Main.LOGGER.info("update: stage AFTER : " + stage);
            applyPowerMap(world);
            // always update save even if no change actually happens
            Main.LOGGER.info("update: stage BEFORE: " + stage);
            stage = Stage.None;
            Main.LOGGER.info("update: stage AFTER : " + stage);
            return true;
        } catch (Exception e) {
            Main.LOGGER.error("update: an error occurred:\n" + getStackTrace(e));
            stage = Stage.None;
            return false;
        }
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
