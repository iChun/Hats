package hats.addons.hatstand.client.core;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import hats.addons.hatstand.client.gui.GuiHatSelection;
import hats.addons.hatstand.client.render.BlockRenderHatStand;
import hats.addons.hatstand.client.render.TileRendererHatStand;
import hats.addons.hatstand.common.HatStand;
import hats.addons.hatstand.common.core.CommonProxy;
import hats.addons.hatstand.common.tileentity.TileEntityHatStand;

public class ClientProxy extends CommonProxy 
{

	@Override
	public void initMod()
	{
		super.initMod();
		
		HatStand.renderHatStandID = RenderingRegistry.getNextAvailableRenderId();
		
		BlockRenderHatStand.instance = new BlockRenderHatStand();
		RenderingRegistry.registerBlockHandler(BlockRenderHatStand.instance);
	}
	
	@Override
	public void registerTileEntity(Class clz, String id)
	{
		super.registerTileEntity(clz, id);
		ClientRegistry.bindTileEntitySpecialRenderer(clz, new TileRendererHatStand());
	}
	
	@Override
	public void openGui(EntityPlayer player, TileEntityHatStand stand)
	{
		FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiHatSelection(stand));
	}
}
