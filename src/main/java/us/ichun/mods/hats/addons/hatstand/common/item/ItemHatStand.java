package us.ichun.mods.hats.addons.hatstand.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import us.ichun.mods.hats.addons.hatstand.common.HatStand;
import us.ichun.mods.hats.addons.hatstand.common.tileentity.TileEntityHatStand;

public class ItemHatStand extends ItemBlock
{

    public ItemHatStand(Block blk)
    {
        super(blk);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
    {
        if (side == EnumFacing.DOWN)
        {
            return false;
        }
        else
        {
            if(!world.isSideSolid(pos.offset(side, -1), side, false))
            {
                return false;
            }
        }
        if (!world.setBlockState(pos, newState, 3))
        {
            return false;
        }

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == HatStand.blockHatStand)
        {
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof TileEntityHatStand)
            {
                TileEntityHatStand stand = (TileEntityHatStand)te;

                int orientation = (MathHelper.floor_double((double)(((player.rotationYaw + 180F) * 4F) / 360F) - 0.5D) & 3);
                switch(side.getIndex())
                {
                    default:break;
                    case 2:
                    {
                        orientation = 1;
                        break;
                    }
                    case 3:
                    {
                        orientation = 3;
                        break;
                    }
                    case 4:
                    {
                        orientation = 0;
                        break;
                    }
                    case 5:
                    {
                        orientation = 2;
                        break;
                    }
                }

                stand.hasStand = stand.isOnFloor = side == EnumFacing.UP;
                stand.orientation = orientation;
                stand.sideOn = side.getIndex();
            }

            setTileEntityNBT(world, pos, stack);
            this.block.onBlockPlacedBy(world, pos, state, player, stack);
        }

        return true;
    }

}
