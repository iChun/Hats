package us.ichun.mods.hats.addons.hatstand.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import us.ichun.mods.hats.addons.hatstand.common.HatStand;
import us.ichun.mods.hats.addons.hatstand.common.tileentity.TileEntityHatStand;

import java.util.List;

public class BlockHatStand extends Block
        implements ITileEntityProvider
{
    public static final PropertyInteger TYPE = PropertyInteger.create("type", 0, 7);

    public BlockHatStand(Material par2Material)
    {
        super(par2Material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, 0));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityHatStand();
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
    public void setBlockBoundsBasedOnState(IBlockAccess iblockaccess, BlockPos pos)
    {
        //    	TileEntity te = iblockaccess.getTileEntity(i, j, k);
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
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityHatStand)
        {
            TileEntityHatStand stand = (TileEntityHatStand)te;
            return state.withProperty(TYPE, stand.hasBase ? stand.hasStand ? stand.hatName.isEmpty() ? 0 : 1 : stand.isOnFloor ? 2 : EnumFacing.getFront(stand.sideOn).ordinal() + 2 : 3);
        }
        return state;
    }

    @Override
    public BlockState createBlockState()
    {
        return new BlockState(this, TYPE);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(pos);
        if(world.isRemote && te instanceof TileEntityHatStand)
        {
            HatStand.proxy.openGui(player, (TileEntityHatStand)te);
        }
        return true;
    }

    @Override
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB aabb, List list, Entity entity)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityHatStand)
        {
            TileEntityHatStand stand = (TileEntityHatStand)te;
            setBlockBoundsForBase(world, stand);
            super.addCollisionBoxesToList(world, pos, state, aabb, list, entity);
            float f = 0.2F;
            setBlockBounds(0.0F + f, 0.3F, 0.0F + f, 1.0F - f, 0.9F, 1.0F - f);
            super.addCollisionBoxesToList(world, pos, state, aabb, list, entity);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityHatStand)
        {
            TileEntityHatStand stand = (TileEntityHatStand)te;

            if(!world.isSideSolid(pos.offset(EnumFacing.getFront(stand.sideOn), -1), EnumFacing.getFront(stand.sideOn), false))
            {
                world.setBlockToAir(pos);
                spawnAsEntity(world, pos, new ItemStack(HatStand.blockHatStand, 1));
            }

        }
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean isFullCube()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return 3;
    }
}
