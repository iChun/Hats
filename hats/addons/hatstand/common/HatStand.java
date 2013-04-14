package hats.addons.hatstand.common;

import hats.addons.hatstand.client.core.ClientProxy;
import hats.addons.hatstand.common.core.CommonProxy;
import hats.addons.hatstand.common.core.MapPacketHandler;
import hats.common.Hats;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkModHandler;

@Mod(modid = "HatStand", name = "HatStand",
			version = "1.0.0",
			dependencies = "required-after:Hats@[1.1.0,)"
				)
@NetworkMod(clientSideRequired = true,
			serverSideRequired = false,
			tinyPacketHandler = MapPacketHandler.class
				)
public class HatStand 
{
	public static final String version = "1.0.0";
	
	public static Block blockHatStand;
	
	public static int blockHatStandID = 1300;
	
	public static int renderHatStandID;
	
	@Instance("HatStand")
	public static HatStand instance;

	@SidedProxy(clientSide = "hats.addons.hatstand.client.core.ClientProxy", serverSide = "hats.addons.hatstand.common.core.CommonProxy")
	public static CommonProxy proxy;

	public static File configFile;
	
	public static void handleConfig()
	{
		boolean isClient = proxy instanceof ClientProxy;
		
		Configuration config = new Configuration(configFile);
		config.load();
		
		config.addCustomCategoryComment("globalOptions", "These settings affect both servers and clients that loads the mod.");
		blockHatStandID = addCommentAndReturnBlock(config, "globalOptions", "blockHatStandID", "Block ID for the HatStand", blockHatStandID);
	}
	
	@PreInit
	public void preLoad(FMLPreInitializationEvent event)
	{
		configFile = event.getSuggestedConfigurationFile();
		
		handleConfig();
	}

	@Init
	public void load(FMLInitializationEvent event)
	{
		proxy.initMod();
	}
	
	public static int addCommentAndReturnBlock(Configuration config, String cat, String s, String comment, int i)
	{
		Property prop = config.getBlock(cat, s, i);
		if(!comment.equalsIgnoreCase(""))
		{
			prop.comment = comment;
		}
		return prop.getInt();
	}
	
    public static int getNetId()
    {
    	return ((NetworkModHandler)FMLNetworkHandler.instance().findNetworkModHandler(HatStand.instance)).getNetworkId();
    }
	
}
