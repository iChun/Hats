package hats.common;

import hats.client.core.ClientProxy;
import hats.common.core.CommonProxy;
import hats.common.core.ConnectionHandler;
import hats.common.core.LoggerHelper;

import java.io.File;
import java.util.logging.Level;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarted;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.Mod.ServerStopping;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkModHandler;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "Hats", name = "Hats",
			version = "1.0.0"
				)
@NetworkMod(clientSideRequired = true,
			serverSideRequired = false,
			connectionHandler = ConnectionHandler.class
				)
public class Hats 
{
	public static final String version = "1.0.0";
	
	public static int renderInFirstPerson;
	
	@Instance("Hats")
	public static Hats instance;
	
	@SidedProxy(clientSide = "hats.client.core.ClientProxy", serverSide = "hats.common.core.CommonProxy")
	public static CommonProxy proxy;
	
	@PreInit
	public void preLoad(FMLPreInitializationEvent event)
	{
		LoggerHelper.init();
		
		proxy.hatsFolder = new File(event.getModConfigurationDirectory().getParent(), "/mods/hats");
		
		if(!proxy.hatsFolder.exists())
		{
			proxy.hatsFolder.mkdirs();
		}
		
		boolean isClient = proxy instanceof ClientProxy;
		
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		if(isClient)
		{
			config.addCustomCategoryComment("clientOnly", "These settings affect only the client that loads the mod.");
			
			renderInFirstPerson = addCommentAndReturnInt(config, "clientOnly", "renderInFirstPerson", "Should your hat render in first person?", 0);
		}
		
		config.save();

	}
	
	@Init
	public void load(FMLInitializationEvent event)
	{
		proxy.initMod();
		proxy.initTickHandlers();
		
		proxy.initRenderersAndTextures();
		
		GameRegistry.registerPlayerTracker(new ConnectionHandler());
		
	}
	
	@PostInit
	public void postInit(FMLPostInitializationEvent event)
	{
		
	}
	
	public static int addCommentAndReturnInt(Configuration config, String cat, String s, String comment, int i) //Taken from iChun Util
	{
		Property prop = config.get(cat, s, i);
		if(!comment.equalsIgnoreCase(""))
		{
			prop.comment = comment;
		}
		return prop.getInt();
	}
	
	@ServerStarting
	public void serverStarting(FMLServerStartingEvent event)
	{

	}
	
	@ServerStarted
	public void serverStarted(FMLServerStartedEvent event)
	{
	}
	
	@ServerStopping
	public void serverStopping(FMLServerStoppingEvent event)
	{
	}
	
    public static int getNetId()
    {
    	return ((NetworkModHandler)FMLNetworkHandler.instance().findNetworkModHandler(Hats.instance)).getNetworkId();
    }

    
    public static void console(String s, boolean warning)
    {
    	StringBuilder sb = new StringBuilder();
    	LoggerHelper.log(warning ? Level.WARNING : Level.INFO, sb.append("[").append(version).append("] ").append(s).toString());
    }

    public static void console(String s)
    {
    	console(s, false);
    }
}
