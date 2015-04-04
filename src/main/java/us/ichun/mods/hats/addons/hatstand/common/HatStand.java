package us.ichun.mods.hats.addons.hatstand.common;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import us.ichun.mods.hats.addons.hatstand.common.core.CommonProxy;
import us.ichun.mods.ichunutil.common.core.network.PacketChannel;
import us.ichun.mods.ichunutil.common.core.updateChecker.ModVersionChecker;
import us.ichun.mods.ichunutil.common.core.updateChecker.ModVersionInfo;
import us.ichun.mods.ichunutil.common.iChunUtil;

@Mod(modid = "HatStand", name = "HatStand",
        version = HatStand.version,
        dependencies = "required-after:Hats@[" + iChunUtil.versionMC + ".0.0,)",
        acceptableRemoteVersions = "[" + iChunUtil.versionMC +".0.0," + iChunUtil.versionMC + ".1.0)"
)
public class HatStand
{
    public static final String version = iChunUtil.versionMC + ".0.0";

    public static PacketChannel channel;

    public static Block blockHatStand;

    @Instance("HatStand")
    public static HatStand instance;

    @SidedProxy(clientSide = "us.ichun.mods.hats.addons.hatstand.client.core.ClientProxy", serverSide = "us.ichun.mods.hats.addons.hatstand.common.core.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void load(FMLPreInitializationEvent event)
    {
        proxy.preInitMod();

        ModVersionChecker.register_iChunMod(new ModVersionInfo("HatStand", iChunUtil.versionOfMC, version, false));
    }
}
