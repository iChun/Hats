package hats.addons.hatstand.common.block;

import hats.addons.hatstand.common.HatStand;
import hats.addons.hatstand.common.tileentity.TileEntityHatStand;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockHatStand extends Block 
	implements ITileEntityProvider
{

	public BlockHatStand(int par1, Material par2Material) 
	{
		super(par1, par2Material);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) 
	{
		return new TileEntityHatStand();
	}

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k)
    {
    	return super.getCollisionBoundingBoxFromPool(world, i, j, k);
    }
    
    public void setBlockBoundsForBase(IBlockAccess iblockaccess, TileEntityHatStand stand)
    {
    	float border = 0.1F;
    	float thickness = 0.15F;
    	if(stand == null || stand.hasBase)
    	{
	    	if(stand == null || stand.isOnFloor)
	    	{
	    		setBlockBounds(0.0F + border, 0.0F, 0.0F + border, 1.0F - border, thickness, 1.0F - border);
	    	}
	    	else
	    	{
	    		switch(stand.orientation)
	    		{
		    		case 0:
		    		{
		    			setBlockBounds(1.0F - thickness, 0.0F + border, 0.0F + border, 1.0F, 1.0F - border, 1.0F - border);
		    			break;
		    		}
		    		case 1:
		    		{
		    			setBlockBounds(0.0F + border, 0.0F + border, 1.0F - thickness, 1.0F - border, 1.0F - border, 1.0F);
		    			break;
		    		}
		    		case 2:
		    		{
		    			setBlockBounds(0.0F, 0.0F + border, 0.0F + border, thickness, 1.0F - border, 1.0F - border);
		    			break;
		    		}
		    		case 3:
		    		{
		    			setBlockBounds(0.0F + border, 0.0F + border, 0.0F, 1.0F - border, 1.0F - border, thickness);
		    			break;
		    		}
	    		}
	    	}
    	}
    	else
    	{
    		setBlockBounds(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    	}
    }

    public void setBlockBoundsForStand(IBlockAccess iblockaccess, TileEntityHatStand stand)
    {
    	if(stand == null || stand.hasStand)
    	{
    		float f = 0.45F;
    		if(stand == null || stand.head > 0 || stand.hatName.equalsIgnoreCase(""))
    		{
    			setBlockBounds(0.0F + f, 0.0F, 0.0F + f, 1.0F - f, 0.8F, 1.0F - f);
    		}
    		else
    		{
    			setBlockBounds(0.0F + f, 0.0F, 0.0F + f, 1.0F - f, 0.450F, 1.0F - f);
    		}
    	}
    	else
    	{
    		setBlockBounds(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    	}
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess iblockaccess, int i, int j, int k)
    {
//    	TileEntity te = iblockaccess.getBlockTileEntity(i, j, k);
//    	if(te instanceof TileEntityHatStand)
//    	{
//    		TileEntityHatStand stand = (TileEntityHatStand)te;
//    		if(stand.isOnFloor)
//    		{
//    			
//    		}
//    	}
    	setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }
    
    @Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int l, float f, float f1, float f2)
	{
    	TileEntity te = world.getBlockTileEntity(i, j, k);
    	if(world.isRemote && te instanceof TileEntityHatStand)
    	{
    		HatStand.proxy.openGui(entityplayer, (TileEntityHatStand)te);
    	}
    	return true;
	}
    
    @Override
    public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB aabb, List list, Entity entity)
    {
    	TileEntity te = world.getBlockTileEntity(i, j, k);
    	if(te instanceof TileEntityHatStand)
    	{
    		TileEntityHatStand stand = (TileEntityHatStand)te;
    		setBlockBoundsForBase(world, stand);
    		super.addCollisionBoxesToList(world, i, j, k, aabb, list, entity);
    		float f = 0.2F;
    		setBlockBounds(0.0F + f, 0.3F, 0.0F + f, 1.0F - f, 0.9F, 1.0F - f);    		
    		super.addCollisionBoxesToList(world, i, j, k, aabb, list, entity);
    	}
    }
    
    @Override
    public void onNeighborBlockChange(World world, int i, int j, int k, int l)
    {
    	TileEntity te = world.getBlockTileEntity(i, j, k);
    	if(te instanceof TileEntityHatStand)
    	{
    		TileEntityHatStand stand = (TileEntityHatStand)te;
    		
			int ii = stand.xCoord;
			int jj = stand.yCoord;
			int kk = stand.zCoord;
			
	    	switch(stand.sideOn)
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
	    	if(!world.isBlockSolidOnSide(ii, jj, kk, ForgeDirection.getOrientation(stand.sideOn), false))
	    	{
	    		world.setBlockToAir(i, j, k);
	    		dropBlockAsItem_do(world, i, j, k, new ItemStack(HatStand.blockHatStand, 1));
	    	}

    	}
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }
    
    @Override
    public int getRenderType()
    {
    	return HatStand.renderHatStandID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
    	this.blockIcon = par1IconRegister.registerIcon("planks_oak");
    }
    
}
