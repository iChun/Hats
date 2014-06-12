package hats.client.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import hats.api.RenderOnEntityHelper;
import hats.client.gui.GuiHatSelection;
import hats.client.gui.GuiHatUnlocked;
import hats.client.gui.GuiTradeReq;
import hats.client.render.helper.HelperGeneric;
import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
import hats.common.entity.EntityHat;
import hats.common.packet.PacketPing;
import hats.common.packet.PacketRequestMobHats;
import ichun.common.core.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

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

        if(mc.theWorld != null)
        {
            WorldClient world = mc.theWorld;
            if(event.phase == TickEvent.Phase.START)
            {
                currentHatRenders = 0;
                Iterator<Entry<String, EntityHat>> iterator = hats.entrySet().iterator();

                while(iterator.hasNext())
                {
                    Entry<String, EntityHat> e = iterator.next();
                    if(e.getValue().parent != null)
                    {
                        EntityHat hat = e.getValue();

                        updateHatPosAndAngle(hat, hat.renderingParent);
                    }
                }

                Iterator<Entry<Integer, EntityHat>> iterator1 = mobHats.entrySet().iterator();

                while(iterator1.hasNext())
                {
                    Entry<Integer, EntityHat> e = iterator1.next();
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

            if(worldInstance != world)
            {
                worldInstance = world;
                mobHats.clear();
                hats.clear();
                requestMobHats.clear();
                requestedMobHats.clear();
                requestCooldown = 40;
            }
            if(Hats.config.getSessionInt("showJoinMessage") == 1)
            {
                Hats.config.updateSession("showJoinMessage", 0);
                mc.thePlayer.addChatMessage(new ChatComponentTranslation(Hats.config.getSessionInt("playerHatsMode") == 4 ? StatCollector.translateToLocal("hats.firstJoin.hatHunting") : Hats.config.getSessionInt("playerHatsMode") == 6 ? StatCollector.translateToLocal("hats.firstJoin.timeActive") : StatCollector.translateToLocal("hats.firstJoin.kingOfTheHat.hasKing")));
            }
            if(Hats.config.getInt("enableInServersWithoutMod") == 1 && Hats.config.getSessionInt("serverHasMod") == 0 || Hats.config.getSessionInt("serverHasMod") == 1)
            {
                for(int i = 0; i < world.playerEntities.size(); i++)
                {
                    EntityPlayer player = (EntityPlayer)world.playerEntities.get(i);
                    if(Hats.config.getSessionInt("serverHasMod") == 0 && Hats.config.getInt("shouldOtherPlayersHaveHats") == 0 && player != Minecraft.getMinecraft().thePlayer || !player.isEntityAlive())
                    {
                        continue;
                    }

                    EntityHat hat = hats.get(player.getCommandSenderName());
                    if(hat == null || hat.isDead)
                    {
                        if(player.getCommandSenderName().equalsIgnoreCase(mc.thePlayer.getCommandSenderName()))
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

                        HatInfo hatInfo = (Hats.config.getSessionInt("serverHasMod") == 1 ? getPlayerHat(player.getCommandSenderName()) : ((Hats.config.getInt("randomHat") == 1 || Hats.config.getInt("randomHat") == 2 && player != mc.thePlayer) ? HatHandler.getRandomHatFromList(HatHandler.getAllHats(), false) : Hats.favouriteHatInfo));
                        hat = new EntityHat(world, player, hatInfo);
                        hats.put(player.getCommandSenderName(), hat);
                        world.spawnEntityInWorld(hat);
                    }
                }
            }

            if(clock != world.getWorldTime() || !world.getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
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
                        PacketHandler.sendToServer(Hats.channels, new PacketRequestMobHats(requestedMobHats));
                        requestMobHats.clear();
                    }
                }

                if(Hats.config.getSessionInt("playerHatsMode") == 6)
                {
                    lastHitKey++;

                    if(Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()))
                    {
                        lastHitKey = 0;
                    }
                    if(clock % 107L == 0)
                    {
                        if((lastHitKey > 100 || lastHitKey == 0 && posX == mc.thePlayer.posX && posY == mc.thePlayer.posY && posZ == mc.thePlayer.posZ) && (rotationYaw == mc.thePlayer.rotationYaw && rotationPitch == mc.thePlayer.rotationPitch || posX == mc.thePlayer.posX && posY == mc.thePlayer.posY && posZ == mc.thePlayer.posZ || mc.thePlayer.ridingEntity != null) || hasScreen)
                        {
                            if(isActive)
                            {
                                isActive = false;

                                PacketHandler.sendToServer(Hats.channels, new PacketPing(1, false));
                            }
                        }
                        else
                        {
                            if(!isActive)
                            {
                                isActive = true;

                                PacketHandler.sendToServer(Hats.channels, new PacketPing(1, true));
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

            if(Hats.config.getInt("randomMobHat") > 0 && !(Hats.config.getSessionInt("serverHasMod") == 1 && Hats.config.getSessionInt("playerHatsMode") == 4) || Hats.config.getSessionInt("serverHasMod") == 1 && Hats.config.getSessionInt("playerHatsMode") == 4)
            {
                for(int i = 0; i < world.loadedEntityList.size(); i++)
                {
                    Entity ent = (Entity)world.loadedEntityList.get(i);
                    if(!(ent instanceof EntityLivingBase) || !(Hats.config.getSessionInt("serverHasMod") == 1 && Hats.config.getSessionInt("playerHatsMode") == 4) && !HatHandler.canMobHat((EntityLivingBase)ent) || ent instanceof EntityPlayer)
                    {
                        continue;
                    }

                    EntityLivingBase living = (EntityLivingBase)ent;

                    EntityHat hat = mobHats.get(living.getEntityId());
                    if(hat == null || hat.isDead)
                    {
                        if(Hats.config.getSessionInt("serverHasMod") == 0 || Hats.config.getSessionInt("playerHatsMode") != 4)
                        {
                            HatInfo hatInfo = living.getRNG().nextFloat() < ((float)Hats.config.getInt("randomMobHat") / 100F) ? (Hats.config.getInt("randomHat") >= 1 ? HatHandler.getRandomHatFromList(HatHandler.getAllHats(), false) : Hats.favouriteHatInfo) : new HatInfo();
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

            Iterator<Entry<String, EntityHat>> ite = hats.entrySet().iterator();

            while(ite.hasNext())
            {
                Entry<String, EntityHat> e = ite.next();
                if(e.getValue().worldObj.provider.dimensionId != world.provider.dimensionId || (world.getWorldTime() - e.getValue().lastUpdate) > 10L)
                {
                    e.getValue().setDead();
                    ite.remove();
                }
            }

            Iterator<Entry<Integer, EntityHat>> ite1 = mobHats.entrySet().iterator();

            while(ite1.hasNext())
            {
                Entry<Integer, EntityHat> e = ite1.next();
                if(e.getValue().worldObj.provider.dimensionId != world.provider.dimensionId || (world.getWorldTime() - e.getValue().lastUpdate) > 10L)
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
			if(Hats.config.getSessionInt("playerHatsMode") == 2)
			{
				return new HatInfo(Hats.config.getSessionString("lockedHat").toLowerCase());
			}
			return new HatInfo();
		}
		return name;
	}
	
	public HashMap<String, HatInfo> playerWornHats = new HashMap<String, HatInfo>();
	public HashMap<String, EntityHat> hats = new HashMap<String, EntityHat>();
	public HashMap<Integer, EntityHat> mobHats = new HashMap<Integer, EntityHat>();
	
	public HashMap<Integer, EntityHat> rendered = new HashMap<Integer, EntityHat>();
	
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
