package us.ichun.mods.hats.common.core;

import us.ichun.mods.hats.common.Hats;
import us.ichun.mods.hats.common.packet.PacketPing;
import us.ichun.mods.hats.common.packet.PacketSession;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.ichun.mods.hats.common.packet.PacketPing;
import us.ichun.mods.ichunutil.client.keybind.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EventHandler
{
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyEvent(KeyEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.keyBind.isPressed() && event.keyBind == Hats.config.guiKeyBind)
        {
            if(mc.currentScreen == null && !Hats.proxy.tickHandlerClient.hasScreen)
            {
                if(Hats.config.playerHatsMode == 3)
                {
                    Hats.channel.sendToServer(new PacketPing(0, false));
                }
                else if(Hats.config.playerHatsMode == 2)
                {
                    mc.thePlayer.addChatMessage(new ChatComponentTranslation("hats.lockedMode"));
                }
                else if(Hats.config.playerHatsMode == 5 && !SessionState.currentKing.equalsIgnoreCase(mc.thePlayer.getCommandSenderName()))
                {
                    mc.thePlayer.addChatMessage(new ChatComponentTranslation("hats.kingOfTheHat.notKing", SessionState.currentKing));
                }
                else
                {
                    Hats.proxy.openHatsGui();
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawn(EntityJoinWorldEvent event)
    {
        if(FMLCommonHandler.instance().getEffectiveSide().isClient() || !(event.entity instanceof EntityLivingBase) || !HatHandler.canMobHat((EntityLivingBase)event.entity) || Hats.proxy.tickHandlerServer.mobHats.containsKey(event.entity))
        {
            return;
        }

        EntityLivingBase living = (EntityLivingBase)event.entity;

        boolean fromSpawner = false;
        for(int k = 0; k < event.entity.worldObj.loadedTileEntityList.size(); k++)
        {
            TileEntity te = (TileEntity)event.entity.worldObj.loadedTileEntityList.get(k);
            if(!HatHandler.isMobSpawner(te.getClass(), te.getClass()))
            {
                continue;
            }

            MobSpawnerBaseLogic logic = HatHandler.getMobSpawnerLogic(te.getClass(), te);
            if(logic.isActivated())
            {
                Entity entity = EntityList.createEntityByName(logic.getEntityNameToSpawn(), logic.getSpawnerWorld());
                if(entity != null)
                {
                    if(living.getClass() == entity.getClass())
                    {
                        List list = logic.getSpawnerWorld().getEntitiesWithinAABB(entity.getClass(), AxisAlignedBB.fromBounds((double)logic.func_177221_b().getX(), (double)logic.func_177221_b().getY(), (double)logic.func_177221_b().getZ(), (double)(logic.func_177221_b().getX() + 1), (double)(logic.func_177221_b().getY() + 1), (double)(logic.func_177221_b().getZ() + 1)).expand((double)(4 * 2), 4.0D, (double)(4 * 2)));
                        if(list.contains(living))
                        {
                            fromSpawner = true;
                            break;
                        }
                    }
                }
            }
        }
        HatInfo hatInfo;
        if(living.getEntityData().hasKey("Hats_hatInfo"))
        {
            hatInfo = new HatInfo(living.getEntityData().getString("Hats_hatInfo"));
        }
        else
        {
            hatInfo  = living.getRNG().nextFloat() < ((float)Hats.config.randomMobHat / 100F) && !fromSpawner ? HatHandler.getRandomHatFromList(HatHandler.getHatsWithWeightedContributors(), Hats.config.playerHatsMode == 4 && Hats.config.hatRarity == 1) : new HatInfo();
            living.getEntityData().setString("Hats_hatInfo", hatInfo.hatName);
        }
        if(!hatInfo.hatName.isEmpty())
        {
            Hats.proxy.tickHandlerServer.mobHats.put(living, hatInfo.hatName);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            if(Hats.config.playerHatsMode >= 4)
            {
                if(Hats.config.playerHatsMode == 4)
                {
                    if(!(event.entityLiving instanceof EntityPlayer) && event.source.getEntity() instanceof EntityPlayer && !((EntityPlayer)event.source.getEntity()).capabilities.isCreativeMode)
                    {
                        Hats.proxy.tickHandlerServer.playerKilledEntity(event.entityLiving, (EntityPlayer)event.source.getEntity());
                    }
                }

                if(event.entityLiving instanceof EntityPlayer)
                {
                    EntityPlayer player = (EntityPlayer)event.entityLiving;
                    EntityPlayer executer = null;
                    if(event.source.getEntity() instanceof EntityPlayer)
                    {
                        executer = (EntityPlayer)event.source.getEntity();
                    }
                    if(Hats.config.playerHatsMode == 5)
                    {
                        //King died
                        if(SessionState.currentKingServer.equalsIgnoreCase(player.getCommandSenderName()))
                        {
                            if(executer != null)
                            {
                                Hats.proxy.tickHandlerServer.updateNewKing(executer.getCommandSenderName(), null, true);
                                Hats.proxy.tickHandlerServer.updateNewKing(executer.getCommandSenderName(), executer, true);
                                FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new ChatComponentTranslation("hats.kingOfTheHat.update.playerSlayed", new Object[] { player.getCommandSenderName(), executer.getCommandSenderName() }));
                            }
                            else
                            {
                                List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;
                                List<EntityPlayerMP> list = new ArrayList(players);
                                list.remove(player);
                                if(!list.isEmpty())
                                {
                                    EntityPlayer newKing = list.get(player.worldObj.rand.nextInt(list.size()));
                                    Hats.proxy.tickHandlerServer.updateNewKing(newKing.getCommandSenderName(), null, true);
                                    Hats.proxy.tickHandlerServer.updateNewKing(newKing.getCommandSenderName(), newKing, true);
                                    FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new ChatComponentTranslation("hats.kingOfTheHat.update.playerDied", new Object[] { player.getCommandSenderName(), newKing.getCommandSenderName() }));
                                }
                            }
                        }
                        else if(executer != null && SessionState.currentKingServer.equalsIgnoreCase(executer.getCommandSenderName()))
                        {
                            TreeMap<String, Integer> playerHatsList = Hats.proxy.tickHandlerServer.getPlayerHatsList(executer.getCommandSenderName());

                            ArrayList<String> newHats = HatHandler.getAllHatNamesAsList();

                            for(Map.Entry<String, Integer> e : playerHatsList.entrySet())
                            {
                                newHats.remove(e.getKey());
                            }

                            EntityPlayerMP newKingEnt = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(executer.getCommandSenderName());

                            if(newKingEnt != null && !newHats.isEmpty())
                            {
                                HatHandler.unlockHat(newKingEnt, newHats.get(newKingEnt.worldObj.rand.nextInt(newHats.size())));
                            }
                        }
                    }

                    if(Hats.config.resetPlayerHatsOnDeath == 1)
                    {
                        Hats.proxy.tickHandlerServer.playerDeath((EntityPlayer)event.entityLiving);
                    }
                }
            }
            Hats.proxy.tickHandlerServer.mobHats.remove(event.entityLiving);
        }
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        Hats.proxy.tickHandlerClient.isActive = true;

        SessionState.serverHasMod = 0;
        SessionState.currentKing = "";
        SessionState.showJoinMessage = 0;
        SessionState.hasVisited = 1;

        HatHandler.repopulateHatsList();
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        Hats.proxy.tickHandlerClient.hats.clear();
        Hats.proxy.tickHandlerClient.mobHats.clear();
        Hats.proxy.tickHandlerClient.playerWornHats.clear();
        Hats.proxy.tickHandlerClient.requestedHats.clear();
        if(Hats.proxy.tickHandlerClient.guiHatUnlocked != null)
        {
            Hats.proxy.tickHandlerClient.guiHatUnlocked.hatList.clear();
        }
        if(Hats.proxy.tickHandlerClient.guiNewTradeReq != null)
        {
            Hats.proxy.tickHandlerClient.guiNewTradeReq.hatList.clear();
        }
        Hats.proxy.tickHandlerClient.worldInstance = null;
    }

    public static void sendPlayerSessionInfo(EntityPlayer player)
    {
        TreeMap<String, Integer> playerHatsList = Hats.proxy.tickHandlerServer.getPlayerHatsList(player.getCommandSenderName());

        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, Integer> e : playerHatsList.entrySet())
        {
            sb.append(e.getKey());
            if(e.getValue() != 1)
            {
                sb.append(">" + e.getValue());
            }
            sb.append(":");
        }

        Hats.channel.sendToPlayer(new PacketSession(player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getBoolean("Hats_hasVisited") && player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getInteger("Hats_hatMode") == Hats.config.playerHatsMode || Hats.config.firstJoinMessage != 1, SessionState.currentKingServer, sb.toString().length() > 0 ? sb.toString().substring(0, sb.toString().length() - 1) : sb.toString()), player);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(Hats.config.playerHatsMode == 5 && SessionState.currentKingServer.equalsIgnoreCase(""))
        {
            //There is No king around now, so technically no players online
            Hats.proxy.tickHandlerServer.updateNewKing(event.player.getCommandSenderName(), null, false);
            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new ChatComponentTranslation("hats.kingOfTheHat.update.playerJoin", event.player.getCommandSenderName()));
        }

        String playerHats = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getString("Hats_unlocked");

        if(Hats.config.playerHatsMode == 5)
        {
            if(!SessionState.currentKingServer.equalsIgnoreCase(event.player.getCommandSenderName()))
            {
                playerHats = "";
                NBTTagCompound persistentTag = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                persistentTag.setString("Hats_unlocked", playerHats);
                persistentTag.setString("Hats_wornHat", "");
                event.player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);
            }
        }

        TreeMap<String, Integer> playerHatsList = Hats.proxy.tickHandlerServer.getPlayerHatsList(event.player.getCommandSenderName());

        playerHatsList.clear();
        String[] hatsWithCount = playerHats.split(":");
        for(String hat : hatsWithCount)
        {
            String[] hatAndCount = hat.split(">");
            if(!hatAndCount[0].trim().isEmpty())
            {
                try
                {
                    playerHatsList.put(hatAndCount[0], hatAndCount.length == 1 ? 1 : Integer.parseInt(hatAndCount[1]));
                }
                catch(NumberFormatException e)
                {
                    playerHatsList.put(hatAndCount[0], 1);
                }
            }
        }

        String hatName = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getString("Hats_wornHat");
        int r = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getInteger("Hats_colourR");
        int g = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getInteger("Hats_colourG");
        int b = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getInteger("Hats_colourB");
        int a = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getInteger("Hats_alpha");

        if(a == 0)
        {
            event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).setInteger("Hats_alpha", 255);
            a = 255;
        }

        if(!HatHandler.hasHat(hatName))
        {
            HatHandler.requestHat(hatName, event.player);
        }

        Hats.proxy.playerWornHats.put(event.player.getCommandSenderName(), new HatInfo(hatName, r, g, b, a));

        if(Hats.config.playerHatsMode == 6)
        {
            TimeActiveInfo info = Hats.proxy.tickHandlerServer.playerActivity.get(event.player.getCommandSenderName());

            if(info == null)
            {
                info = new TimeActiveInfo();
                info.timeLeft = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getInteger("Hats_activityTimeleft");
                info.levels = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getInteger("Hats_activityLevels");

                if(info.levels == 0 && info.timeLeft == 0)
                {
                    info.levels = 0;
                    info.timeLeft = Hats.config.startTime;
                }

                Hats.proxy.tickHandlerServer.playerActivity.put(event.player.getCommandSenderName(), info);
            }

            info.active = true;
        }

        sendPlayerSessionInfo(event.player);

        NBTTagCompound persistentTag = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        persistentTag.setBoolean("Hats_hasVisited", true);
        persistentTag.setInteger("Hats_hatMode", Hats.config.playerHatsMode);
        event.player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);

        if(Hats.config.playerHatsMode != 2)
        {
            Hats.proxy.sendPlayerListOfWornHats(event.player, true);
            Hats.proxy.sendPlayerListOfWornHats(event.player, false);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if(Hats.config.playerHatsMode == 5 && SessionState.currentKingServer.equalsIgnoreCase(event.player.getCommandSenderName()))
        {
            //King logged out
            List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;
            List<EntityPlayerMP> list = new ArrayList(players);
            list.remove(event.player);
            if(!list.isEmpty())
            {
                EntityPlayer newKing = list.get(event.player.worldObj.rand.nextInt(list.size()));
                Hats.proxy.tickHandlerServer.updateNewKing(newKing.getCommandSenderName(), null, true);
                Hats.proxy.tickHandlerServer.updateNewKing(newKing.getCommandSenderName(), newKing, true);
                FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new ChatComponentTranslation("hats.kingOfTheHat.update.playerLeft", new Object[] { event.player.getCommandSenderName(), newKing.getCommandSenderName() }));
            }
        }

        TimeActiveInfo info = Hats.proxy.tickHandlerServer.playerActivity.get(event.player.getCommandSenderName());

        if(info != null)
        {
            NBTTagCompound persistentTag = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            persistentTag.setInteger("Hats_activityLevels", info.levels);
            persistentTag.setInteger("Hats_activityTimeleft", info.timeLeft);
            event.player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);

            info.active = false;
        }

        Hats.proxy.playerWornHats.remove(event.player.getCommandSenderName());
    }
}
