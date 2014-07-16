package hats.addons.hatstand.common;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.relauncher.Side;
import hats.addons.hatstand.common.core.CommonProxy;
import ichun.common.core.updateChecker.ModVersionChecker;
import ichun.common.core.updateChecker.ModVersionInfo;
import ichun.common.iChunUtil;
import net.minecraft.block.Block;

import java.util.EnumMap;

@Mod(modid = "HatStand", name = "HatStand",
			version = HatStand.version,
			dependencies = "required-after:Hats@[" + iChunUtil.versionMC + ".0.0,)",
            acceptableRemoteVersions = "[" + iChunUtil.versionMC +".0.0," + iChunUtil.versionMC + ".1.0)"
				)
public class HatStand
{
	public static final String version = iChunUtil.versionMC + ".0.0";

    public static EnumMap<Side, FMLEmbeddedChannel> channels;

    public static Block blockHatStand;
	
	public static int renderHatStandID;
	
	@Instance("HatStand")
	public static HatStand instance;

	@SidedProxy(clientSide = "hats.addons.hatstand.client.core.ClientProxy", serverSide = "hats.addons.hatstand.common.core.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void load(FMLPreInitializationEvent event)
	{
		proxy.initMod();

        ModVersionChecker.register_iChunMod(new ModVersionInfo("HatStand", iChunUtil.versionOfMC, version, false));
	}
}
