package hats.addons.hatstand.client.render;

import org.lwjgl.opengl.GL11;

import hats.addons.hatstand.common.HatStand;
import hats.addons.hatstand.common.block.BlockHatStand;
import hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class BlockRenderHatStand 
	implements ISimpleBlockRenderingHandler 
{
	
	public static BlockRenderHatStand instance; 

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) 
	{
		if(block instanceof BlockHatStand)
		{
			BlockHatStand stand = (BlockHatStand)block;
			
			Tessellator tessellator = Tessellator.instance;
			
			stand.setBlockBoundsForBase(null, null);
			renderer.setRenderBoundsFromBlock(stand);

            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, -1.0F, 0.0F);
            renderer.renderBottomFace(stand, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(stand, 0));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 1.0F, 0.0F);
            renderer.renderTopFace(stand, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(stand, 1));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, -1.0F);
            renderer.renderEastFace(stand, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(stand, 2));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, 1.0F);
            renderer.renderWestFace(stand, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(stand, 3));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(-1.0F, 0.0F, 0.0F);
            renderer.renderNorthFace(stand, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(stand, 4));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(1.0F, 0.0F, 0.0F);
            renderer.renderSouthFace(stand, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(stand, 5));
            tessellator.draw();
			
			
			stand.setBlockBoundsForStand(null, null);
			renderer.setRenderBoundsFromBlock(stand);
			
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, -1.0F, 0.0F);
            renderer.renderBottomFace(stand, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(stand, 0));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 1.0F, 0.0F);
            renderer.renderTopFace(stand, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(stand, 1));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, -1.0F);
            renderer.renderEastFace(stand, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(stand, 2));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, 1.0F);
            renderer.renderWestFace(stand, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(stand, 3));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(-1.0F, 0.0F, 0.0F);
            renderer.renderNorthFace(stand, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(stand, 4));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(1.0F, 0.0F, 0.0F);
            renderer.renderSouthFace(stand, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSide(stand, 5));
            tessellator.draw();
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);

		}

	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) 
	{
		if(block instanceof BlockHatStand)
		{
			this.renderHatStand(world, x, y, z, (BlockHatStand)block, modelId, renderer);
		}
		return true;
	}
	
	public void renderHatStand(IBlockAccess world, int x, int y, int z, BlockHatStand block, int modelId, RenderBlocks renderer)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityHatStand)
		{
			TileEntityHatStand stand = (TileEntityHatStand)te;
			
			block.setBlockBoundsForBase(world, stand);
			renderer.setRenderBoundsFromBlock(block);
			renderer.renderStandardBlock(block, x, y, z);
			
			block.setBlockBoundsForStand(world, stand);
			renderer.setRenderBoundsFromBlock(block);
			renderer.renderStandardBlock(block, x, y, z);
		}
	}

	@Override
	public boolean shouldRender3DInInventory() 
	{
		return true;
	}

	@Override
	public int getRenderId() 
	{
		return HatStand.renderHatStandID;
	}

}
