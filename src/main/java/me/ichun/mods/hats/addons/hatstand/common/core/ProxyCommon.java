package me.ichun.mods.hats.addons.hatstand.common.core;

import me.ichun.mods.hats.addons.hatstand.common.HatStand;
import me.ichun.mods.hats.addons.hatstand.common.block.BlockHatStand;
import me.ichun.mods.hats.addons.hatstand.common.item.ItemHatStand;
import me.ichun.mods.hats.addons.hatstand.common.packet.PacketStandHatInfo;
import me.ichun.mods.hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProxyCommon
{

    public void preInitMod()
    {
        HatStand.blockHatStand = GameRegistry.register((new BlockHatStand(Material.WOOD)).setHardness(0.5F).setCreativeTab(CreativeTabs.DECORATIONS).setRegistryName(new ResourceLocation("hatstand", "HatStand")).setUnlocalizedName("hats.addon.hatstands.block"));
        GameRegistry.register(new ItemHatStand(HatStand.blockHatStand).setRegistryName(HatStand.blockHatStand.getRegistryName()));

        GameRegistry.addRecipe(new ItemStack(HatStand.blockHatStand, 1),
                "S", "#", '#', Blocks.WOODEN_SLAB, 'S', Items.STICK);

        GameRegistry.registerTileEntity(TileEntityHatStand.class, "HatStand");

        HatStand.channel = new PacketChannel("HatStand", PacketStandHatInfo.class);
    }

    public void openGui(EntityPlayer player, TileEntityHatStand stand){}
}
