package hats.addons.hatstand.common.core;

import cpw.mods.fml.common.network.NetworkRegistry;
import hats.addons.hatstand.common.HatStand;
import hats.addons.hatstand.common.block.BlockHatStand;
import hats.addons.hatstand.common.item.ItemHatStand;
import hats.addons.hatstand.common.packet.PacketStandHatInfo;
import hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import ichun.common.core.network.ChannelHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy 
{

	public void initMod()
	{
		HatStand.blockHatStand = (new BlockHatStand(Material.wood)).setHardness(0.5F).setCreativeTab(CreativeTabs.tabDecorations).setStepSound(Block.soundTypeWood).setBlockName("hats.addon.hatstands.block").setBlockTextureName("planks");

        GameRegistry.registerBlock(HatStand.blockHatStand, ItemHatStand.class, "hats.addon.hatstands.block");

		GameRegistry.addRecipe(new ItemStack(HatStand.blockHatStand, 1),
				new Object[] { "S", "#", Character.valueOf('#'), Blocks.wooden_slab, Character.valueOf('S'), Items.stick});

		registerTileEntity(TileEntityHatStand.class, "Hats_Stand");

        HatStand.channels = NetworkRegistry.INSTANCE.newChannel("HatStand", new ChannelHandler("HatStand", PacketStandHatInfo.class));
    }
	
	public void registerTileEntity(Class clz, String id)
	{
		GameRegistry.registerTileEntity(clz, id);
	}
	
	public void openGui(EntityPlayer player, TileEntityHatStand stand)
	{
		
	}
	
}
