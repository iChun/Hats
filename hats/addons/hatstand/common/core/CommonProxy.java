package hats.addons.hatstand.common.core;

import hats.addons.hatstand.common.HatStand;
import hats.addons.hatstand.common.block.BlockHatStand;
import hats.addons.hatstand.common.item.ItemHatStand;
import hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class CommonProxy 
{

	public void initMod()
	{
		HatStand.blockHatStand = (new BlockHatStand(HatStand.blockHatStandID, Material.wood)).setHardness(0.5F).setCreativeTab(CreativeTabs.tabDecorations).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("wood").setTextureName("planks");
		
		GameRegistry.addRecipe(new ItemStack(HatStand.blockHatStand, 1),
				new Object[] { "S", "#", Character.valueOf('#'), Block.woodSingleSlab, Character.valueOf('S'), Item.stick});

		
		GameRegistry.registerBlock(HatStand.blockHatStand, ItemHatStand.class, "Hats_Stand");
		
		registerTileEntity(TileEntityHatStand.class, "Hats_Stand");
		
		LanguageRegistry.addName(HatStand.blockHatStand, "Hat Stand");
	}
	
	public void registerTileEntity(Class clz, String id)
	{
		GameRegistry.registerTileEntity(clz, id);
	}
	
	public void openGui(EntityPlayer player, TileEntityHatStand stand)
	{
		
	}
	
}
