package me.ichun.mods.hats.common.core;

import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.client.core.TickHandlerClient;
import me.ichun.mods.hats.client.render.helper.*;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.packet.*;
import me.ichun.mods.hats.common.thread.ThreadGetModMobSupport;
import me.ichun.mods.hats.common.thread.ThreadHatsReader;
import me.ichun.mods.ichunutil.common.module.tabula.client.formats.ImportList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CommomProxy
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
		//TODO Every mob between 1.8 and 1.11
        CommomProxy.renderHelpers.put(EntityBat.class			 , new HelperBat());
		CommomProxy.renderHelpers.put(EntityBlaze.class			 , new HelperBlaze());
		CommomProxy.renderHelpers.put(EntityChicken.class		 , new HelperChicken());
		CommomProxy.renderHelpers.put(EntityCow.class			 , new HelperCow());
		CommomProxy.renderHelpers.put(EntityCreeper.class		 , new HelperCreeper());
		CommomProxy.renderHelpers.put(EntityDonkey.class         , new HelperDonkey());
		CommomProxy.renderHelpers.put(EntityEnderman.class		 , new HelperEnderman());
		CommomProxy.renderHelpers.put(EntityEvoker.class		 , new HelperEvoker());
		CommomProxy.renderHelpers.put(EntityGhast.class			 , new HelperGhast());
		CommomProxy.renderHelpers.put(EntityGiantZombie.class	 , new HelperGiantZombie());
		CommomProxy.renderHelpers.put(EntityGuardian.class       , new HelperGuardian());
		CommomProxy.renderHelpers.put(EntityHorse.class			 , new HelperHorse());
		CommomProxy.renderHelpers.put(EntityMule.class           , new HelperMule());
		CommomProxy.renderHelpers.put(EntityOcelot.class		 , new HelperOcelot());
		CommomProxy.renderHelpers.put(EntityPig.class			 , new HelperPig());
		CommomProxy.renderHelpers.put(EntityPlayer.class		 , new HelperPlayer());
		CommomProxy.renderHelpers.put(EntityPolarBear.class      , new HelperPolarBear());
		CommomProxy.renderHelpers.put(EntityStray.class			 , new HelperStray());
		CommomProxy.renderHelpers.put(EntitySheep.class			 , new HelperSheep());
		CommomProxy.renderHelpers.put(EntityShulker.class        , new HelperShulker());
		CommomProxy.renderHelpers.put(EntitySkeleton.class		 , new HelperSkeleton());
		CommomProxy.renderHelpers.put(EntitySkeletonHorse.class  , new HelperSkeletonHorse());
		CommomProxy.renderHelpers.put(EntitySlime.class			 , new HelperSlime());
		CommomProxy.renderHelpers.put(EntitySpider.class		 , new HelperSpider());
		CommomProxy.renderHelpers.put(EntitySquid.class			 , new HelperSquid());
		CommomProxy.renderHelpers.put(EntityVillager.class		 , new HelperVillager());
		CommomProxy.renderHelpers.put(EntityVindicator.class     , new HelperVindictor());
		CommomProxy.renderHelpers.put(EntityVex.class		     , new HelperVex());
		CommomProxy.renderHelpers.put(EntityWitherSkeleton.class , new HelperWitherSkeleton());
		CommomProxy.renderHelpers.put(EntityWolf.class			 , new HelperWolf());
		CommomProxy.renderHelpers.put(EntityZombie.class		 , new HelperZombie());
		CommomProxy.renderHelpers.put(EntityZombieHorse.class    , new HelperZombieHorse());
		CommomProxy.renderHelpers.put(EntityZombieVillager.class , new HelperZombieVillager());
		CommomProxy.renderHelpers.put(EntityWither.class		 , new HelperWither());

        getHatMobModSupport();//Done after initial mapping so that the new JSON can override the mod's vanilla helpers.

        Hats.channel = new PacketChannel("Hats",
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
        if(Hats.config.modMobSupport == 1)
        {
            (new ThreadGetModMobSupport()).start();
        }
    }

	public void initTickHandlers()
	{
		tickHandlerServer = new TickHandlerServer();
        MinecraftForge.EVENT_BUS.register(tickHandlerServer);
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
		HatHandler.getHatNames().clear();
		HatHandler.checksums.clear();
		HatHandler.categories.clear();
	}
	
	public void openHatsGui()
	{
	}
	
	public void loadHatFile(File file)
	{
        if(ImportList.isFileSupported(file) && ImportList.createProjectFromFile(file) != null)
        {
            String hatName = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
            HatHandler.getActualHatNamesMap().put(file, hatName);
        }
	}
	
	public void remap(String duplicate, String original)
	{
		File file = null;
		for(Map.Entry<File, String> e : HatHandler.getActualHatNamesMap().entrySet())
		{
			if(e.getValue().equalsIgnoreCase(original))
			{
				file = e.getKey();
				break;
			}
		}
		if(file != null)
		{
			HatHandler.getActualHatNamesMap().put(file, duplicate);
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

			for (Entry<String, HatInfo> e : Hats.proxy.playerWornHats.entrySet()) {
				playerNames.add(e.getKey());
			}

            Hats.channel.sendTo(new PacketWornHatList(playerNames), player);
        }
        else
        {
            playerNames.add(player.getName());

            PacketWornHatList packet = new PacketWornHatList(playerNames);

            if(ignorePlayer)
            {
                Hats.channel.sendToAllExcept(packet, player);
            }
            else
            {
                Hats.channel.sendToAll(packet);
            }
        }
    }
    
	public HashMap<String, HatInfo> playerWornHats = new HashMap<String, HatInfo>();
	
	public TickHandlerClient tickHandlerClient;
	public TickHandlerServer tickHandlerServer;
}
