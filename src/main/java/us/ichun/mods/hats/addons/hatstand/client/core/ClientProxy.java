package us.ichun.mods.hats.addons.hatstand.client.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import us.ichun.mods.hats.addons.hatstand.client.gui.GuiHatSelection;
import us.ichun.mods.hats.addons.hatstand.client.render.TileRendererHatStand;
import us.ichun.mods.hats.addons.hatstand.common.HatStand;
import us.ichun.mods.hats.addons.hatstand.common.core.CommonProxy;
import us.ichun.mods.hats.addons.hatstand.common.tileentity.TileEntityHatStand;

public class ClientProxy extends CommonProxy
{
    @Override
    public void initMod()
    {
        super.initMod();

        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(HatStand.blockHatStand), 0, new ModelResourceLocation("hats:HatStand", "inventory"));
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
