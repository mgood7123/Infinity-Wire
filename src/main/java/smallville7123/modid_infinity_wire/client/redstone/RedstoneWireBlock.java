package smallville7123.modid_infinity_wire.client.redstone;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import smallville7123.modid_infinity_wire.Main;

public class RedstoneWireBlock extends Block {
   public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
   public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
   public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
   public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
   private static final VoxelShape SHAPE_DOT = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
   private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D), Direction.SOUTH, Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Direction.EAST, Block.box(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Direction.WEST, Block.box(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D)));
   private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, VoxelShapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)), Direction.SOUTH, VoxelShapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)), Direction.EAST, VoxelShapes.or(SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)), Direction.WEST, VoxelShapes.or(SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D))));
   private final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();
   private static final Vector3f[] COLORS = new Vector3f[16];
   private final BlockState crossState;
   private boolean shouldSignal = true;

   public RedstoneWireBlock(AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE).setValue(POWER, Integer.valueOf(0)));
      this.crossState = this.defaultBlockState().setValue(NORTH, RedstoneSide.SIDE).setValue(EAST, RedstoneSide.SIDE).setValue(SOUTH, RedstoneSide.SIDE).setValue(WEST, RedstoneSide.SIDE);

      for(BlockState blockstate : this.getStateDefinition().getPossibleStates()) {
         if (blockstate.getValue(POWER) == 0) {
            this.SHAPES_CACHE.put(blockstate, this.calculateShape(blockstate));
         }
      }

   }

   private VoxelShape calculateShape(BlockState pState) {
      VoxelShape voxelshape = SHAPE_DOT;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         RedstoneSide redstoneside = pState.getValue(PROPERTY_BY_DIRECTION.get(direction));
         if (redstoneside == RedstoneSide.SIDE) {
            voxelshape = VoxelShapes.or(voxelshape, SHAPES_FLOOR.get(direction));
         } else if (redstoneside == RedstoneSide.UP) {
            voxelshape = VoxelShapes.or(voxelshape, SHAPES_UP.get(direction));
         }
      }

      return voxelshape;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return this.SHAPES_CACHE.get(pState.setValue(POWER, Integer.valueOf(0)));
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return this.getConnectionState(pContext.getLevel(), this.crossState, pContext.getClickedPos());
   }

   private BlockState getConnectionState(IBlockReader pLevel, BlockState pState, BlockPos pPos) {
      boolean flag = isDot(pState);
      pState = this.getMissingConnections(pLevel, this.defaultBlockState().setValue(POWER, pState.getValue(POWER)), pPos);
      if (flag && isDot(pState)) {
         return pState;
      } else {
         boolean flag1 = pState.getValue(NORTH).isConnected();
         boolean flag2 = pState.getValue(SOUTH).isConnected();
         boolean flag3 = pState.getValue(EAST).isConnected();
         boolean flag4 = pState.getValue(WEST).isConnected();
         boolean flag5 = !flag1 && !flag2;
         boolean flag6 = !flag3 && !flag4;
         if (!flag4 && flag5) {
            pState = pState.setValue(WEST, RedstoneSide.SIDE);
         }

         if (!flag3 && flag5) {
            pState = pState.setValue(EAST, RedstoneSide.SIDE);
         }

         if (!flag1 && flag6) {
            pState = pState.setValue(NORTH, RedstoneSide.SIDE);
         }

         if (!flag2 && flag6) {
            pState = pState.setValue(SOUTH, RedstoneSide.SIDE);
         }

         return pState;
      }
   }

   private BlockState getMissingConnections(IBlockReader pLevel, BlockState pState, BlockPos pPos) {
      boolean flag = !pLevel.getBlockState(pPos.above()).isRedstoneConductor(pLevel, pPos);

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (!pState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected()) {
            RedstoneSide redstoneside = this.getConnectingSide(pLevel, pPos, direction, flag);
            pState = pState.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneside);
         }
      }

      return pState;
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pFacing == Direction.DOWN) {
         return pState;
      } else if (pFacing == Direction.UP) {
         return this.getConnectionState(pLevel, pState, pCurrentPos);
      } else {
         RedstoneSide redstoneside = this.getConnectingSide(pLevel, pCurrentPos, pFacing);
         return redstoneside.isConnected() == pState.getValue(PROPERTY_BY_DIRECTION.get(pFacing)).isConnected() && !isCross(pState) ? pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), redstoneside) : this.getConnectionState(pLevel, this.crossState.setValue(POWER, pState.getValue(POWER)).setValue(PROPERTY_BY_DIRECTION.get(pFacing), redstoneside), pCurrentPos);
      }
   }

   private static boolean isCross(BlockState pState) {
      return pState.getValue(NORTH).isConnected() && pState.getValue(SOUTH).isConnected() && pState.getValue(EAST).isConnected() && pState.getValue(WEST).isConnected();
   }

   private static boolean isDot(BlockState pState) {
      return !pState.getValue(NORTH).isConnected() && !pState.getValue(SOUTH).isConnected() && !pState.getValue(EAST).isConnected() && !pState.getValue(WEST).isConnected();
   }

   /**
    * performs updates on diagonal neighbors of the target position and passes in the flags. The flags can be referenced
    * from the docs for {@link IWorldWriter#setBlock(BlockPos, BlockState, int)}.
    */
   public void updateIndirectNeighbourShapes(BlockState pState, IWorld pLevel, BlockPos pPos, int pFlags, int pRecursionLeft) {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         RedstoneSide redstoneside = pState.getValue(PROPERTY_BY_DIRECTION.get(direction));
         if (redstoneside != RedstoneSide.NONE && !pLevel.getBlockState(blockpos$mutable.setWithOffset(pPos, direction)).is(this)) {
            blockpos$mutable.move(Direction.DOWN);
            BlockState blockstate = pLevel.getBlockState(blockpos$mutable);
            if (!blockstate.is(Blocks.OBSERVER)) {
               BlockPos blockpos = blockpos$mutable.relative(direction.getOpposite());
               BlockState blockstate1 = blockstate.updateShape(direction.getOpposite(), pLevel.getBlockState(blockpos), pLevel, blockpos$mutable, blockpos);
               updateOrDestroy(blockstate, blockstate1, pLevel, blockpos$mutable, pFlags, pRecursionLeft);
            }

            blockpos$mutable.setWithOffset(pPos, direction).move(Direction.UP);
            BlockState blockstate3 = pLevel.getBlockState(blockpos$mutable);
            if (!blockstate3.is(Blocks.OBSERVER)) {
               BlockPos blockpos1 = blockpos$mutable.relative(direction.getOpposite());
               BlockState blockstate2 = blockstate3.updateShape(direction.getOpposite(), pLevel.getBlockState(blockpos1), pLevel, blockpos$mutable, blockpos1);
               updateOrDestroy(blockstate3, blockstate2, pLevel, blockpos$mutable, pFlags, pRecursionLeft);
            }
         }
      }

   }

   private RedstoneSide getConnectingSide(IBlockReader pLevel, BlockPos pPos, Direction pFace) {
      return this.getConnectingSide(pLevel, pPos, pFace, !pLevel.getBlockState(pPos.above()).isRedstoneConductor(pLevel, pPos));
   }

   private RedstoneSide getConnectingSide(IBlockReader pLevel, BlockPos pPos, Direction pDirection, boolean pNonNormalCubeAbove) {
      BlockPos blockpos = pPos.relative(pDirection);
      BlockState blockstate = pLevel.getBlockState(blockpos);
      if (pNonNormalCubeAbove) {
         boolean flag = this.canSurviveOn(pLevel, blockpos, blockstate);
         if (flag && canConnectTo(pLevel.getBlockState(blockpos.above()), pLevel, blockpos.above(), null) ) {
            if (blockstate.isFaceSturdy(pLevel, blockpos, pDirection.getOpposite())) {
               return RedstoneSide.UP;
            }

            return RedstoneSide.SIDE;
         }
      }

      return !canConnectTo(blockstate, pLevel, blockpos, pDirection) && (blockstate.isRedstoneConductor(pLevel, blockpos) || !canConnectTo(pLevel.getBlockState(blockpos.below()), pLevel, blockpos.below(), null)) ? RedstoneSide.NONE : RedstoneSide.SIDE;
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      BlockState blockstate = pLevel.getBlockState(blockpos);
      return this.canSurviveOn(pLevel, blockpos, blockstate);
   }

   private boolean canSurviveOn(IBlockReader pReader, BlockPos pPos, BlockState pState) {
      return pState.isFaceSturdy(pReader, pPos, Direction.UP) || pState.is(Blocks.HOPPER);
   }

   private void updatePowerStrength(World pLevel, BlockPos pPos, BlockState pState) {
      int i = this.calculateTargetStrength(pLevel, pPos);
      if (pState.getValue(POWER) != i) {
         if (pLevel.getBlockState(pPos) == pState) {
            pLevel.setBlock(pPos, pState.setValue(POWER, Integer.valueOf(i)), 2);
         }

         Set<BlockPos> set = Sets.newHashSet();
         set.add(pPos);

         for(Direction direction : Direction.values()) {
            set.add(pPos.relative(direction));
         }

         for(BlockPos blockpos : set) {
            pLevel.updateNeighborsAt(blockpos, this);
         }
      }

   }

   private int calculateTargetStrength(World pLevel, BlockPos pPos) {
      this.shouldSignal = false;
      int i = pLevel.getBestNeighborSignal(pPos);
      this.shouldSignal = true;
      int j = 0;
      if (i < 15) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pPos.relative(direction);
            BlockState blockstate = pLevel.getBlockState(blockpos);
            j = Math.max(j, this.getWireSignal(blockstate, blockpos));
            BlockPos blockpos1 = pPos.above();
            if (blockstate.isRedstoneConductor(pLevel, blockpos) && !pLevel.getBlockState(blockpos1).isRedstoneConductor(pLevel, blockpos1)) {
               BlockPos b = blockpos.above();
               j = Math.max(j, this.getWireSignal(pLevel.getBlockState(b), b));
            } else if (!blockstate.isRedstoneConductor(pLevel, blockpos)) {
               BlockPos b = blockpos.below();
               j = Math.max(j, this.getWireSignal(pLevel.getBlockState(b), b));
            }
         }
      }

      return Math.max(i, j - 1);
   }

   private int getWireSignal(BlockState pState, BlockPos pPos) {
      if (pState.is(this)) {
         Main.LOGGER.info("pState (this) = " + pState.getBlock().getRegistryName() + " + " + pPos);
         int power = pState.getValue(POWER);
         Main.LOGGER.info("pState power = " + power);
         return power + 0;
      } else {
         return 0;
      }
   }

   /**
    * Calls {@link net.minecraft.world.World#updateNeighborsAt(BlockPos, Block)} for all neighboring blocks, but only if the given
    * block is a redstone wire.
    */
   private void checkCornerChangeAt(World pLevel, BlockPos pPos) {
      if (pLevel.getBlockState(pPos).is(this)) {
         pLevel.updateNeighborsAt(pPos, this);

         for(Direction direction : Direction.values()) {
            pLevel.updateNeighborsAt(pPos.relative(direction), this);
         }

      }
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      RedstonePowerManagement saver = RedstonePowerManagement.getFromWorld(pLevel);
      if (saver != null) {
         saver.onPlace(pLevel, pPos, pState);
         saver.setDirty();
      }
   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pIsMoving && !pState.is(pNewState.getBlock())) {
         RedstonePowerManagement saver = RedstonePowerManagement.getFromWorld(pLevel);
         if (saver != null) {
            saver.onRemove(pLevel, pPos, pState);
            saver.setDirty();
         }
         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   private void updateNeighborsOfNeighboringWires(World pLevel, BlockPos pPos) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         this.checkCornerChangeAt(pLevel, pPos.relative(direction));
      }

      for(Direction direction1 : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction1);
         if (pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos)) {
            this.checkCornerChangeAt(pLevel, blockpos.above());
         } else {
            this.checkCornerChangeAt(pLevel, blockpos.below());
         }
      }

   }

   public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      Main.LOGGER.info("neighborChanged() called with: pState = [" + pState + "], pLevel = [" + pLevel + "], pPos = [" + pPos + "], pBlock = [" + pBlock + "], pFromPos = [" + pFromPos + "], pIsMoving = [" + pIsMoving + "]");
      RedstonePowerManagement saver = RedstonePowerManagement.getFromWorld(pLevel);
      if (saver != null) {
         saver.neighborChanged(pLevel, pPos, pState, pFromPos);
         saver.setDirty();
      }
   }

   public int getDirectSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return !this.shouldSignal ? 0 : pBlockState.getSignal(pBlockAccess, pPos, pSide);
   }

   public int getSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      if (this.shouldSignal && pSide != Direction.DOWN) {
         int i = pBlockState.getValue(POWER);
         if (i == 0) {
            return 0;
         } else {
            return pSide != Direction.UP && !this.getConnectionState(pBlockAccess, pBlockState, pPos).getValue(PROPERTY_BY_DIRECTION.get(pSide.getOpposite())).isConnected() ? 0 : i;
         }
      } else {
         return 0;
      }
   }

   protected static boolean canConnectTo(BlockState pState, IBlockReader world, BlockPos pos, @Nullable Direction pDirection) {
      if (pState.is(Blocks.REDSTONE_WIRE) || pState.is(StartupCommon.redstoneWireBlock)) {
         return true;
      } else if (pState.is(Blocks.REPEATER)) {
         Direction direction = pState.getValue(RepeaterBlock.FACING);
         return direction == pDirection || direction.getOpposite() == pDirection;
      } else if (pState.is(Blocks.OBSERVER)) {
         return pDirection == pState.getValue(ObserverBlock.FACING);
      } else {
         return pState.canConnectRedstone(world, pos, pDirection) && pDirection != null;
      }
   }

   /**
    * Can this block provide power. Only wire currently seems to have this change based on its state.
    */
   public boolean isSignalSource(BlockState pState) {
      return this.shouldSignal;
   }

   @OnlyIn(Dist.CLIENT)
   public static int getColorForPower(int pPower) {
      Vector3f vector3f = COLORS[pPower];
      return MathHelper.color(vector3f.x(), vector3f.y(), vector3f.z());
   }

   @OnlyIn(Dist.CLIENT)
   private void spawnParticlesAlongLine(World pLevel, Random pRand, BlockPos pPos, Vector3f pVec3f, Direction pDirection, Direction pDirection1, float pFloat1, float pFloat2) {
      float f = pFloat2 - pFloat1;
      if (!(pRand.nextFloat() >= 0.2F * f)) {
         float f1 = 0.4375F;
         float f2 = pFloat1 + f * pRand.nextFloat();
         double d0 = 0.5D + (double)(0.4375F * (float)pDirection.getStepX()) + (double)(f2 * (float)pDirection1.getStepX());
         double d1 = 0.5D + (double)(0.4375F * (float)pDirection.getStepY()) + (double)(f2 * (float)pDirection1.getStepY());
         double d2 = 0.5D + (double)(0.4375F * (float)pDirection.getStepZ()) + (double)(f2 * (float)pDirection1.getStepZ());
         pLevel.addParticle(new RedstoneParticleData(pVec3f.x(), pVec3f.y(), pVec3f.z(), 1.0F), (double)pPos.getX() + d0, (double)pPos.getY() + d1, (double)pPos.getZ() + d2, 0.0D, 0.0D, 0.0D);
      }
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link #randomTick(BlockState, ServerWorld, BlockPos, Random)} and {@link #isRandomlyTicking}
    * , and will always be called regardless of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      int i = pState.getValue(POWER);
      if (i != 0) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneside = pState.getValue(PROPERTY_BY_DIRECTION.get(direction));
            switch(redstoneside) {
               case UP:
                  this.spawnParticlesAlongLine(pLevel, pRand, pPos, COLORS[i], direction, Direction.UP, -0.5F, 0.5F);
               case SIDE:
                  this.spawnParticlesAlongLine(pLevel, pRand, pPos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.5F);
                  break;
               case NONE:
               default:
                  this.spawnParticlesAlongLine(pLevel, pRand, pPos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.3F);
            }
         }

      }
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link BlockState#rotate(Rotation)} whenever possible. Implementing/overriding is
    * fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      switch(pRotation) {
         case CLOCKWISE_180:
            return pState.setValue(NORTH, pState.getValue(SOUTH)).setValue(EAST, pState.getValue(WEST)).setValue(SOUTH, pState.getValue(NORTH)).setValue(WEST, pState.getValue(EAST));
         case COUNTERCLOCKWISE_90:
            return pState.setValue(NORTH, pState.getValue(EAST)).setValue(EAST, pState.getValue(SOUTH)).setValue(SOUTH, pState.getValue(WEST)).setValue(WEST, pState.getValue(NORTH));
         case CLOCKWISE_90:
            return pState.setValue(NORTH, pState.getValue(WEST)).setValue(EAST, pState.getValue(NORTH)).setValue(SOUTH, pState.getValue(EAST)).setValue(WEST, pState.getValue(SOUTH));
         default:
            return pState;
      }
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link BlockState#mirror(Mirror)} whenever possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      switch(pMirror) {
         case LEFT_RIGHT:
            return pState.setValue(NORTH, pState.getValue(SOUTH)).setValue(SOUTH, pState.getValue(NORTH));
         case FRONT_BACK:
            return pState.setValue(EAST, pState.getValue(WEST)).setValue(WEST, pState.getValue(EAST));
         default:
            return super.mirror(pState, pMirror);
      }
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(NORTH, EAST, SOUTH, WEST, POWER);
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (!pPlayer.abilities.mayBuild) {
         return ActionResultType.PASS;
      } else {
         if (isCross(pState) || isDot(pState)) {
            BlockState blockstate = isCross(pState) ? this.defaultBlockState() : this.crossState;
            blockstate = blockstate.setValue(POWER, pState.getValue(POWER));
            blockstate = this.getConnectionState(pLevel, blockstate, pPos);
            if (blockstate != pState) {
               pLevel.setBlock(pPos, blockstate, 3);
               this.updatesOnShapeChange(pLevel, pPos, pState, blockstate);
               return ActionResultType.SUCCESS;
            }
         }

         return ActionResultType.PASS;
      }
   }

   private void updatesOnShapeChange(World pLevel, BlockPos pPos, BlockState pOldState, BlockState pNewState) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction);
         if (pOldState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() != pNewState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos)) {
            pLevel.updateNeighborsAtExceptFromFacing(blockpos, pNewState.getBlock(), direction.getOpposite());
         }
      }

   }

   static {
      for(int i = 0; i <= 15; ++i) {
         float f = (float)i / 15.0F;
         float f1 = f * 0.1F + (f > 0.0F ? 0.4F : 0.3F);
         float f2 = MathHelper.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
         float f3 = f * 0.3F + (f > 0.0F ? 0.4F : 0.3F);
         COLORS[i] = new Vector3f(f1, f2, f3);
      }

   }
}
