package me.ichun.mods.hats.addons.hatstand.common.core;

import me.ichun.mods.hats.addons.hatstand.common.HatStand;
import me.ichun.mods.hats.addons.hatstand.common.packet.PacketStandHatInfo;
import me.ichun.mods.hats.addons.hatstand.common.tileentity.TileEntityHatStand;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProxyCommon
{
    public void preInitMod()
    {
        HatStand.channel = new PacketChannel("HatStand", PacketStandHatInfo.class);
    }

    public void openGui(EntityPlayer player, TileEntityHatStand stand){}
}
