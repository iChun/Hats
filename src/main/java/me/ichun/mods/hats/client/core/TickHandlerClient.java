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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class TickHandlerClient
{
    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();

        if(mc.world != null)
        {
            WorldClient world = mc.world;
            if(event.phase == TickEvent.Phase.START)
            {
                currentHatRenders = 0;

                for (Entry<String, EntityHat> e : hats.entrySet()) {
                    if (e.getValue().parent != null) {
                        EntityHat hat = e.getValue();

                        updateHatPosAndAngle(hat, hat.renderingParent);
                    }
                }

                for (Entry<Integer, EntityHat> e : mobHats.entrySet()) {
                    if (e.getValue().parent != null) {
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
        if(event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().world != null)
        {
            Minecraft mc = Minecraft.getMinecraft();
            WorldClient world = mc.world;

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
                mc.ingameGUI.getChatGUI().addToSentMessages(Hats.config.playerHatsMode == 4 ? I18n.format("hats.firstJoin.hatHunting") : Hats.config.playerHatsMode == 6 ? I18n.format("hats.firstJoin.timeActive") : I18n.format("hats.firstJoin.kingOfTheHat.hasKing"));
            }
            if(Hats.config.enableInServersWithoutMod == 1 && SessionState.serverHasMod == 0 || SessionState.serverHasMod == 1)
            {
                for(int i = 0; i < world.playerEntities.size(); i++)
                {
                    EntityPlayer player = world.playerEntities.get(i);
                    if(SessionState.serverHasMod == 0 && Hats.config.shouldOtherPlayersHaveHats == 0 && player != Minecraft.getMinecraft().player || !player.isEntityAlive())
                    {
                        continue;
                    }

                    EntityHat hat = hats.get(player.getName());
                    if(hat == null || hat.isDead)
                    {
                        if(player.getName().equalsIgnoreCase(mc.player.getName()))
                        {
                            //Assume respawn
                            for(Entry<String, EntityHat> e : hats.entrySet())
                            {
                                e.getValue().setDead();
                            }
                            for(Entry<Integer, EntityHat> e : mobHats.entrySet())
                            {
                                e.getValue().setDead();
                            }
                            requestedMobHats.clear();
                        }

                        HatInfo hatInfo = (SessionState.serverHasMod == 1 ? getPlayerHat(player.getName()) : ((Hats.config.randomHat == 1 || Hats.config.randomHat == 2 && player != mc.player) ? HatHandler.getRandomHatFromList(HatHandler.getAllHats(), false) : Hats.favouriteHatInfo));
                        hat = new EntityHat(world, player, hatInfo);
                        hats.put(player.getName(), hat);
                        world.spawnEntity(hat);
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
                        if((lastHitKey > 100 || lastHitKey == 0 && posX == mc.player.posX && posY == mc.player.posY && posZ == mc.player.posZ) && (rotationYaw == mc.player.rotationYaw && rotationPitch == mc.player.rotationPitch || posX == mc.player.posX && posY == mc.player.posY && posZ == mc.player.posZ || mc.player.isRiding()) || hasScreen)
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
                        rotationYaw = mc.player.rotationYaw;
                        rotationPitch = mc.player.rotationPitch;
                        posX = mc.player.posX;
                        posY = mc.player.posY;
                        posZ = mc.player.posZ;
                    }
                }
            }

            if(Hats.config.randomMobHat > 0 && !(SessionState.serverHasMod == 1 && Hats.config.playerHatsMode == 4) || SessionState.serverHasMod == 1 && Hats.config.playerHatsMode == 4)
            {
                for(int i = 0; i < world.loadedEntityList.size(); i++)
                {
                    Entity ent = (Entity)world.loadedEntityList.get(i);
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
                            world.spawnEntity(hat);
                        }
                        else if(!requestMobHats.contains(living.getEntityId()) && !requestedMobHats.contains(living.getEntityId()))
                        {
                            requestMobHats.add(living.getEntityId());
                            requestedMobHats.add(living.getEntityId());
                        }
                    }
                }
            }

            Iterator<Entry<String, EntityHat>> ite = hats.entrySet().iterator();

            while(ite.hasNext())
            {
                Entry<String, EntityHat> e = ite.next();
                if(e.getValue().world.provider.getDimension() != world.provider.getDimension() || (world.getWorldTime() - e.getValue().lastUpdate) > 10L)
                {
                    e.getValue().setDead();
                    ite.remove();
                }
            }

            Iterator<Entry<Integer, EntityHat>> ite1 = mobHats.entrySet().iterator();

            while(ite1.hasNext())
            {
                Entry<Integer, EntityHat> e = ite1.next();
                if(e.getValue().world.provider.getDimension() != world.provider.getDimension() || (world.getWorldTime() - e.getValue().lastUpdate) > 10L)
                {
                    e.getValue().setDead();
                    ite1.remove();
                }
            }

            hasScreen = mc.currentScreen != null;
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

    public HashMap<String, HatInfo> playerWornHats = new HashMap<String, HatInfo>();
    public HashMap<String, EntityHat> hats = new HashMap<String, EntityHat>();
    public HashMap<Integer, EntityHat> mobHats = new HashMap<Integer, EntityHat>();

    public HashMap<String, Integer> availableHats = new HashMap<String, Integer>();
    public HashMap<String, Integer> serverHats = new HashMap<String, Integer>();
    public ArrayList<String> requestedHats = new ArrayList<String>();
    public ArrayList<Integer> requestMobHats = new ArrayList<Integer>();
    public ArrayList<Integer> requestedMobHats = new ArrayList<Integer>();

    public World worldInstance;

    public long clock;

    public long lastHitKey;
    public float rotationYaw;
    public float rotationPitch;

    public double posX;
    public double posY;
    public double posZ;

    public boolean isActive;

    public boolean hasScreen;
    public int currentHatRenders;
    public int requestCooldown;

    public GuiHatUnlocked guiHatUnlocked;
    public GuiTradeReq guiNewTradeReq;

    public String tradeReq;
    public int tradeReqTimeout;
}
