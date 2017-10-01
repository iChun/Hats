package me.ichun.mods.hats.common.core;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.client.render.helper.*;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.packet.*;
import me.ichun.mods.hats.common.thread.ThreadGetModMobSupport;
import me.ichun.mods.hats.common.thread.ThreadHatsReader;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.module.tabula.formats.ImportList;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ProxyCommon
{
    public static HashMap<Class, RenderOnEntityHelper> renderHelpers = new HashMap<>();
    public void initCommands(MinecraftServer server)
    {
        ICommandManager manager = server.getCommandManager();
        if(manager instanceof CommandHandler)
        {
            CommandHandler handler = (CommandHandler)manager;
            handler.registerCommand(new CommandHats());
        }
    }

    public void preInitMod()
    {
        getHats();

        ProxyCommon.renderHelpers.put(AbstractHorse.class		, new HelperHorse());
        ProxyCommon.renderHelpers.put(AbstractIllager.class		, new HelperIllager());
        ProxyCommon.renderHelpers.put(AbstractSkeleton.class	, new HelperSkeleton());
        ProxyCommon.renderHelpers.put(EntityBat.class			, new HelperBat());
        ProxyCommon.renderHelpers.put(EntityBlaze.class			, new HelperBlaze());
        ProxyCommon.renderHelpers.put(EntityChicken.class		, new HelperChicken());
        ProxyCommon.renderHelpers.put(EntityCow.class			, new HelperCow());
        ProxyCommon.renderHelpers.put(EntityCreeper.class		, new HelperCreeper());
        ProxyCommon.renderHelpers.put(EntityEnderman.class		, new HelperEnderman());
        ProxyCommon.renderHelpers.put(EntityGhast.class			, new HelperGhast());
        ProxyCommon.renderHelpers.put(EntityGiantZombie.class	, new HelperGiantZombie());
        ProxyCommon.renderHelpers.put(EntityGuardian.class		, new HelperGuardian());
        ProxyCommon.renderHelpers.put(EntityLlama.class		    , new HelperLlama());
        ProxyCommon.renderHelpers.put(EntityOcelot.class		, new HelperOcelot());
        ProxyCommon.renderHelpers.put(EntityPig.class			, new HelperPig());
        ProxyCommon.renderHelpers.put(EntityPlayer.class		, new HelperPlayer());
        ProxyCommon.renderHelpers.put(EntityPolarBear.class		, new HelperPolarBear());
        ProxyCommon.renderHelpers.put(EntityRabbit.class		, new HelperRabbit());
        ProxyCommon.renderHelpers.put(EntitySheep.class			, new HelperSheep());
        ProxyCommon.renderHelpers.put(EntityShulker.class		, new HelperShulker());
        ProxyCommon.renderHelpers.put(EntitySlime.class			, new HelperSlime());
        ProxyCommon.renderHelpers.put(EntitySpider.class		, new HelperSpider());
        ProxyCommon.renderHelpers.put(EntitySquid.class			, new HelperSquid());
        ProxyCommon.renderHelpers.put(EntityVex.class	    	, new HelperVex());
        ProxyCommon.renderHelpers.put(EntityVillager.class		, new HelperVillager());
        ProxyCommon.renderHelpers.put(EntityWither.class		, new HelperWither());
        ProxyCommon.renderHelpers.put(EntityWolf.class			, new HelperWolf());
        ProxyCommon.renderHelpers.put(EntityZombie.class		, new HelperZombie());

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

        Hats.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(Hats.eventHandlerServer);

    }

    public void getHatMobModSupport()
    {
        if(Hats.config.modMobSupport == 1)
        {
            (new ThreadGetModMobSupport()).start();
        }
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
        ArrayList<String> playerNames = new ArrayList<>();

        if(sendAllPlayerHatInfo)
        {

            for(Entry<String, HatInfo> e : Hats.proxy.playerWornHats.entrySet())
            {
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

    public HashMap<String, HatInfo> playerWornHats = new HashMap<>();
}
