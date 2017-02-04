package me.ichun.mods.hats.common.core;

import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.ichunutil.client.keybind.KeyBind;
import me.ichun.mods.ichunutil.common.core.config.ConfigBase;
import me.ichun.mods.ichunutil.common.core.config.annotations.ConfigProp;
import me.ichun.mods.ichunutil.common.core.config.annotations.IntBool;
import me.ichun.mods.ichunutil.common.core.config.annotations.IntMinMax;
import me.ichun.mods.ichunutil.common.core.config.types.Colour;

import java.io.File;
import java.util.Random;

public class Config extends ConfigBase
{
    @ConfigProp(category = "globalOptions")
    @IntBool
    public int safeLoad = 1;

    @ConfigProp(category = "globalOptions")
    @IntBool
    public int allowSendingOfHats = 1;

    @ConfigProp(category = "globalOptions")
    @IntBool
    public int allowReceivingOfHats = 1;

    @ConfigProp(category = "globalOptions")
    @IntBool
    public int modMobSupport = 1;

    @ConfigProp(category = "globalOptions")
    @IntBool
    public int readLocalModMobSupport = 0;

    @ConfigProp(category = "serverOptions", useSession = true)
    @IntMinMax(min = 1, max = 6)
    public int playerHatsMode = 4;

    @ConfigProp(category = "serverOptions")
    @IntBool
    public int firstJoinMessage = 1;

    @ConfigProp(category = "serverOptions", useSession = true)
    public String lockedHat = "Straw Hat";

    @ConfigProp(category = "serverOptions")
    @IntMinMax(min = 10)
    public int startTime = 6000;

    @ConfigProp(category = "serverOptions")
    @IntMinMax(min = 0)
    public int timeIncrement = 125;

    @ConfigProp(category = "serverOptions")
    @IntBool
    public int resetPlayerHatsOnDeath = 0;

    @ConfigProp(category = "serverOptions")
    @IntBool
    public int hatRarity = 1;

    @ConfigProp(category = "serverOptions", useSession = true)
    public int hatGenerationSeed = (new Random(System.currentTimeMillis())).nextInt();

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntBool
    public int renderInFirstPerson = 0;

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntBool
    public int enableInServersWithoutMod = 1;

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntBool
    public int shouldOtherPlayersHaveHats = 1;

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntMinMax(min = 0 , max = 2)
    public int randomHat = 2;

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public String favouriteHat = "Top Hat";

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public Colour favouriteHatColourizer = new Colour(0xffffff);

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public KeyBind guiKeyBind = new KeyBind(Keyboard.KEY_H, false, false, false, false);

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    public String personalizeEnabled = "1 2 3 4 5 6 7 8 9";

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntMinMax(min = 0, max = 5000)
    public int maxHatRenders = 300;

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntBool
    public int showContributorHatsInGui = 1;

    @ConfigProp(category = "clientOnly", side = Side.CLIENT)
    @IntBool
    public int renderHats = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntMinMax(min = 0, max = 100)
    public int randomMobHat = 10;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatBat = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatBlaze = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatChicken = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatCow = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatCreeper = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatDonkey = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatEnderman = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatEvoker = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatGhast = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatHorse = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatMule = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatOcelot = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatPig = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatPolarBear = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatStray = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatShulker = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatSheep = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatSkeleton = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatSlime = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatSpider = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatSquid = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatVillager = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatVindictor = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatVex = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatWither = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatWolf = 1;

    @ConfigProp(category = "randoMobOptions")
    @IntBool
    public int hatZombie = 1;

    public Config(File file)
    {
        super(file);
    }

    @Override
    public String getModId()
    {
        return "hats";
    }

    @Override
    public String getModName()
    {
        return "Hats";
    }

    @Override
    public void setup()
    {
        super.setup();
        Hats.favouriteHatInfo = new HatInfo(favouriteHat.toLowerCase(), favouriteHatColourizer.r, favouriteHatColourizer.g, favouriteHatColourizer.b, 255);
    }
}
