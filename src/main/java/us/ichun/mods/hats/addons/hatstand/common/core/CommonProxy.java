package us.ichun.mods.hats.addons.hatstand.common.core;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import us.ichun.mods.hats.addons.hatstand.common.HatStand;
import us.ichun.mods.hats.addons.hatstand.common.block.BlockHatStand;
import us.ichun.mods.hats.addons.hatstand.common.item.ItemHatStand;
import us.ichun.mods.hats.addons.hatstand.common.packet.PacketStandHatInfo;
import us.ichun.mods.hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import us.ichun.mods.ichunutil.common.core.network.ChannelHandler;

public class CommonProxy
{

    public void preInitMod()
    {
        HatStand.blockHatStand = (new BlockHatStand(Material.wood)).setHardness(0.5F).setCreativeTab(CreativeTabs.tabDecorations).setStepSound(Block.soundTypeWood).setUnlocalizedName("hats.addon.hatstands.block");

        GameRegistry.registerBlock(HatStand.blockHatStand, ItemHatStand.class, "hats.addon.hatstands.block");

        GameRegistry.addRecipe(new ItemStack(HatStand.blockHatStand, 1),
                new Object[] { "S", "#", Character.valueOf('#'), Blocks.wooden_slab, Character.valueOf('S'), Items.stick});

        registerTileEntity(TileEntityHatStand.class, "Hats_Stand");

        HatStand.channel = ChannelHandler.getChannelHandlers("HatStand", PacketStandHatInfo.class);
    }

    public void initMod()
    {
    }

    public void registerTileEntity(Class clz, String id)
    {
        GameRegistry.registerTileEntity(clz, id);
    }

    public void openGui(EntityPlayer player, TileEntityHatStand stand)
    {

    }

}
