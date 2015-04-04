package us.ichun.mods.hats.common.core;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import us.ichun.mods.hats.api.RenderOnEntityHelper;
import us.ichun.mods.hats.common.Hats;
import us.ichun.mods.hats.common.packet.PacketKingOfTheHatInfo;
import us.ichun.mods.hats.common.packet.PacketPing;
import us.ichun.mods.hats.common.trade.TradeInfo;
import us.ichun.mods.hats.common.trade.TradeRequest;

import java.util.*;
import java.util.Map.Entry;

public class TickHandlerServer
{
    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            //            for(int i = 0; i < 200; i++)
            //            {
            //                HatHandler.unlockHat(FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername("ohaiiChun"), HatHandler.getRandomHatFromList(HatHandler.getHatsWithWeightedContributors(), true).hatName);
            //            }
            Iterator<Entry<EntityLivingBase, String>> iterator1 = mobHats.entrySet().iterator();

            while(iterator1.hasNext())
            {
                Entry<EntityLivingBase, String> e = iterator1.next();
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

                    TreeMap<String, Integer> playerHatsList = Hats.proxy.tickHandlerServer.getPlayerHatsList(e.getKey());

                    ArrayList<String> newHats = HatHandler.getAllHatNamesAsList();

                    for(Map.Entry<String, Integer> e1 : playerHatsList.entrySet())
                    {
                        newHats.remove(e1.getKey());
                    }

                    EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(e.getKey());

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

            Iterator<Entry<String, TradeRequest>> ite = playerTradeRequests.entrySet().iterator();

            while(ite.hasNext())
            {
                Entry<String, TradeRequest> e = ite.next();
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
                    TreeMap<String, Integer> trader1Hats = Hats.proxy.tickHandlerServer.getPlayerHatsList(ti.trader1.getCommandSenderName());

                    TreeMap<String, Integer> trader2Hats = Hats.proxy.tickHandlerServer.getPlayerHatsList(ti.trader2.getCommandSenderName());

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

                    EventHandler.sendPlayerSessionInfo(ti.trader1);
                    EventHandler.sendPlayerSessionInfo(ti.trader2);

                    removeItems(ti.trader1, ti.trader1Items);
                    removeItems(ti.trader2, ti.trader2Items);

                    for(ItemStack is : ti.trader2Items)
                    {
                        if(!ti.trader1.inventory.addItemStackToInventory(is))
                        {
                            ti.trader1.dropPlayerItemWithRandomChoice(is, false);
                        }
                    }

                    for(ItemStack is : ti.trader1Items)
                    {
                        if(!ti.trader2.inventory.addItemStackToInventory(is))
                        {
                            ti.trader2.dropPlayerItemWithRandomChoice(is, false);
                        }
                    }

                    Hats.channel.sendToPlayer(new PacketPing(3, false), ti.trader1);
                    Hats.channel.sendToPlayer(new PacketPing(3, false), ti.trader2);

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
        Iterator<Entry<String, Integer>> ite = origin.entrySet().iterator();
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
        for(Entry<String, Integer> e : temp.entrySet())
        {
            origin.put(e.getKey(), e.getValue());
        }
        temp.clear();

        Iterator<Entry<String, Integer>> ite1 = hatsList.entrySet().iterator();
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
        for(Entry<String, Integer> e : temp.entrySet())
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

        Hats.proxy.playerWornHats.put(player.getCommandSenderName(), new HatInfo());

        Hats.channel.sendToPlayer(new PacketPing(1, false), player);

        Hats.proxy.sendPlayerListOfWornHats(player, false, false);
    }

    public void updateNewKing(String newKing, EntityPlayer player, boolean send)
    {
        if(!SessionState.currentKingServer.equalsIgnoreCase("") && !SessionState.currentKingServer.equalsIgnoreCase(newKing))
        {
            EntityPlayerMP oldKing = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerByUsername(SessionState.currentKingServer);
            if(oldKing != null)
            {
                playerDeath(oldKing);
            }

            TreeMap<String, Integer> playerHatsList = Hats.proxy.tickHandlerServer.getPlayerHatsList(SessionState.currentKingServer);

            Hats.proxy.tickHandlerServer.playerHats.put(SessionState.currentKingServer, null);

            Hats.proxy.tickHandlerServer.playerHats.put(newKing, playerHatsList);
        }
        SessionState.currentKingServer = newKing;
        if(send)
        {
            if(player != null)
            {
                if(player.getCommandSenderName().equalsIgnoreCase(SessionState.currentKingServer))
                {
                    StringBuilder sb = new StringBuilder();
                    TreeMap<String, Integer> hats = Hats.proxy.tickHandlerServer.playerHats.get(newKing);
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

                    Hats.channel.sendToPlayer(new PacketKingOfTheHatInfo(SessionState.currentKingServer, sb.toString().length() > 0 ? sb.toString().substring(0, sb.toString().length() - 1) : sb.toString()), player);
                }
                else
                {
                    Hats.channel.sendToPlayer(new PacketKingOfTheHatInfo(SessionState.currentKingServer, ""), player);
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
        TreeMap<String, Integer> playerHatsList = Hats.proxy.tickHandlerServer.playerHats.get(player);
        if(playerHatsList == null)
        {
            playerHatsList = new TreeMap<String, Integer>();
            Hats.proxy.tickHandlerServer.playerHats.put(player, playerHatsList);
        }
        return playerHatsList;
    }

    public WeakHashMap<EntityLivingBase, String> mobHats = new WeakHashMap<EntityLivingBase, String>();
    public HashMap<String, TreeMap<String, Integer>> playerHats = new HashMap<String, TreeMap<String, Integer>>();
    public HashMap<String, TimeActiveInfo> playerActivity = new HashMap<String, TimeActiveInfo>();

    public HashMap<String, TradeRequest> playerTradeRequests = new HashMap<String, TradeRequest>();

    public ArrayList<TradeInfo> activeTrades = new ArrayList<TradeInfo>();
}
