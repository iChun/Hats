package me.ichun.mods.hats.addons.hatstand.common;

import me.ichun.mods.hats.addons.hatstand.common.block.BlockHatStand;
import me.ichun.mods.hats.addons.hatstand.common.core.ProxyCommon;
import me.ichun.mods.hats.addons.hatstand.common.item.ItemHatStand;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.ichunutil.common.module.update.UpdateChecker;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

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

        MinecraftForge.EVENT_BUS.register(this);

        UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo(MOD_NAME, iChunUtil.VERSION_OF_MC, VERSION, false));
    }

    @SubscribeEvent
    public void onRegisterBlock(RegistryEvent.Register<Block> event)
    {
        HatStand.blockHatStand = (new BlockHatStand(Material.WOOD)).setHardness(0.5F).setCreativeTab(CreativeTabs.DECORATIONS).setRegistryName(new ResourceLocation("hatstand", "hatstand")).setUnlocalizedName("hats.addon.hatstands.block");
        event.getRegistry().register(HatStand.blockHatStand);
    }

    @SubscribeEvent
    public void onRegisterItem(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(new ItemHatStand(HatStand.blockHatStand).setRegistryName(HatStand.blockHatStand.getRegistryName()));
    }
}
