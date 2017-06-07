package me.ichun.mods.hats.addons.hatstand.common;

import me.ichun.mods.hats.addons.hatstand.common.core.ProxyCommon;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.update.UpdateChecker;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = HatStand.MOD_ID, name = HatStand.MOD_NAME,
        version = HatStand.VERSION,
        guiFactory = "me.ichun.mods.ichunutil.common.core.config.GenericModGuiFactory",
        dependencies = "required-after:hats@[" + iChunUtil.VERSION_MAJOR + ".0.0,)",
        acceptableRemoteVersions = "[" + iChunUtil.VERSION_MAJOR +".0.0," + iChunUtil.VERSION_MAJOR + ".1.0)"
)
public class HatStand
{
    public static final String MOD_NAME = "HatStand";
    public static final String MOD_ID = "hatstand";
    public static final String VERSION = iChunUtil.VERSION_MAJOR + ".0.0";

    public static PacketChannel channel;

    public static Block blockHatStand;

    @Instance(MOD_ID)
    public static HatStand instance;

    @SidedProxy(clientSide = "me.ichun.mods.hats.addons.hatstand.client.core.ProxyClient", serverSide = "me.ichun.mods.hats.addons.hatstand.common.core.ProxyCommon")
    public static ProxyCommon proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInitMod();

        UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo(MOD_NAME, iChunUtil.VERSION_OF_MC, VERSION, false));
    }
}
