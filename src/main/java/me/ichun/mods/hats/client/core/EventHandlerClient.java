package me.ichun.mods.hats.client.core;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.client.gui.GuiHatSelection;
import me.ichun.mods.hats.client.gui.GuiHatUnlocked;
import me.ichun.mods.hats.client.gui.GuiTradeReq;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.core.HatHandler;
import me.ichun.mods.hats.common.core.HatInfo;
import me.ichun.mods.hats.common.core.SessionState;
import me.ichun.mods.hats.common.entity.EntityHat;
import me.ichun.mods.hats.common.packet.PacketPing;
import me.ichun.mods.hats.common.packet.PacketRequestMobHats;
import me.ichun.mods.ichunutil.client.keybind.KeyEvent;
import me.ichun.mods.ichunutil.common.iChunUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EventHandlerClient
{
    public HashMap<String, HatInfo> playerWornHats = new HashMap<>();
    public HashMap<String, EntityHat> hats = new HashMap<>();
    public HashMap<Integer, EntityHat> mobHats = new HashMap<>();

    public HashMap<Integer, EntityHat> rendered = new HashMap<>();

    public HashMap<String, Integer> availableHats = new HashMap<>();
    public HashMap<String, Integer> serverHats = new HashMap<>();
    public ArrayList<String> requestedHats = new ArrayList<>();
    public ArrayList<Integer> requestMobHats = new ArrayList<>();
    public ArrayList<Integer> requestedMobHats = new ArrayList<>();

    public World worldInstance;

    public long clock;

    public long lastHitKey;
    public float rotationYaw;
    public float rotationPitch;

    public double posX;
    public double posY;
    public double posZ;

    public boolean isActive;

    public int currentHatRenders;
    public int requestCooldown;

    public GuiHatUnlocked guiHatUnlocked;
    public GuiTradeReq guiNewTradeReq;

    public String tradeReq;
    public int tradeReqTimeout;

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();

        if(mc.theWorld != null)
        {
            WorldClient world = mc.theWorld;
            if(event.phase == TickEvent.Phase.START)
            {
                currentHatRenders = 0;

                for(Map.Entry<String, EntityHat> e : hats.entrySet())
                {
                    if(e.getValue().parent != null)
                    {
                        EntityHat hat = e.getValue();

                        updateHatPosAndAngle(hat, hat.renderingParent);
                    }
                }

                for(Map.Entry<Integer, EntityHat> e : mobHats.entrySet())
                {
                    if(e.getValue().parent != null)
                    {
                        EntityHat hat = e.getValue();

                        updateHatPosAndAngle(hat, hat.parent);
                    }
                }
            }
            else
            {
                if(guiHatUnlocked == null)
                {
                    guiHatUnlocked = new GuiHatUnlocked(mc);
                }
                guiHatUnlocked.updateGui();
                if(guiNewTradeReq == null)
                {
                    guiNewTradeReq = new GuiTradeReq(mc);
                }
                guiNewTradeReq.updateGui();
            }
        }
    }

    @SubscribeEvent
    public void worldTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().theWorld != null)
        {
            Minecraft mc = Minecraft.getMinecraft();
            WorldClient world = mc.theWorld;

            //            for(int i = 0 ; i < world.loadedEntityList.size(); i++)
            //            {
            //                Entity ent = (Entity)world.loadedEntityList.get(i);
            //                if(ent instanceof EntityPlayer)
            //                {
            //                    continue;
            //                }
            //                ent.renderDistanceWeight = 200D;
            //            }


            if(worldInstance != world)
            {
                worldInstance = world;
                mobHats.clear();
                hats.clear();
                requestMobHats.clear();
                requestedMobHats.clear();
                requestCooldown = 40;
            }
            if(SessionState.showJoinMessage == 1)
            {
                SessionState.showJoinMessage = 0;
                mc.thePlayer.addChatMessage(new TextComponentTranslation(Hats.config.playerHatsMode == 4 ? I18n.translateToLocal("hats.firstJoin.hatHunting") : Hats.config.playerHatsMode == 6 ? I18n.translateToLocal("hats.firstJoin.timeActive") : I18n.translateToLocal("hats.firstJoin.kingOfTheHat.hasKing")));
            }
            if(Hats.config.enableInServersWithoutMod == 1 && SessionState.serverHasMod == 0 || SessionState.serverHasMod == 1)
            {
                for(int i = 0; i < world.playerEntities.size(); i++)
                {
                    EntityPlayer player = world.playerEntities.get(i);
                    if(SessionState.serverHasMod == 0 && Hats.config.shouldOtherPlayersHaveHats == 0 && player != Minecraft.getMinecraft().thePlayer || !player.isEntityAlive())
                    {
                        continue;
                    }

                    EntityHat hat = hats.get(player.getName());
                    if(hat == null || hat.isDead)
                    {
                        if(player.getName().equalsIgnoreCase(mc.thePlayer.getName()))
                        {
                            //Assume respawn
                            for(Map.Entry<String, EntityHat> e : hats.entrySet())
                            {
                                e.getValue().setDead();
                            }
                            for(Map.Entry<Integer, EntityHat> e : mobHats.entrySet())
                            {
                                e.getValue().setDead();
                            }
                            requestedMobHats.clear();
                        }

                        HatInfo hatInfo = (SessionState.serverHasMod == 1 ? getPlayerHat(player.getName()) : ((Hats.config.randomHat == 1 || Hats.config.randomHat == 2 && player != mc.thePlayer) ? HatHandler.getRandomHatFromList(HatHandler.getAllHats(), false) : Hats.favouriteHatInfo));
                        hat = new EntityHat(world, player, hatInfo);
                        hats.put(player.getName(), hat);
                        world.spawnEntityInWorld(hat);
                    }
                }
            }

            if(clock != world.getWorldTime() || !world.getGameRules().getBoolean("doDaylightCycle"))
            {
                //			ScaledResolution reso = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
                //			System.out.println(reso.getScaledHeight());
                clock = world.getWorldTime();
                if(requestCooldown > 0)
                {
                    requestCooldown--;
                }

                if(tradeReqTimeout > 0)
                {
                    tradeReqTimeout--;
                    if(tradeReqTimeout == 0)
                    {
                        if(mc.currentScreen instanceof GuiHatSelection)
                        {
                            ((GuiHatSelection)mc.currentScreen).updateButtonList();
                        }
                        tradeReq = null;
                    }
                }

                if(clock % 5L == 0L && requestCooldown <= 0)
                {
                    if(requestMobHats.size() > 0)
                    {
                        Hats.channel.sendToServer(new PacketRequestMobHats(requestMobHats));
                        requestMobHats.clear();
                    }
                }

                if(Hats.config.playerHatsMode == 6)
                {
                    lastHitKey++;

                    if(Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()))
                    {
                        lastHitKey = 0;
                    }
                    if(clock % 107L == 0)
                    {
                        if((lastHitKey > 100 || lastHitKey == 0 && posX == mc.thePlayer.posX && posY == mc.thePlayer.posY && posZ == mc.thePlayer.posZ) && (rotationYaw == mc.thePlayer.rotationYaw && rotationPitch == mc.thePlayer.rotationPitch || posX == mc.thePlayer.posX && posY == mc.thePlayer.posY && posZ == mc.thePlayer.posZ || mc.thePlayer.ridingEntity != null) || iChunUtil.eventHandlerClient.hasScreen)
                        {
                            if(isActive)
                            {
                                isActive = false;

                                Hats.channel.sendToServer(new PacketPing(1, false));
                            }
                        }
                        else
                        {
                            if(!isActive)
                            {
                                isActive = true;

                                Hats.channel.sendToServer(new PacketPing(1, true));
                            }
                        }
                        rotationYaw = mc.thePlayer.rotationYaw;
                        rotationPitch = mc.thePlayer.rotationPitch;
                        posX = mc.thePlayer.posX;
                        posY = mc.thePlayer.posY;
                        posZ = mc.thePlayer.posZ;
                    }
                }
            }

            if(Hats.config.randomMobHat > 0 && !(SessionState.serverHasMod == 1 && Hats.config.playerHatsMode == 4) || SessionState.serverHasMod == 1 && Hats.config.playerHatsMode == 4)
            {
                for(int i = 0; i < world.loadedEntityList.size(); i++)
                {
                    Entity ent = world.loadedEntityList.get(i);
                    if(!(ent instanceof EntityLivingBase) || !(SessionState.serverHasMod == 1 && Hats.config.playerHatsMode == 4) && !HatHandler.canMobHat((EntityLivingBase)ent) || ent instanceof EntityPlayer)
                    {
                        continue;
                    }

                    EntityLivingBase living = (EntityLivingBase)ent;

                    EntityHat hat = mobHats.get(living.getEntityId());
                    if(hat == null || hat.isDead)
                    {
                        if(SessionState.serverHasMod == 0 || Hats.config.playerHatsMode != 4)
                        {
                            HatInfo hatInfo = living.getRNG().nextFloat() < ((float)Hats.config.randomMobHat / 100F) ? (Hats.config.randomHat >= 1 ? HatHandler.getRandomHatFromList(HatHandler.getAllHats(), false) : Hats.favouriteHatInfo) : new HatInfo();
                            hat = new EntityHat(world, living, hatInfo);
                            mobHats.put(living.getEntityId(), hat);
                            world.spawnEntityInWorld(hat);
                        }
                        else if(!requestMobHats.contains(living.getEntityId()) && !requestedMobHats.contains(living.getEntityId()))
                        {
                            requestMobHats.add(living.getEntityId());
                            requestedMobHats.add(living.getEntityId());
                        }
                    }
                }
            }

            Iterator<Map.Entry<String, EntityHat>> ite = hats.entrySet().iterator();

            while(ite.hasNext())
            {
                Map.Entry<String, EntityHat> e = ite.next();
                if(e.getValue().worldObj.provider.getDimension() != world.provider.getDimension() || (world.getWorldTime() - e.getValue().lastUpdate) > 10L)
                {
                    e.getValue().setDead();
                    ite.remove();
                }
            }

            Iterator<Map.Entry<Integer, EntityHat>> ite1 = mobHats.entrySet().iterator();

            while(ite1.hasNext())
            {
                Map.Entry<Integer, EntityHat> e = ite1.next();
                if(e.getValue().worldObj.provider.getDimension() != world.provider.getDimension() || (world.getWorldTime() - e.getValue().lastUpdate) > 10L)
                {
                    e.getValue().setDead();
                    ite1.remove();
                }
            }
        }
    }

    public void updateHatPosAndAngle(EntityHat hat, EntityLivingBase parent)
    {
        hat.lastTickPosX = hat.parent.lastTickPosX;
        hat.lastTickPosY = hat.parent.lastTickPosY;
        hat.lastTickPosZ = hat.parent.lastTickPosZ;

        hat.prevPosX = hat.parent.prevPosX;
        hat.prevPosY = hat.parent.prevPosY;
        hat.prevPosZ = hat.parent.prevPosZ;

        hat.posX = hat.parent.posX;
        hat.posY = hat.parent.posY;
        hat.posZ = hat.parent.posZ;

        RenderOnEntityHelper helper = HatHandler.getRenderHelper(parent.getClass());

        if(helper != null)
        {
            hat.prevRotationPitch = helper.getPrevRotationPitch(parent);
            hat.rotationPitch = helper.getRotationPitch(parent);

            hat.prevRotationYaw = helper.getPrevRotationYaw(parent);
            hat.rotationYaw = helper.getRotationYaw(parent);
        }
    }

    public HatInfo getPlayerHat(String s)
    {
        HatInfo name = playerWornHats.get(s);
        if(name == null)
        {
            if(Hats.config.playerHatsMode == 2)
            {
                return new HatInfo(Hats.config.lockedHat.toLowerCase());
            }
            return new HatInfo();
        }
        return name;
    }

    @SubscribeEvent
    public void onKeyEvent(KeyEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.keyBind.isPressed() && event.keyBind == Hats.config.guiKeyBind)
        {
            if(mc.currentScreen == null && !iChunUtil.eventHandlerClient.hasScreen)
            {
                if(Hats.config.playerHatsMode == 3)
                {
                    Hats.channel.sendToServer(new PacketPing(0, false));
                }
                else if(Hats.config.playerHatsMode == 2)
                {
                    mc.thePlayer.addChatMessage(new TextComponentTranslation("hats.lockedMode"));
                }
                else if(Hats.config.playerHatsMode == 5 && !SessionState.currentKing.equalsIgnoreCase(mc.thePlayer.getName()))
                {
                    mc.thePlayer.addChatMessage(new TextComponentTranslation("hats.kingOfTheHat.notKing", SessionState.currentKing));
                }
                else
                {
                    Hats.proxy.openHatsGui();
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        Hats.eventHandlerClient.isActive = true;

        SessionState.serverHasMod = 0;
        SessionState.currentKing = "";
        SessionState.showJoinMessage = 0;
        SessionState.hasVisited = 1;

        HatHandler.repopulateHatsList();
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        Hats.eventHandlerClient.hats.clear();
        Hats.eventHandlerClient.mobHats.clear();
        Hats.eventHandlerClient.playerWornHats.clear();
        Hats.eventHandlerClient.requestedHats.clear();
        if(Hats.eventHandlerClient.guiHatUnlocked != null)
        {
            Hats.eventHandlerClient.guiHatUnlocked.hatList.clear();
        }
        if(Hats.eventHandlerClient.guiNewTradeReq != null)
        {
            Hats.eventHandlerClient.guiNewTradeReq.hatList.clear();
        }
        Hats.eventHandlerClient.worldInstance = null;
    }
}
