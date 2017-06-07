package me.ichun.mods.hats.common.core;

import me.ichun.mods.hats.api.RenderOnEntityHelper;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.packet.PacketKingOfTheHatInfo;
import me.ichun.mods.hats.common.packet.PacketPing;
import me.ichun.mods.hats.common.packet.PacketSession;
import me.ichun.mods.hats.common.trade.TradeInfo;
import me.ichun.mods.hats.common.trade.TradeRequest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;

public class EventHandlerServer
{
    public WeakHashMap<EntityLivingBase, String> mobHats = new WeakHashMap<EntityLivingBase, String>();
    public HashMap<String, TreeMap<String, Integer>> playerHats = new HashMap<String, TreeMap<String, Integer>>();
    public HashMap<String, TimeActiveInfo> playerActivity = new HashMap<String, TimeActiveInfo>();

    public HashMap<String, TradeRequest> playerTradeRequests = new HashMap<String, TradeRequest>();

    public ArrayList<TradeInfo> activeTrades = new ArrayList<TradeInfo>();

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            //            for(int i = 0; i < 200; i++)
            //            {
            //                HatHandler.unlockHat(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerForUsername("ohaiiChun"), HatHandler.getRandomHatFromList(HatHandler.getHatsWithWeightedContributors(), true).hatName);
            //            }
            Iterator<Map.Entry<EntityLivingBase, String>> iterator1 = mobHats.entrySet().iterator();

            while(iterator1.hasNext())
            {
                Map.Entry<EntityLivingBase, String> e = iterator1.next();
                if(e.getKey().isDead)
                {
                    iterator1.remove();
                }
            }

            for(Map.Entry<String, TimeActiveInfo> e : playerActivity.entrySet())
            {
                TimeActiveInfo info = e.getValue();
                info.tick();

                if(info.timeLeft == 0 && info.active)
                {
                    info.levels++;
                    info.timeLeft = Hats.config.startTime;

                    TreeMap<String, Integer> playerHatsList = Hats.eventHandlerServer.getPlayerHatsList(e.getKey());

                    ArrayList<String> newHats = HatHandler.getAllHatNamesAsList();

                    for(Map.Entry<String, Integer> e1 : playerHatsList.entrySet())
                    {
                        newHats.remove(e1.getKey());
                    }

                    EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(e.getKey());

                    if(player != null && !newHats.isEmpty())
                    {
                        HatHandler.unlockHat(player, HatHandler.getRandomHatFromList(newHats, Hats.config.hatRarity == 1).hatName);
                    }

                    for(int i = 0; i < info.levels; i++)
                    {
                        info.timeLeft *= 1F + (float)Hats.config.timeIncrement / 10000F;
                    }
                }
            }

            Iterator<Map.Entry<String, TradeRequest>> ite = playerTradeRequests.entrySet().iterator();

            while(ite.hasNext())
            {
                Map.Entry<String, TradeRequest> e = ite.next();
                TradeRequest tr = e.getValue();
                tr.timePending++;
                if(tr.timePending >= 1200)
                {
                    ite.remove();
                }
            }

            for(int i = activeTrades.size() - 1; i >= 0; i--)
            {
                TradeInfo ti = activeTrades.get(i);
                ti.update();
                if(ti.trade1 && ti.trade2)
                {
                    TreeMap<String, Integer> trader1Hats = Hats.eventHandlerServer.getPlayerHatsList(ti.trader1.getName());

                    TreeMap<String, Integer> trader2Hats = Hats.eventHandlerServer.getPlayerHatsList(ti.trader2.getName());

                    transferHat(trader1Hats, trader2Hats, ti.trader1Hats);
                    transferHat(trader2Hats, trader1Hats, ti.trader2Hats);

                    StringBuilder sb = new StringBuilder();
                    for(Map.Entry<String, Integer> e1 : trader1Hats.entrySet())
                    {
                        String hatName = HatHandler.getNameForHat(e1.getKey());
                        sb.append(hatName);
                        if(e1.getValue() > 1)
                        {
                            sb.append(">" + e1.getValue());
                        }
                        sb.append(":");
                    }

                    NBTTagCompound persistentTag = ti.trader1.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                    persistentTag.setString("Hats_unlocked", sb.toString().length() > 0 ? sb.toString().substring(0, sb.toString().length() - 1) : sb.toString());
                    ti.trader1.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);

                    StringBuilder sb1 = new StringBuilder();
                    for(Map.Entry<String, Integer> e1 : trader2Hats.entrySet())
                    {
                        String hatName = HatHandler.getNameForHat(e1.getKey());
                        sb1.append(hatName);
                        if(e1.getValue() > 1)
                        {
                            sb1.append(">" + e1.getValue());
                        }
                        sb1.append(":");
                    }

                    persistentTag = ti.trader2.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                    persistentTag.setString("Hats_unlocked", sb1.toString().length() > 0 ? sb1.toString().substring(0, sb1.toString().length() - 1) : sb1.toString());
                    ti.trader2.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);

                    sendPlayerSessionInfo(ti.trader1);
                    sendPlayerSessionInfo(ti.trader2);

                    removeItems(ti.trader1, ti.trader1Items);
                    removeItems(ti.trader2, ti.trader2Items);

                    for(ItemStack is : ti.trader2Items)
                    {
                        if(!ti.trader1.inventory.addItemStackToInventory(is))
                        {
                            ti.trader1.dropItem(is, false);
                        }
                    }

                    for(ItemStack is : ti.trader1Items)
                    {
                        if(!ti.trader2.inventory.addItemStackToInventory(is))
                        {
                            ti.trader2.dropItem(is, false);
                        }
                    }

                    Hats.channel.sendTo(new PacketPing(3, false), ti.trader1);
                    Hats.channel.sendTo(new PacketPing(3, false), ti.trader2);

                    ti.terminate = true;

                    activeTrades.remove(i);
                }
                else if(ti.terminate)
                {
                    activeTrades.remove(i);
                }
            }
        }
    }

    //    @SubscribeEvent
    //    public void onWorldTick(TickEvent.WorldTickEvent event)
    //    {
    //        if(event.phase == TickEvent.Phase.END)
    //        {
    //            boolean start = false;
    //            if(!start)
    //            {
    //                return;
    //            }
    //            for(int i = 0 ; i < event.world.loadedEntityList.size(); i++)
    //            {
    //                Entity ent = (Entity)event.world.loadedEntityList.get(i);
    //                if(ent instanceof EntityPlayer)
    //                {
    //                    continue;
    //                }
    //                if(event.world.getBlock((int)Math.floor(ent.posX), (int)Math.floor(ent.boundingBox.minY) - 1, (int)Math.floor(ent.posZ)) != Blocks.air)
    //                {
    //                    event.world.setBlock((int)Math.floor(ent.posX), (int)Math.floor(ent.boundingBox.minY) - 1, (int)Math.floor(ent.posZ), Blocks.wool, ent.getEntityId() % 16, 3);
    //                }
    //            }
    //        }
    //    }

    public void transferHat(TreeMap<String, Integer> origin, TreeMap<String, Integer> destination, TreeMap<String, Integer> hatsList)
    {
        HashMap<String, Integer> temp = new HashMap<String, Integer>();
        Iterator<Map.Entry<String, Integer>> ite = origin.entrySet().iterator();
        while(ite.hasNext())
        {
            Map.Entry<String, Integer> e = ite.next();
            for(Map.Entry<String, Integer> e1 : hatsList.entrySet())
            {
                if(e.getKey().equals(e1.getKey()))
                {
                    if(e.getValue() - e1.getValue() <= 0)
                    {
                        ite.remove();
                        break;
                    }
                    else
                    {
                        temp.put(e.getKey(), e.getValue() - e1.getValue());
                    }
                }
            }
        }
        for(Map.Entry<String, Integer> e : temp.entrySet())
        {
            origin.put(e.getKey(), e.getValue());
        }
        temp.clear();

        Iterator<Map.Entry<String, Integer>> ite1 = hatsList.entrySet().iterator();
        while(ite1.hasNext())
        {
            Map.Entry<String, Integer> e = ite1.next();
            for(Map.Entry<String, Integer> e1 : destination.entrySet())
            {
                if(e.getKey().equals(e1.getKey()))
                {
                    temp.put(e1.getKey(), e.getValue() + e1.getValue());
                    ite1.remove();
                    break;
                }
            }
        }
        for(Map.Entry<String, Integer> e : temp.entrySet())
        {
            destination.put(e.getKey(), e.getValue());
        }
        temp.clear();

        for(Map.Entry<String, Integer> e : hatsList.entrySet())
        {
            destination.put(e.getKey(), e.getValue());
        }
    }

    public void removeItems(EntityPlayer origin, ArrayList<ItemStack> itemsList)
    {
        ArrayList<ItemStack> itemsListCopy = new ArrayList<ItemStack>();
        for(ItemStack is : itemsList)
        {
            itemsListCopy.add(is.copy());
        }

        for(int i = origin.inventory.mainInventory.length - 1; i >= 0; i--)
        {
            ItemStack is = origin.inventory.mainInventory[i];
            if(is != null)
            {
                for(int j = itemsListCopy.size() - 1; j >= 0; j--)
                {
                    ItemStack is1 = itemsListCopy.get(j);
                    if(is1.isItemEqual(is) && ItemStack.areItemStackTagsEqual(is, is1))
                    {
                        while(is.stackSize > 0 && is1.stackSize > 0)
                        {
                            is.stackSize--;
                            is1.stackSize--;
                        }
                        if(is1.stackSize <= 0)
                        {
                            itemsListCopy.remove(j);
                        }
                    }
                }
                if(is.stackSize <= 0)
                {
                    origin.inventory.mainInventory[i] = null;
                }
                origin.inventory.markDirty();
            }
        }

        for(ItemStack is : itemsListCopy)
        {
            for(int i = itemsList.size() - 1; i >= 0; i--)
            {
                ItemStack is1 = itemsList.get(i);
                if(is1.isItemEqual(is) && ItemStack.areItemStackTagsEqual(is, is1))
                {
                    while(is.stackSize > 0 && is1.stackSize > 0)
                    {
                        is.stackSize--;
                        is1.stackSize--;
                    }
                    if(is1.stackSize <= 0)
                    {
                        itemsList.remove(i);
                    }
                }
            }
        }
    }

    public void playerKilledEntity(EntityLivingBase living, EntityPlayer player)
    {
        String hat = mobHats.get(living);

        RenderOnEntityHelper helper = HatHandler.getRenderHelper(living.getClass());

        if((helper == null || helper.canUnlockHat(living)) && hat != null)
        {
            HatHandler.unlockHat(player, hat);
        }
        mobHats.remove(living);
    }

    public void playerDeath(EntityPlayer player)
    {
        NBTTagCompound persistentTag = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        persistentTag.setString("Hats_unlocked", "");
        player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);

        Hats.proxy.playerWornHats.put(player.getName(), new HatInfo());

        Hats.channel.sendTo(new PacketPing(1, false), player);

        Hats.proxy.sendPlayerListOfWornHats(player, false, false);
    }

    public void updateNewKing(String newKing, EntityPlayer player, boolean send)
    {
        if(!SessionState.currentKingServer.equalsIgnoreCase("") && !SessionState.currentKingServer.equalsIgnoreCase(newKing))
        {
            EntityPlayerMP oldKing = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(SessionState.currentKingServer);
            if(oldKing != null)
            {
                playerDeath(oldKing);
            }

            TreeMap<String, Integer> playerHatsList = Hats.eventHandlerServer.getPlayerHatsList(SessionState.currentKingServer);

            Hats.eventHandlerServer.playerHats.put(SessionState.currentKingServer, null);

            Hats.eventHandlerServer.playerHats.put(newKing, playerHatsList);
        }
        SessionState.currentKingServer = newKing;
        if(send)
        {
            if(player != null)
            {
                if(player.getName().equalsIgnoreCase(SessionState.currentKingServer))
                {
                    StringBuilder sb = new StringBuilder();
                    TreeMap<String, Integer> hats = Hats.eventHandlerServer.playerHats.get(newKing);
                    if(hats != null)
                    {
                        for(Map.Entry<String, Integer> e : hats.entrySet())
                        {
                            sb.append(e.getKey());
                            if(e.getValue() != 1)
                            {
                                sb.append(">" + e.getValue());
                            }
                            sb.append(":");
                        }
                    }

                    Hats.channel.sendTo(new PacketKingOfTheHatInfo(SessionState.currentKingServer, sb.toString().length() > 0 ? sb.toString().substring(0, sb.toString().length() - 1) : sb.toString()), player);
                }
                else
                {
                    Hats.channel.sendTo(new PacketKingOfTheHatInfo(SessionState.currentKingServer, ""), player);
                }
            }
            else
            {
                Hats.channel.sendToAll(new PacketKingOfTheHatInfo(SessionState.currentKingServer, ""));
            }
        }
    }

    public void initializeTrade(EntityPlayerMP player, EntityPlayerMP plyr)
    {
        if(player == null || plyr == null)
        {
            return;
        }
        activeTrades.add((new TradeInfo(player, plyr)).initialize());
    }

    public TreeMap<String, Integer> getPlayerHatsList(String player)
    {
        TreeMap<String, Integer> playerHatsList = Hats.eventHandlerServer.playerHats.get(player);
        if(playerHatsList == null)
        {
            playerHatsList = new TreeMap<String, Integer>();
            Hats.eventHandlerServer.playerHats.put(player, playerHatsList);
        }
        return playerHatsList;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawn(EntityJoinWorldEvent event)
    {
        if(FMLCommonHandler.instance().getEffectiveSide().isClient() || !(event.getEntity() instanceof EntityLivingBase) || !HatHandler.canMobHat((EntityLivingBase)event.getEntity()) || Hats.eventHandlerServer.mobHats.containsKey(event.getEntity()))
        {
            return;
        }

        EntityLivingBase living = (EntityLivingBase)event.getEntity();

        boolean fromSpawner = false;
        for(int k = 0; k < event.getEntity().worldObj.loadedTileEntityList.size(); k++)
        {
            TileEntity te = (TileEntity)event.getEntity().worldObj.loadedTileEntityList.get(k);
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
                        List list = logic.getSpawnerWorld().getEntitiesWithinAABB(entity.getClass(), new AxisAlignedBB((double)logic.getSpawnerPosition().getX(), (double)logic.getSpawnerPosition().getY(), (double)logic.getSpawnerPosition().getZ(), (double)(logic.getSpawnerPosition().getX() + 1), (double)(logic.getSpawnerPosition().getY() + 1), (double)(logic.getSpawnerPosition().getZ() + 1)).expand((double)(4 * 2), 4.0D, (double)(4 * 2)));
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
            Hats.eventHandlerServer.mobHats.put(living, hatInfo.hatName);
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
                    if(!(event.getEntityLiving() instanceof EntityPlayer) && event.getSource().getEntity() instanceof EntityPlayer && !((EntityPlayer)event.getSource().getEntity()).capabilities.isCreativeMode)
                    {
                        Hats.eventHandlerServer.playerKilledEntity(event.getEntityLiving(), (EntityPlayer)event.getSource().getEntity());
                    }
                }

                if(event.getEntityLiving() instanceof EntityPlayer)
                {
                    EntityPlayer player = (EntityPlayer)event.getEntityLiving();
                    EntityPlayer executer = null;
                    if(event.getSource().getEntity() instanceof EntityPlayer)
                    {
                        executer = (EntityPlayer)event.getSource().getEntity();
                    }
                    if(Hats.config.playerHatsMode == 5)
                    {
                        //King died
                        if(SessionState.currentKingServer.equalsIgnoreCase(player.getName()))
                        {
                            if(executer != null)
                            {
                                Hats.eventHandlerServer.updateNewKing(executer.getName(), null, true);
                                Hats.eventHandlerServer.updateNewKing(executer.getName(), executer, true);
                                FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendChatMsg(new TextComponentTranslation("hats.kingOfTheHat.update.playerSlayed", new Object[] { player.getName(), executer.getName() }));
                            }
                            else
                            {
                                List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerList();
                                List<EntityPlayerMP> list = new ArrayList(players);
                                list.remove(player);
                                if(!list.isEmpty())
                                {
                                    EntityPlayer newKing = list.get(player.worldObj.rand.nextInt(list.size()));
                                    Hats.eventHandlerServer.updateNewKing(newKing.getName(), null, true);
                                    Hats.eventHandlerServer.updateNewKing(newKing.getName(), newKing, true);
                                    FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendChatMsg(new TextComponentTranslation("hats.kingOfTheHat.update.playerDied", new Object[] { player.getName(), newKing.getName() }));
                                }
                            }
                        }
                        else if(executer != null && SessionState.currentKingServer.equalsIgnoreCase(executer.getName()))
                        {
                            TreeMap<String, Integer> playerHatsList = Hats.eventHandlerServer.getPlayerHatsList(executer.getName());

                            ArrayList<String> newHats = HatHandler.getAllHatNamesAsList();

                            for(Map.Entry<String, Integer> e : playerHatsList.entrySet())
                            {
                                newHats.remove(e.getKey());
                            }

                            EntityPlayerMP newKingEnt = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(executer.getName());

                            if(newKingEnt != null && !newHats.isEmpty())
                            {
                                HatHandler.unlockHat(newKingEnt, newHats.get(newKingEnt.worldObj.rand.nextInt(newHats.size())));
                            }
                        }
                    }

                    if(Hats.config.resetPlayerHatsOnDeath == 1)
                    {
                        Hats.eventHandlerServer.playerDeath((EntityPlayer)event.getEntityLiving());
                    }
                }
            }
            Hats.eventHandlerServer.mobHats.remove(event.getEntityLiving());
        }
    }


    public static void sendPlayerSessionInfo(EntityPlayer player)
    {
        TreeMap<String, Integer> playerHatsList = Hats.eventHandlerServer.getPlayerHatsList(player.getName());

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

        Hats.channel.sendTo(new PacketSession(player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getBoolean("Hats_hasVisited") && player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getInteger("Hats_hatMode") == Hats.config.playerHatsMode || Hats.config.firstJoinMessage != 1, SessionState.currentKingServer, sb.toString().length() > 0 ? sb.toString().substring(0, sb.toString().length() - 1) : sb.toString()), player);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(Hats.config.playerHatsMode == 5 && SessionState.currentKingServer.equalsIgnoreCase(""))
        {
            //There is No king around now, so technically no players online
            Hats.eventHandlerServer.updateNewKing(event.player.getName(), null, false);
            FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendChatMsg(new TextComponentTranslation("hats.kingOfTheHat.update.playerJoin", event.player.getName()));
        }

        String playerHats = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getString("Hats_unlocked");

        if(Hats.config.playerHatsMode == 5)
        {
            if(!SessionState.currentKingServer.equalsIgnoreCase(event.player.getName()))
            {
                playerHats = "";
                NBTTagCompound persistentTag = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                persistentTag.setString("Hats_unlocked", playerHats);
                persistentTag.setString("Hats_wornHat", "");
                event.player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);
            }
        }

        TreeMap<String, Integer> playerHatsList = Hats.eventHandlerServer.getPlayerHatsList(event.player.getName());

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

        Hats.proxy.playerWornHats.put(event.player.getName(), new HatInfo(hatName, r, g, b, a));

        if(Hats.config.playerHatsMode == 6)
        {
            TimeActiveInfo info = Hats.eventHandlerServer.playerActivity.get(event.player.getName());

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

                Hats.eventHandlerServer.playerActivity.put(event.player.getName(), info);
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
        if(Hats.config.playerHatsMode == 5 && SessionState.currentKingServer.equalsIgnoreCase(event.player.getName()))
        {
            //King logged out
            List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerList();
            List<EntityPlayerMP> list = new ArrayList(players);
            list.remove(event.player);
            if(!list.isEmpty())
            {
                EntityPlayer newKing = list.get(event.player.worldObj.rand.nextInt(list.size()));
                Hats.eventHandlerServer.updateNewKing(newKing.getName(), null, true);
                Hats.eventHandlerServer.updateNewKing(newKing.getName(), newKing, true);
                FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendChatMsg(new TextComponentTranslation("hats.kingOfTheHat.update.playerLeft", new Object[] { event.player.getName(), newKing.getName() }));
            }
        }

        TimeActiveInfo info = Hats.eventHandlerServer.playerActivity.get(event.player.getName());

        if(info != null)
        {
            NBTTagCompound persistentTag = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            persistentTag.setInteger("Hats_activityLevels", info.levels);
            persistentTag.setInteger("Hats_activityTimeleft", info.timeLeft);
            event.player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentTag);

            info.active = false;
        }

        Hats.proxy.playerWornHats.remove(event.player.getName());
    }
}
