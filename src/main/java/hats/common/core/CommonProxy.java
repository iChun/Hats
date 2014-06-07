package hats.common.core;

import cpw.mods.fml.common.FMLCommonHandler;
import hats.api.RenderOnEntityHelper;
import hats.client.core.TickHandlerClient;
import hats.client.render.helper.*;
import hats.common.Hats;
import hats.common.packet.*;
import hats.common.thread.ThreadGetModMobSupport;
import hats.common.thread.ThreadHatsReader;
import ichun.common.core.network.ChannelHandler;
import ichun.common.core.network.PacketHandler;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class CommonProxy 
{
	public static HashMap<Class, RenderOnEntityHelper> renderHelpers = new HashMap<Class, RenderOnEntityHelper>();
	public void initCommands(MinecraftServer server)
	{
		ICommandManager manager = server.getCommandManager();
		if(manager instanceof CommandHandler)
		{
			CommandHandler handler = (CommandHandler)manager;
			handler.registerCommand(new CommandHats());
		}
	}

	public void initMod()
	{
		getHats();

		CommonProxy.renderHelpers.put(EntityBlaze.class			, new HelperBlaze());
		CommonProxy.renderHelpers.put(EntityChicken.class		, new HelperChicken());
		CommonProxy.renderHelpers.put(EntityCow.class			, new HelperCow());
		CommonProxy.renderHelpers.put(EntityCreeper.class		, new HelperCreeper());
		CommonProxy.renderHelpers.put(EntityEnderman.class		, new HelperEnderman());
		CommonProxy.renderHelpers.put(EntityGhast.class			, new HelperGhast());
		CommonProxy.renderHelpers.put(EntityGiantZombie.class	, new HelperGiantZombie());
		CommonProxy.renderHelpers.put(EntityHorse.class			, new HelperHorse());
		CommonProxy.renderHelpers.put(EntityOcelot.class		, new HelperOcelot());
		CommonProxy.renderHelpers.put(EntityPig.class			, new HelperPig());
		CommonProxy.renderHelpers.put(EntityPlayer.class		, new HelperPlayer());
		CommonProxy.renderHelpers.put(EntitySheep.class			, new HelperSheep());
		CommonProxy.renderHelpers.put(EntitySkeleton.class		, new HelperSkeleton());
		CommonProxy.renderHelpers.put(EntitySlime.class			, new HelperSlime());
		CommonProxy.renderHelpers.put(EntitySpider.class		, new HelperSpider());
		CommonProxy.renderHelpers.put(EntitySquid.class			, new HelperSquid());
		CommonProxy.renderHelpers.put(EntityVillager.class		, new HelperVillager());
		CommonProxy.renderHelpers.put(EntityWolf.class			, new HelperWolf());
		CommonProxy.renderHelpers.put(EntityZombie.class		, new HelperZombie());
		CommonProxy.renderHelpers.put(EntityWither.class		, new HelperWither());

        getHatMobModSupport();//Done after initial mapping so that the new JSON can override the mod's vanilla helpers.

        Hats.channels = ChannelHandler.getChannelHandlers("Hats",
                PacketPlayerHatSelection.class,
                PacketRequestHat.class,
                PacketPing.class,
                PacketString.class,
                PacketRequestMobHats.class,
                PacketSession.class,
                PacketTradeReadyInfo.class,
                PacketWornHatList.class,
                PacketMobHatsList.class,
                PacketKingOfTheHatInfo.class,
                PacketTradeOffers.class,
                PacketHatFragment.class
        );

    }

    public void getHatMobModSupport()
    {
        if(Hats.config.getInt("modMobSupport") == 1)
        {
            (new ThreadGetModMobSupport()).start();
        }
    }

    public void initRenderersAndTextures() {}
	
	public void initSounds() {}
	
	public void initTickHandlers() 
	{
		tickHandlerServer = new TickHandlerServer();
        FMLCommonHandler.instance().bus().register(tickHandlerServer);
	}
	
	public void getHats()
	{
		(new ThreadHatsReader(HatHandler.hatsFolder, true, false)).start();
	}
	
    public void getHatsAndOpenGui()
	{
	}
	
	public void clearAllHats()
	{
		HatHandler.hatNames.clear();
		HatHandler.checksums.clear();
		HatHandler.categories.clear();
	}
	
	public void openHatsGui()
	{
	}
	
	public void loadHatFile(File file)
	{
		String hatName = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
		HatHandler.hatNames.put(file, hatName);
	}
	
	public void remap(String duplicate, String original)
	{
		File file = null;
		for(Map.Entry<File, String> e : HatHandler.hatNames.entrySet())
		{
			if(e.getValue().equalsIgnoreCase(original))
			{
				file = e.getKey();
				break;
			}
		}
		if(file != null)
		{
			HatHandler.hatNames.put(file, duplicate);
		}
	}
	
    public void sendPlayerListOfWornHats(EntityPlayer player, boolean sendAllPlayerHatInfo)
    {
    	this.sendPlayerListOfWornHats(player, sendAllPlayerHatInfo, true);
    }
    
    public void sendPlayerListOfWornHats(EntityPlayer player, boolean sendAllPlayerHatInfo, boolean ignorePlayer) //if false send the only player's info to all players
    {
        ArrayList<String> playerNames = new ArrayList<String>();

        if(sendAllPlayerHatInfo)
        {
            Iterator<Entry<String, HatInfo>> ite = Hats.proxy.playerWornHats.entrySet().iterator();

            while(ite.hasNext())
            {
                Entry<String, HatInfo> e = ite.next();

                playerNames.add(e.getKey());
            }

            PacketHandler.sendToPlayer(Hats.channels, new PacketWornHatList(playerNames), player);
        }
        else
        {
            playerNames.add(player.getCommandSenderName());

            PacketWornHatList packet = new PacketWornHatList(playerNames);

            if(ignorePlayer)
            {
                PacketHandler.sendToAllExcept(Hats.channels, packet, player);
            }
            else
            {
                PacketHandler.sendToAll(Hats.channels, packet);
            }
        }
    }
    
	public static HashMap<String, HatInfo> playerWornHats = new HashMap<String, HatInfo>();
	
	public static TickHandlerClient tickHandlerClient;
	public static TickHandlerServer tickHandlerServer;
}
