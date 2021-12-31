package smallville7123.infinity_wire;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class InfinityWire_ITEM extends Item
{
    public InfinityWire_ITEM()
    {
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }

    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        boolean flag = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
        BlockPos blockpos = flag ? pos : pos.offset(facing);
        ItemStack itemstack = player.getHeldItem(hand);


        if (player.canPlayerEdit(blockpos, facing, itemstack) && worldIn.mayPlace(worldIn.getBlockState(blockpos).getBlock(), blockpos, false, facing, player) && Main.Registered.InfinityWire_Block.canPlaceBlockAt(worldIn, blockpos))
        {
            worldIn.setBlockState(blockpos, Main.Registered.InfinityWire_Block.getDefaultState());

            if (player instanceof EntityPlayerMP)
            {
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, blockpos, itemstack);
            }

            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
        else
        {
            return EnumActionResult.FAIL;
        }
    }
}
