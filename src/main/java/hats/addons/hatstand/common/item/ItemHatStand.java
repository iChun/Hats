package hats.addons.hatstand.common.item;

import hats.addons.hatstand.common.HatStand;
import hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemHatStand extends ItemBlock 
{

	public ItemHatStand(Block blk)
	{
		super(blk);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		if (side == 0)
		{
			return false;
		}
		else
		{
			int ii = x;
			int jj = y;
			int kk = z;
			
	    	switch(side)
	    	{
	    		case 0:
	    		{
	    			jj++;
	    			break;
	    		}
	    		case 1:
	    		{
	    			jj--;
	    			break;
	    		}
	    		case 2:
	    		{
	    			kk++;
	    			break;
	    		}
	    		case 3:
	    		{
	    			kk--;
	    			break;
	    		}
	    		case 4:
	    		{
	    			ii++;
	    			break;
	    		}
	    		case 5:
	    		{
	    			ii--;
	    			break;
	    		}
	    	}
	    	if(!world.isSideSolid(ii, jj, kk, ForgeDirection.getOrientation(side), false))
	    	{
	    		return false;
	    	}
		}
		if (!world.setBlock(x, y, z, HatStand.blockHatStand, metadata, 3))
		{
			return false;
		}

		if (world.getBlock(x, y, z) == HatStand.blockHatStand)
		{
			TileEntity te = world.getTileEntity(x, y, z);
			if(te instanceof TileEntityHatStand)
			{
				TileEntityHatStand stand = (TileEntityHatStand)te;
				
				int orientation = (MathHelper.floor_double((double)(((player.rotationYaw + 180F) * 4F) / 360F) - 0.5D) & 3);
				if(side > 1)
				{
					switch(side)
					{
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
				}
				
				stand.hasStand = stand.isOnFloor = side == 1;
				stand.orientation = orientation;
				stand.sideOn = side;
			}

            field_150939_a.onBlockPlacedBy(world, x, y, z, player, stack);
            field_150939_a.onPostBlockPlaced(world, x, y, z, metadata);
		}

		return true;
	}

}
