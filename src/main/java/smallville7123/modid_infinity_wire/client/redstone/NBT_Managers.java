package smallville7123.modid_infinity_wire.client.redstone;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import smallville7123.modid_infinity_wire.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class NBT_Managers {

    private static class Base {

    }

    private static abstract class ArrayBase<E> extends Base {
        List<E> list;

        protected ArrayBase(Supplier<List<E>> s) {
            list = s.get();
        }

        abstract public void put(CompoundNBT compoundNBT, String key);

        abstract public void get(CompoundNBT nbt, String key);

        public int size() {
            return list.size();
        }
    }

    private static class ListNBTBase extends Base {
        net.minecraft.nbt.ListNBT listNBT = new net.minecraft.nbt.ListNBT();

        public void put(CompoundNBT compoundNBT, String key) {
            compoundNBT.put(key, listNBT);
        }

        public void get(CompoundNBT nbt, String key) {
            listNBT = nbt.getList(key, Constants.NBT.TAG_COMPOUND);
        }

        public int size() {
            return listNBT.size();
        }
    }

    public static class ListNBT extends ListNBTBase {
        public void add(ListNBTBase listNBT) {
            add(listNBT.listNBT);
        }

        public void add(net.minecraft.nbt.ListNBT listNBT) {
            this.listNBT.add(listNBT);
        }

        public  void addBlockPosList(List<net.minecraft.util.math.BlockPos> list) {
            if (list != null) {
                NBT_Managers.BlockPos array = new BlockPos();
                for (net.minecraft.util.math.BlockPos blockPos : list) {
                    array.add(blockPos);
//                    Main.LOGGER.info("addBlockPosList: added pos to array: " + blockPos);
                }
                add(array);
//                Main.LOGGER.info("addBlockPosList: added array: " + array.listNBT);
            }
        }

        public void addBlockStateList(List<net.minecraft.block.BlockState> list) {
            if (list != null) {
                NBT_Managers.BlockState array = new BlockState();
                for (net.minecraft.block.BlockState blockState : list) {
                    array.add(blockState);
//                    Main.LOGGER.info("addBlockStateList: added state to array: " + blockState);
                }
                add(array);
//                Main.LOGGER.info("addBlockStateList: added array: " + array.listNBT);
            }
        }

        @Override
        public void get(CompoundNBT nbt, String key) {
            listNBT = nbt.getList(key, Constants.NBT.TAG_LIST);
        }

        public <T extends ListNBTBase> T read(int i, final Supplier<T> supplier) {
            T list = supplier.get();
            list.listNBT = listNBT.getList(i);
            return list;
        }

        public <List extends java.util.List<net.minecraft.util.math.BlockPos>> List readBlockPosList(Supplier<List> supplier, ListNBT list, int i) {
            BlockPos l = list.read(i, BlockPos::new);
            if (l.size() != 0) {
                List arrayList = supplier.get();
                for (int i_ = 0; i_ < l.size(); i_++) {
                    net.minecraft.util.math.BlockPos blockPos = l.read(i_);
                    arrayList.add(blockPos);
//                    Main.LOGGER.info("readBlockPosList: added pos to array: " + blockPos);
                }
//                Main.LOGGER.info("readBlockPosList: added array: " + arrayList);
                return arrayList;
            }
            return null;
        }

        public <List extends java.util.List<net.minecraft.util.math.BlockPos>> List readBlockPosList(Supplier<List> supplier, int i) {
            return readBlockPosList(supplier, this, i);
        }

        public <List extends java.util.List<net.minecraft.block.BlockState>> List readBlockStateList(Supplier<List> supplier, ListNBT list, int i) {
            BlockState l = list.read(i, BlockState::new);
            if (l.size() != 0) {
                List arrayList = supplier.get();
                for (int i_ = 0; i_ < l.size(); i_++) {
                    net.minecraft.block.BlockState blockState = l.read(i_);
                    arrayList.add(blockState);
//                    Main.LOGGER.info("readBlockStateList: added state to array: " + blockState);
                }
//                Main.LOGGER.info("readBlockStateList: added array: " + arrayList);
                return arrayList;
            }
            return null;
        }

        public <List extends java.util.List<net.minecraft.block.BlockState>> List readBlockStateList(Supplier<List> supplier, int i) {
            return readBlockStateList(supplier, this, i);
        }
    }

    public static class BlockPos extends ListNBTBase {
        public void add(net.minecraft.util.math.BlockPos blockPos) {
            if (blockPos == null) {
                Main.LOGGER.error("NBT_Managers$BlockPos.add: not writing a null BlockPos");
                return;
            }
            CompoundNBT compoundNBT = NBTUtil.writeBlockPos(blockPos);
            listNBT.add(compoundNBT);
//            Main.LOGGER.info("add: added compound: " + compoundNBT);
//            Main.LOGGER.info("add: added pos: " + blockPos);
//            Main.LOGGER.info("add: listNBT: " + listNBT);
        }

        public net.minecraft.util.math.BlockPos read(int i) {
//            Main.LOGGER.info("read: listNBT: " + listNBT);
            CompoundNBT compoundNBT = listNBT.getCompound(i);
            net.minecraft.util.math.BlockPos blockPos = NBTUtil.readBlockPos(compoundNBT);
//            Main.LOGGER.info("read: obtained compound: " + compoundNBT);
//            Main.LOGGER.info("read: obtained pos: " + blockPos);
            return blockPos;
        }
    }

    public static class BlockState extends ListNBTBase {
        public void add(net.minecraft.block.BlockState blockState) {
            CompoundNBT compoundNBT = NBTUtil.writeBlockState(blockState);
            listNBT.add(compoundNBT);
        }

        public net.minecraft.block.BlockState read(int i) {
            CompoundNBT compoundNBT = listNBT.getCompound(i);
            net.minecraft.block.BlockState blockState = NBTUtil.readBlockState(compoundNBT);
            return blockState;
        }

        @Override
        public void put(CompoundNBT compoundNBT, String key) {
//            Main.LOGGER.info("put: storing list: " + listNBT);
            super.put(compoundNBT, key);
        }

        @Override
        public void get(CompoundNBT nbt, String key) {
            super.get(nbt, key);
//            Main.LOGGER.info("get: obtained list: " + listNBT);
        }
    }

    public static class Integer extends ArrayBase<java.lang.Integer> {
        protected Integer(Supplier<List<java.lang.Integer>> s) {
            super(s);
        }

        @Override
        public void put(CompoundNBT compoundNBT, String key) {
            compoundNBT.putIntArray(key, list);
        }

        @Override
        public void get(CompoundNBT nbt, String key) {
            int[] intArray = nbt.getIntArray(key);
            for (int i : intArray) {
                list.add(i);
            }
        }

        public void add(int value) {
            list.add(value);
        }

        public int read(int i) {
            return (int) list.get(i);
        }
    }

    public static class Boolean extends ArrayBase<java.lang.Integer> {
        protected Boolean(Supplier<List<java.lang.Integer>> s) {
            super(s);
        }

        private static final int INTEGER_TRUE = 1;
        private static final int INTEGER_FALSE = 0;

        @Override
        public void put(CompoundNBT compoundNBT, String key) {
            compoundNBT.putIntArray(key, list);
        }

        @Override
        public void get(CompoundNBT nbt, String key) {
            int[] intArray = nbt.getIntArray(key);
            for (int i : intArray) {
                list.add(i);
            }
        }

        public void add(boolean value) {
            list.add(value ? INTEGER_TRUE : INTEGER_FALSE);
        }

        public boolean read(int i) {
            return list.get(i) == INTEGER_TRUE;
        }
    }
}
