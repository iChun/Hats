package me.ichun.mods.hats.addons.hatstand.common.block;

import me.ichun.mods.hats.addons.hatstand.common.HatStand;
import me.ichun.mods.hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class BlockHatStand extends Block
        implements ITileEntityProvider
{
    public static final PropertyInteger TYPE = PropertyInteger.create("type", 0, 7);
    public static final AxisAlignedBB BASE_FLOOR = new AxisAlignedBB(0.0F + 0.1F, 0.0F, 0.0F + 0.1F, 1.0F - 0.1F, 0.15F, 1.0F - 0.1F);
    public static final AxisAlignedBB BASE_ORIENT_0 = new AxisAlignedBB(1.0F - 0.15F, 0.0F + 0.1F, 0.0F + 0.1F, 1.0F, 1.0F - 0.1F, 1.0F - 0.1F);
    public static final AxisAlignedBB BASE_ORIENT_1 = new AxisAlignedBB(0.0F + 0.1F, 0.0F + 0.1F, 1.0F - 0.15F, 1.0F - 0.1F, 1.0F - 0.1F, 1.0F);
    public static final AxisAlignedBB BASE_ORIENT_2 = new AxisAlignedBB(0.0F, 0.0F + 0.1F, 0.0F + 0.1F, 0.15F, 1.0F - 0.1F, 1.0F - 0.1F);
    public static final AxisAlignedBB BASE_ORIENT_3 = new AxisAlignedBB(0.0F + 0.1F, 0.0F + 0.1F, 0.0F, 1.0F - 0.1F, 1.0F - 0.1F, 0.15F);
    public static final AxisAlignedBB STAND = new AxisAlignedBB(0.0F + 0.2F, 0.3F, 0.0F + 0.2F, 1.0F - 0.2F, 0.9F, 1.0F - 0.2F);


    public BlockHatStand(Material par2Material)
    {
        super(par2Material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, 0));
        this.setSoundType(SoundType.WOOD);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityHatStand();
    }

    public AxisAlignedBB getBlockBoundsForBase(IBlockAccess iblockaccess, TileEntityHatStand stand)
    {
        if(stand == null || stand.hasBase)
        {
            if(stand == null || stand.isOnFloor)
            {
                return BASE_FLOOR;
            }
            else
            {
                switch(stand.orientation)
                {
                    case 0:
                    {
                        return BASE_ORIENT_0;
                    }
                    case 1:
                    {
                        return BASE_ORIENT_1;
                    }
                    case 2:
                    {
                        return BASE_ORIENT_2;
                    }
                    case 3:
                    {
                        return BASE_ORIENT_3;
                    }
                }
            }
        }
        return null;
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
    public BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, TYPE);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(pos);
        if(world.isRemote && te instanceof TileEntityHatStand)
        {
            HatStand.proxy.openGui(player, (TileEntityHatStand)te);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
    {
        //THIS CANNOT BE NULL.
        return FULL_BLOCK_AABB.offset(pos);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, boolean isActualState)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityHatStand)
        {
            AxisAlignedBB base = getBlockBoundsForBase(world, (TileEntityHatStand)te);
            if(base != null)
            {
                if(base.offset(pos).intersects(mask))
                {
                    list.add(base.offset(pos));
                }
                if(STAND.offset(pos).intersects(mask))
                {
                    list.add(STAND.offset(pos));
                }
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
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
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
}
