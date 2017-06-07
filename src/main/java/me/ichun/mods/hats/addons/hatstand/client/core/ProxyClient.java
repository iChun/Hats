package me.ichun.mods.hats.addons.hatstand.client.core;

import me.ichun.mods.hats.addons.hatstand.common.HatStand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import me.ichun.mods.hats.addons.hatstand.client.gui.GuiHatSelection;
import me.ichun.mods.hats.addons.hatstand.client.render.TileRendererHatStand;
import me.ichun.mods.hats.addons.hatstand.common.core.ProxyCommon;
import me.ichun.mods.hats.addons.hatstand.common.tileentity.TileEntityHatStand;

public class ProxyClient extends ProxyCommon
{
    @Override
    public void preInitMod()
    {
        super.preInitMod();

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHatStand.class, new TileRendererHatStand());

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(HatStand.blockHatStand), 0, new ModelResourceLocation("hatstand:HatStand", "inventory"));
    }

    @Override
    public void openGui(EntityPlayer player, TileEntityHatStand stand)
    {
        FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiHatSelection(stand));
    }
}
