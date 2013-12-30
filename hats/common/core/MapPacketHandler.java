package hats.common.core;

import hats.client.gui.GuiHatSelection;
import hats.client.gui.GuiHatUnlocked;
import hats.client.gui.GuiTradeWindow;
import hats.common.Hats;
import hats.common.trade.TradeInfo;
import hats.common.trade.TradeRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MapPacketHandler
implements ITinyPacketHandler
{
	@Override
	public void handle(NetHandler handler, Packet131MapData mapData) 
	{
		int id = mapData.uniqueID;
		if(handler instanceof NetServerHandler)
		{
			handleServerPacket((NetServerHandler)handler, mapData.uniqueID, mapData.itemData, (EntityPlayerMP)handler.getPlayer());
		}
		else
		{
			handleClientPacket((NetClientHandler)handler, mapData.uniqueID, mapData.itemData);
		}
	}

	public void handleServerPacket(NetServerHandler handler, short id, byte[] data, EntityPlayerMP player)
	{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
		try
		{
			switch(id)
			{
				case 0:
				{
					String hatName = stream.readUTF();
					int r = stream.readInt();
					int g = stream.readInt();
					int b = stream.readInt();
	
					Hats.proxy.playerWornHats.put(player.username, new HatInfo(hatName, r, g, b));
	
					if(HatHandler.hasHat(hatName))
					{
						Hats.proxy.saveData(DimensionManager.getWorld(0));
	
						Hats.proxy.sendPlayerListOfWornHats(player, false);
					}
					else
					{
						HatHandler.requestHat(hatName, player);
					}
	
//					EntityGiantZombie zomb = new EntityGiantZombie(player.worldObj);
//					zomb.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
//					player.worldObj.spawnEntityInWorld(zomb);

					break;
				}
				case 1:
				{
					String hatName = stream.readUTF();
	
					HatHandler.sendHat(hatName, player);
	
					break;
				}
				case 2:
				{
					ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					DataOutputStream stream1 = new DataOutputStream(bytes);
	
					try
					{
						stream1.writeBoolean(FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().isPlayerOpped(player.username.toLowerCase().trim()));
	
						PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)0, bytes.toByteArray()), (Player)player);
					}
					catch(IOException e)
					{}
					break;
				}
				case 3:
				{
					ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					DataOutputStream stream1 = new DataOutputStream(bytes);
	
					try
					{
						stream1.writeByte(3);

						boolean cont = stream.readBoolean();
						int idd;
						while(cont)
						{
							idd = stream.readInt();
		
							Entity ent = player.worldObj.getEntityByID(idd);
							if(ent instanceof EntityLivingBase)
							{
								String hatName = Hats.proxy.tickHandlerServer.mobHats.get((EntityLivingBase)ent);
								if(hatName != null)
								{
									stream1.writeInt(new Integer(idd));
									stream1.writeUTF(hatName.trim());
									
									if(bytes.size() > 32000)
									{
										stream1.writeInt(-1);
			
										PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Hats", bytes.toByteArray()), (Player)player);
										
										bytes = new ByteArrayOutputStream();
										stream1 = new DataOutputStream(bytes);
			
										stream1.writeByte(3);
									}
								}
							}
							cont = stream.readBoolean();
						}
	
						PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Hats", bytes.toByteArray()), (Player)player);
					}
					catch(IOException e)
					{}
	
					break;
				}
				case 4:
				{
					TimeActiveInfo info = Hats.proxy.tickHandlerServer.playerActivity.get(player.username);
					
					if(info != null)
					{
						info.active = stream.readBoolean();
					}
					break;
				}
				case 5:
				{
					//Trade request;
					String plyr1 = stream.readUTF();
					
					EntityPlayerMP plyr = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(plyr1);
					
					if(plyr != null && plyr.isEntityAlive() && plyr.getDistanceToEntity(player) < 16D && plyr.canEntityBeSeen(player) && plyr.dimension == player.dimension)
					{
						TradeRequest tr1 = Hats.proxy.tickHandlerServer.playerTradeRequests.get(player.username);
						
						if(tr1 != null && tr1.traderName.equalsIgnoreCase(plyr.username))
						{
							Hats.proxy.tickHandlerServer.initializeTrade(player, plyr);
							break;
						}
						
						TradeRequest tr = Hats.proxy.tickHandlerServer.playerTradeRequests.get(plyr.username);
						if(tr == null || !tr.traderName.equalsIgnoreCase(player.username))
						{
							Hats.proxy.tickHandlerServer.playerTradeRequests.put(plyr.username, new TradeRequest(player.username));
							
					        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					        DataOutputStream stream1 = new DataOutputStream(bytes);

					        try
					        {
					        	stream1.writeUTF(player.username);
					        	
					        	PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)5, bytes.toByteArray()), (Player)plyr);
					        }
					        catch(IOException e)
					        {}
						}
					}
					else
					{
						player.sendChatToPlayer(ChatMessageComponent.createFromTranslationWithSubstitutions("hats.trade.cannotFindTrader", new Object[] { plyr1 }));
					}
					
					break;
				}
				case 6:
				{
					//Accept request
					String plyr1 = stream.readUTF();
					
					TradeRequest tr = Hats.proxy.tickHandlerServer.playerTradeRequests.get(player.username);
					if(tr == null)
					{
						player.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("hats.trade.cannotAcceptTrade"));
						break;
					}
					
					EntityPlayerMP plyr = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(plyr1);
					
					if(plyr != null && plyr.isEntityAlive() && plyr.getDistanceToEntity(player) < 16D && plyr.canEntityBeSeen(player) && plyr.dimension == player.dimension)
					{
						Hats.proxy.tickHandlerServer.playerTradeRequests.remove(player.username);
						Hats.proxy.tickHandlerServer.initializeTrade(player, plyr);
					}
					else
					{
						player.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("hats.trade.cannotAcceptTrade"));
					}
					
					break;
				}
				case 7:
				{
					//received chat.
					for(TradeInfo ti : Hats.proxy.tickHandlerServer.activeTrades)
					{
						if(ti.isPlayerInTrade(player))
						{
							ti.sendTradeMessage(stream.readUTF(), ti.getOtherPlayer(player));
							break;
						}
					}
					break;
				}
				case 8:
				{
					//gui closed by player
					for(TradeInfo ti : Hats.proxy.tickHandlerServer.activeTrades)
					{
						if(ti.isPlayerInTrade(player))
						{
							ti.terminate(3, player);
							break;
						}
					}
					break;
				}
				case 9:
				{
					//received trade info from client
					for(TradeInfo ti : Hats.proxy.tickHandlerServer.activeTrades)
					{
						if(ti.isPlayerInTrade(player))
						{
							ti.receiveTradeInfo(stream, data, player);
							break;
						}
					}
					break;
				}
				case 10:
				{
					//toggleReady;
					boolean ready = stream.readBoolean();
					
					for(TradeInfo ti : Hats.proxy.tickHandlerServer.activeTrades)
					{
						if(ti.isPlayerInTrade(player))
						{
							if(ti.trader1 == player)
							{
								ti.toggleReadyTrader1(ready);
							}
							else
							{
								ti.toggleReadyTrader2(ready);
							}
							break;
						}
					}

					break;
				}
				case 11:
				{
					//mark point of no return;
					for(TradeInfo ti : Hats.proxy.tickHandlerServer.activeTrades)
					{
						if(ti.isPlayerInTrade(player))
						{
							if(ti.trader1 == player)
							{
								ti.trade1 = true;
							}
							else
							{
								ti.trade2 = true;
							}
							
					        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					        DataOutputStream stream1 = new DataOutputStream(bytes);

					        try
					        {
					        	stream1.writeByte(0);
					       		PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short)Hats.getNetId(), (short)10, bytes.toByteArray()), (Player)ti.getOtherPlayer(player));
					        }
					        catch(IOException e)
					        {}

							break;
						}
					}
					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}

	//TODO Side Split

	@SideOnly(Side.CLIENT)
	public void handleClientPacket(NetClientHandler handler, short id, byte[] data)
	{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
		try
		{
			switch(id)
			{
				case 0:
				{
					//Can show gui?
					if(stream.readBoolean())
					{
						FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiHatSelection(Minecraft.getMinecraft().thePlayer));
					}
					else
					{
						Minecraft.getMinecraft().thePlayer.addChatMessage(StatCollector.translateToLocal("hats.serverOnCommandGiverMode"));
					}
					break;
				}
				case 1:
				{
					//Received hat request
					String hatName = stream.readUTF();
	
					HatHandler.sendHat(hatName, null);
	
					break;
				}
				case 2:
				{
					//Unlocked hat
					String name = stream.readUTF();
					if(!Hats.proxy.tickHandlerClient.serverHats.contains(name))
					{
						Hats.proxy.tickHandlerClient.serverHats.add(name);
						Collections.sort(Hats.proxy.tickHandlerClient.serverHats);
						if(Hats.proxy.tickHandlerClient.guiHatUnlocked == null)
						{
							Hats.proxy.tickHandlerClient.guiHatUnlocked = new GuiHatUnlocked(Minecraft.getMinecraft());
						}
						Hats.proxy.tickHandlerClient.guiHatUnlocked.queueHatUnlocked(name);
					}
	
					break;
				}
				case 3:
				{
					HatHandler.populateHatsList("");
					break;
				}
				case 4:
				{
					stream.readByte();
					SessionState.currentKing = stream.readUTF();
					break;
				}
				case 5:
				{
					//received Trade request
					Hats.proxy.tickHandlerClient.tradeReq = stream.readUTF();
					Hats.proxy.tickHandlerClient.tradeReqTimeout = 1200;
					
					Minecraft.getMinecraft().sndManager.playSound("random.successful_hit", (float)Minecraft.getMinecraft().thePlayer.posX, (float)Minecraft.getMinecraft().thePlayer.posY, (float)Minecraft.getMinecraft().thePlayer.posZ, 1.0F, 1.0F);
					
					Hats.proxy.tickHandlerClient.guiNewTradeReq.queueHatUnlocked(Hats.proxy.tickHandlerClient.tradeReq);
					
					if(Minecraft.getMinecraft().currentScreen instanceof GuiHatSelection)
					{
						((GuiHatSelection)Minecraft.getMinecraft().currentScreen).updateButtonList();
					}
					break;
				}
				case 6:
				{
					//received chat message from server;
					if(Minecraft.getMinecraft().currentScreen instanceof GuiTradeWindow)
					{
						GuiTradeWindow trade = (GuiTradeWindow)Minecraft.getMinecraft().currentScreen;
						trade.chatMessages.add(stream.readUTF());
					}
					break;
				}
				case 7:
				{
					//begin trade session
					FMLClientHandler.instance().displayGuiScreen(Minecraft.getMinecraft().thePlayer, new GuiTradeWindow(stream.readUTF()));
					break;
				}
				case 8:
				{
					//receive other player's trade info
					if(Minecraft.getMinecraft().currentScreen instanceof GuiTradeWindow)
					{
						GuiTradeWindow trade = (GuiTradeWindow)Minecraft.getMinecraft().currentScreen;
						
			        	ArrayList<String> hats = new ArrayList<String>();
			        	ArrayList<ItemStack> items = new ArrayList<ItemStack>();
			        	
			        	int hatCount = stream.readInt();
			        	
			        	for(int i = 0; i < hatCount; i++)
			        	{
			        		hats.add(stream.readUTF());
			        	}
			        	
			        	int itemCount = stream.readInt();
			        	
			        	for(int i = 0; i < itemCount; i++)
			        	{
			        		ItemStack is = ItemStack.loadItemStackFromNBT(Hats.readNBTTagCompound(stream));
			        		if(is != null)
			        		{
			        			items.add(is);
			        		}
			        	}
			        	
			        	ArrayList<String> oldHats = new ArrayList<String>(trade.theirHatsForTrade);
			        	ArrayList<ItemStack> oldItems = new ArrayList<ItemStack>(trade.theirItemsForTrade);

			        	trade.theirHatsForTrade = hats;
			        	trade.theirItemsForTrade = items;
			        	
			        	int tradeSize = oldItems.size();
			        	int hatsSize = oldHats.size();
			        	
			    		trade.theirCanScroll = trade.theirHatsForTrade.size() > 3 || trade.theirItemsForTrade.size() > 6;
			    		if(!trade.theirCanScroll)
			    		{
			    			trade.theirScrollProg = 0.0F;
			    		}
			    		else if(tradeSize != trade.theirItemsForTrade.size() && (trade.theirItemsForTrade.size() % 6 == 1 && tradeSize % 6 == 0 || trade.theirItemsForTrade.size() % 6 == 0 && tradeSize % 6 == 1))
			    		{
			    			float currentBoxes = (float)Math.ceil((float)Math.max(trade.theirHatsForTrade.size(), 3) / 3F) * 2 + (float)Math.ceil((float)Math.max(tradeSize, 6) / 6F) - 3;
			    			if(currentBoxes > 0)
			    			{
			    				trade.theirScrollProg = MathHelper.clamp_float(trade.theirScrollProg * (trade.theirItemsForTrade.size() > tradeSize ? ((currentBoxes) / (currentBoxes + 1)) : ((currentBoxes) / (currentBoxes - 1))), 0.0F, 1.0F);
			    			}
			    		}
			    		else if(hatsSize != trade.theirHatsForTrade.size() && (trade.theirHatsForTrade.size() % 3 == 1 && hatsSize % 3 == 0 || trade.theirHatsForTrade.size() % 3 == 0 && hatsSize % 3 == 1))
			    		{
			    			float currentBoxes = (float)Math.ceil((float)Math.max(hatsSize, 3) / 3F) * 2 + (float)Math.ceil((float)Math.max(trade.theirItemsForTrade.size(), 6) / 6F) - 3;
			    			if(currentBoxes > 0)
			    			{
			    				trade.theirScrollProg = MathHelper.clamp_float(trade.theirScrollProg * (trade.theirHatsForTrade.size() > hatsSize ? ((currentBoxes) / (currentBoxes + 2)) : ((currentBoxes) / (currentBoxes - 2))), 0.0F, 1.0F);
			    			}
			    		}

					}
					break;	
				}
				case 9:
				{
					//receive ready info
					if(Minecraft.getMinecraft().currentScreen instanceof GuiTradeWindow)
					{
						GuiTradeWindow trade = (GuiTradeWindow)Minecraft.getMinecraft().currentScreen;
						
						String name1 = stream.readUTF();
						boolean r1 = stream.readBoolean();
						String name2 = stream.readUTF();
						boolean r2 = stream.readBoolean();
						
						if(name1.equalsIgnoreCase(Minecraft.getMinecraft().thePlayer.username))
						{
							if(trade.selfReady && !r1)
							{
								Minecraft.getMinecraft().sndManager.playSoundFX("random.click", 1.0F, 1.0F);
							}
							trade.selfReady = r1;
							trade.theirReady = r2;
						}
						else
						{
							if(trade.selfReady && !r2)
							{
								Minecraft.getMinecraft().sndManager.playSoundFX("random.click", 1.0F, 1.0F);
							}
							trade.selfReady = r2;
							trade.theirReady = r1;
						}
						if(!trade.theirReady)
						{
							trade.pointOfNoReturn = false;
							trade.clickedMakeTrade = false;
						}
					}
					break;
				}
				case 10:
				{
					//trigger point of no return
					if(Minecraft.getMinecraft().currentScreen instanceof GuiTradeWindow)
					{
						GuiTradeWindow trade = (GuiTradeWindow)Minecraft.getMinecraft().currentScreen;
						trade.pointOfNoReturn = true;
					}
					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}

}
