package hats.common;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.relauncher.Side;
import hats.client.core.ClientProxy;
import hats.common.core.CommonProxy;
import hats.common.core.EventHandler;
import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
import ichun.common.core.config.Config;
import ichun.common.core.config.ConfigHandler;
import ichun.common.core.config.IConfigUser;
import ichun.common.core.updateChecker.ModVersionChecker;
import ichun.common.core.updateChecker.ModVersionInfo;
import ichun.common.iChunUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.EnumMap;
import java.util.Random;

@Mod(modid = "Hats", name = "Hats",
        version = Hats.version,
        dependencies = "required-after:iChunUtil@[" + iChunUtil.versionMC +".0.0,)",
        acceptableRemoteVersions = "[" + iChunUtil.versionMC +".0.0," + iChunUtil.versionMC + ".1.0)"
)
public class Hats
        implements IConfigUser
{
    public static final String version = iChunUtil.versionMC +".0.0";

    private static Logger logger = LogManager.getLogger("Hats");

    public static EnumMap<Side, FMLEmbeddedChannel> channels;

    public static Config config;

    public static HatInfo favouriteHatInfo = new HatInfo();

    public static boolean hasMorphMod = false;

    @Instance("Hats")
    public static Hats instance;

    @SidedProxy(clientSide = "hats.client.core.ClientProxy", serverSide = "hats.common.core.CommonProxy")
    public static CommonProxy proxy;

    @Override
    public boolean onConfigChange(Config cfg, Property prop)
    {
        return true;
    }

    @Mod.EventHandler
    public void preLoad(FMLPreInitializationEvent event)
    {
        HatHandler.hatsFolder = new File(event.getModConfigurationDirectory().getParent(), "/hats");

        if(!HatHandler.hatsFolder.exists())
        {
            HatHandler.hatsFolder.mkdirs();
        }

        boolean isClient = proxy instanceof ClientProxy;

        config = ConfigHandler.createConfig(event.getSuggestedConfigurationFile(), "hats", "Hats", logger, instance);

        config.setCurrentCategory("globalOptions", "hats.config.cat.globalOptions.name", "hats.config.cat.globalOptions.comment");
        config.createIntBoolProperty("safeLoad", "hats.config.prop.safeLoad.name", "hats.config.prop.safeLoad.comment", true, false, true);
        config.createIntBoolProperty("allowSendingOfHats", "hats.config.prop.allowSendingOfHats.name", "hats.config.prop.allowSendingOfHats.comment", true, false, true);
        config.createIntBoolProperty("allowReceivingOfHats", "hats.config.prop.allowReceivingOfHats.name", "hats.config.prop.allowReceivingOfHats.comment", true, false, true);
        config.createIntBoolProperty("modMobSupport", "hats.config.prop.modMobSupport.name", "hats.config.prop.modMobSupport.comment", true, false, true);
        config.createIntBoolProperty("readLocalModMobSupport", "hats.config.prop.readLocalModMobSupport.name", "hats.config.prop.readLocalModMobSupport.comment", true, false, false);

        config.setCurrentCategory("serverOptions", "hats.config.cat.serverOptions.name", "hats.config.cat.serverOptions.comment");
        config.createIntProperty("playerHatsMode", "hats.config.prop.playerHatsMode.name", "hats.config.prop.playerHatsMode.comment", true, true, 4, 1, 6);
        config.createIntBoolProperty("firstJoinMessage", "hats.config.prop.firstJoinMessage.name", "hats.config.prop.firstJoinMessage.comment", true, false, true);
        config.createStringProperty("lockedHat", "hats.config.prop.lockedHat.name", "hats.config.prop.lockedHat.comment", true, true, "Straw Hat");
        config.createIntProperty("startTime", "hats.config.prop.startTime.name", "hats.config.prop.startTime.comment", false, false, 6000, 10, Integer.MAX_VALUE);
        config.createIntProperty("timeIncrement", "hats.config.prop.timeIncrement.name", "hats.config.prop.timeIncrement.comment", false, false, 125, 0, Integer.MAX_VALUE);
        config.createIntBoolProperty("resetPlayerHatsOnDeath", "hats.config.prop.resetPlayerHatsOnDeath.name", "hats.config.prop.resetPlayerHatsOnDeath.comment", true, false, false);
        config.createIntBoolProperty("hatRarity", "hats.config.prop.hatRarity.name", "hats.config.prop.hatRarity.comment", true, false, true);
        config.createIntProperty("hatGenerationSeed", "hats.config.prop.hatGenerationSeed.name", "hats.config.prop.hatGenerationSeed.comment", true, true, (new Random(System.currentTimeMillis())).nextInt(), Integer.MIN_VALUE, Integer.MAX_VALUE);

        if(isClient)
        {
            config.setCurrentCategory("clientOnly", "hats.config.cat.clientOnly.name", "hats.config.cat.clientOnly.comment");
            config.createIntBoolProperty("renderInFirstPerson", "hats.config.prop.renderInFirstPerson.name", "hats.config.prop.renderInFirstPerson.comment", true, false, false);
            config.createIntBoolProperty("enableInServersWithoutMod", "hats.config.prop.enableInServersWithoutMod.name", "hats.config.prop.enableInServersWithoutMod.comment", true, false, true);
            config.createIntBoolProperty("shouldOtherPlayersHaveHats", "hats.config.prop.shouldOtherPlayersHaveHats.name", "hats.config.prop.shouldOtherPlayersHaveHats.comment", true, false, true);
            config.createIntProperty("randomHat", "hats.config.prop.randomHat.name", "hats.config.prop.randomHat.comment", true, false, 2, 0, 2);

            String favHat = config.createStringProperty("favouriteHat", "hats.config.prop.favouriteHat.name", "hats.config.prop.favouriteHat.comment", true, false, "Top Hat");
            int clr = config.createColourProperty("favouriteHatColourizer", "hats.config.prop.favouriteHatColourizer.name", "hats.config.prop.favouriteHatColourizer.comment", true, false, 0xffffff);
            favouriteHatInfo = new HatInfo(favHat.toLowerCase(), clr >> 16 & 255, clr >> 8 & 255, clr & 255, 255);

            config.createKeybindProperty("guiKeyBind", "hats.config.prop.guiKeyBind.name", "hats.config.prop.guiKeyBind.comment", Keyboard.KEY_H, false, false, false, false, 0, true);
            config.createStringProperty("personalizeEnabled", "hats.config.prop.personalizeEnabled.name", "hats.config.prop.personalizeEnabled.comment", true, false, "1 2 3 4 5 6 7 8 9");
            config.createIntProperty("maxHatRenders", "hats.config.prop.maxHatRenders.name", "hats.config.prop.maxHatRenders.comment", true, false, 300, 0, 5000);
            config.createIntBoolProperty("showContributorHatsInGui", "hats.config.prop.showContributorHatsInGui.name", "hats.config.prop.showContributorHatsInGui.comment", true, false, true);

            config.createIntBoolProperty("renderHats", "hats.config.prop.renderHats.name", "hats.config.prop.renderHats.comment", true, true, true);
        }

        config.setCurrentCategory("randoMobOptions", "hats.config.cat.randoMobOptions.name", "hats.config.cat.randoMobOptions.comment");
        config.createIntProperty("randomMobHat", "hats.config.prop.randomMobHat.name", "hats.config.prop.randomMobHat.comment", true, false, config.getInt("playerHatsMode") != 4 && isClient ? 0 : 10, 0, 100);
        config.createIntProperty("useRandomContributorHats", "hats.config.prop.useRandomContributorHats.name", "hats.config.prop.useRandomContributorHats.comment", true, false, 80, 0, 100);

        config.createIntBoolProperty("hatBlaze"     , "hats.config.prop.hatBlaze.name"      , "hats.config.prop.hatBlaze.comment"   , true, false, true);
        config.createIntBoolProperty("hatChicken"   , "hats.config.prop.hatChicken.name"    , "hats.config.prop.hatChicken.comment" , true, false, true);
        config.createIntBoolProperty("hatCow"       , "hats.config.prop.hatCow.name"        , "hats.config.prop.hatCow.comment"     , true, false, true);
        config.createIntBoolProperty("hatCreeper"   , "hats.config.prop.hatCreeper.name"    , "hats.config.prop.hatCreeper.comment" , true, false, true);
        config.createIntBoolProperty("hatEnderman"  , "hats.config.prop.hatEnderman.name"   , "hats.config.prop.hatEnderman.comment", true, false, true);
        config.createIntBoolProperty("hatGhast"     , "hats.config.prop.hatGhast.name"      , "hats.config.prop.hatGhast.comment"   , true, false, true);
        config.createIntBoolProperty("hatHorse"     , "hats.config.prop.hatHorse.name"      , "hats.config.prop.hatHorse.comment"   , true, false, true);
        config.createIntBoolProperty("hatOcelot"    , "hats.config.prop.hatOcelot.name"     , "hats.config.prop.hatOcelot.comment"  , true, false, true);
        config.createIntBoolProperty("hatPig"       , "hats.config.prop.hatPig.name"        , "hats.config.prop.hatPig.comment"     , true, false, true);
        config.createIntBoolProperty("hatSheep"     , "hats.config.prop.hatSheep.name"      , "hats.config.prop.hatSheep.comment"   , true, false, true);
        config.createIntBoolProperty("hatSkeleton"  , "hats.config.prop.hatSkeleton.name"   , "hats.config.prop.hatSkeleton.comment", true, false, true);
        config.createIntBoolProperty("hatSlime"     , "hats.config.prop.hatSlime.name"      , "hats.config.prop.hatSlime.comment"   , true, false, true);
        config.createIntBoolProperty("hatSpider"    , "hats.config.prop.hatSpider.name"     , "hats.config.prop.hatSpider.comment"  , true, false, true);
        config.createIntBoolProperty("hatSquid"     , "hats.config.prop.hatSquid.name"      , "hats.config.prop.hatSquid.comment"   , true, false, true);
        config.createIntBoolProperty("hatVillager"  , "hats.config.prop.hatVillager.name"   , "hats.config.prop.hatVillager.comment", true, false, true);
        config.createIntBoolProperty("hatWither"    , "hats.config.prop.hatWither.name"     , "hats.config.prop.hatWither.comment"  , true, false, true);
        config.createIntBoolProperty("hatWolf"      , "hats.config.prop.hatWolf.name"       , "hats.config.prop.hatWolf.comment"    , true, false, true);
        config.createIntBoolProperty("hatZombie"    , "hats.config.prop.hatZombie.name"     , "hats.config.prop.hatZombie.comment"  , true, false, true);

        //		handleConfig();

        ModVersionChecker.register_iChunMod(new ModVersionInfo("Hats", "1.7", version, false));
    }

    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
        proxy.initMod();
        proxy.initTickHandlers();

        proxy.initRenderersAndTextures();

        EventHandler eventHandler = new EventHandler();

        FMLCommonHandler.instance().bus().register(eventHandler);
        MinecraftForge.EVENT_BUS.register(eventHandler);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        if(FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            try
            {
                Class clz = Class.forName("morph.common.Morph");
                hasMorphMod = true;
            }
            catch(Exception e)
            {
            }
        }

        //GSON gen test

        //        Map<String, Map<String, Object>> className = new HashMap<String, Map<String, Object>>();
        //
        //        Map<String, Object> props = new HashMap<String, Object>();
        //
        //        props.put("rotatePointVert", 20.2F/16F);
        //        props.put("rotatePointHori", 8F/16F);
        //        props.put("offsetPointVert", 4F/16F);
        //        props.put("offsetPointHori", 2F/16F);
        //
        //        className.put("net.minecraft.entity.passive.EntityCow", props);
        //
        //        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //        String jsonOutput = gson.toJson(className);
        //
        //        System.out.println(jsonOutput);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerAboutToStartEvent event)
    {
        Hats.config.resetSession();
        Hats.config.updateSession("serverHasMod", 1);
        Hats.config.updateSession("currentKing", "");

        //		SessionState.serverHasMod = true;
        //		SessionState.serverHatMode = playerHatsMode;
        //		SessionState.serverHat = lockedHat;

        proxy.initCommands(event.getServer());
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event)
    {
        proxy.tickHandlerServer.mobHats.clear();
        proxy.tickHandlerServer.playerHats.clear();
        proxy.tickHandlerServer.playerActivity.clear();
        proxy.tickHandlerServer.playerTradeRequests.clear();
        proxy.tickHandlerServer.activeTrades.clear();
        proxy.playerWornHats.clear();
    }

    public static void console(String s, boolean warning)
    {
        StringBuilder sb = new StringBuilder();
        logger.log(warning ? Level.WARN : Level.INFO, sb.append("[").append(version).append("] ").append(s).toString());
    }

    public static void console(String s)
    {
        console(s, false);
    }
}
