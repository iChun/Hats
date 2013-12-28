package hats.common.core;

import hats.common.Hats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class ConnectionHandler 
	implements IConnectionHandler, IPlayerTracker 
{

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) //client: remove server
	{
		onClientConnected();
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) //client: local server
	{
		onClientConnected();
	}
	
	public void onClientConnected()
	{
		SessionState.serverHasMod = false;
		
		HatHandler.repopulateHatsList();
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) //server
	{
		return null;
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) //client
	{
	}

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) //server
	{
		
	}

	@Override
	public void connectionClosed(INetworkManager manager) //both 
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			Hats.proxy.tickHandlerClient.hats.clear();
			Hats.proxy.tickHandlerClient.mobHats.clear();
			Hats.proxy.tickHandlerClient.playerWornHats.clear();
			Hats.proxy.tickHandlerClient.requestedHats.clear();
			Hats.proxy.tickHandlerClient.guiHatUnlocked.hatList.clear();
			Hats.proxy.tickHandlerClient.worldInstance = null;
		}
	}


	//IPlayerTracker area
	
	@Override
	public void onPlayerLogin(EntityPlayer player) 
	{
		if(SessionState.serverHatMode == 5 && SessionState.currentKing.equalsIgnoreCase(""))
		{
			//There is No king around now, so technically no players online
			Hats.proxy.tickHandlerServer.updateNewKing(player.username, null, false);
			FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromTranslationWithSubstitutions("hats.kingOfTheHat.update.playerJoin", new Object[] { player.username }));
		}
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(bytes);

		try
		{
			stream.writeByte(0); //packetID;
			
			stream.writeByte((byte)SessionState.serverHatMode);
			
			stream.writeBoolean(Hats.proxy.saveData.getBoolean(player.username + "_hasVisited") && Hats.proxy.saveData.getInteger(player.username + "_hatMode") == SessionState.serverHatMode);
			
			stream.writeUTF(SessionState.serverHat);
			
			stream.writeUTF(SessionState.currentKing);
			
			if(Hats.proxy.saveData != null)
			{
				String playerHats = Hats.proxy.saveData.getString(player.username + "_unlocked");
				
				if(SessionState.serverHatMode == 5)
				{
					if(!SessionState.currentKing.equalsIgnoreCase(player.username))
					{
						playerHats = "";
					}
				}
				
				stream.writeUTF(SessionState.serverHatMode >= 4 ? playerHats : "");
				
				ArrayList<String> playerHatsList = Hats.proxy.tickHandlerServer.playerHats.get(player.username);
				if(playerHatsList == null)
				{
					playerHatsList = new ArrayList<String>();
					Hats.proxy.tickHandlerServer.playerHats.put(player.username, playerHatsList);
				}
				
				playerHatsList.clear();
				String[] hats = playerHats.split(":");
				for(String hat : hats)
				{
					if(!hat.trim().equalsIgnoreCase(""))
					{
						boolean has = false;
						for(String s : playerHatsList)
						{
							if(s.equalsIgnoreCase(hat))
							{
								has = true;
								break;
							}
						}
						if(!has)
						{
							playerHatsList.add(hat);
						}
					}
				}
				
				String hatName = Hats.proxy.saveData.getString(player.username + "_wornHat");
				int r = Hats.proxy.saveData.getInteger(player.username + "_colourR");
				int g = Hats.proxy.saveData.getInteger(player.username + "_colourG");
				int b = Hats.proxy.saveData.getInteger(player.username + "_colourB");
				
				if(!HatHandler.hasHat(hatName))
				{
					HatHandler.requestHat(hatName, player);
				}
				
				Hats.proxy.playerWornHats.put(player.username, new HatInfo(hatName, r, g, b));
				
				TimeActiveInfo info = Hats.proxy.tickHandlerServer.playerActivity.get(player.username);
				
				if(info == null)
				{
					info = new TimeActiveInfo();
					info.timeLeft = Hats.proxy.saveData.getInteger(player.username + "_activityTimeleft");
					info.levels = Hats.proxy.saveData.getInteger(player.username + "_activityLevels");
					
					if(info.levels == 0 && info.timeLeft == 0)
					{
						info.levels = 0;
						info.timeLeft = Hats.startTime;
					}
					
					Hats.proxy.tickHandlerServer.playerActivity.put(player.username, info);
				}
				
				info.active = true;
			}
			else
			{
				stream.writeUTF("");
				
				Hats.proxy.playerWornHats.put(player.username, new HatInfo());
			}
			
			PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Hats", bytes.toByteArray()), (Player)player);
			
			Hats.proxy.saveData.setBoolean(player.username + "_hasVisited", true);
			Hats.proxy.saveData.setInteger(player.username + "_hatMode", SessionState.serverHatMode);
		}
		catch(IOException e)
		{}

		if(SessionState.serverHatMode != 2)
		{
			Hats.proxy.sendPlayerListOfWornHats(player, true);
			Hats.proxy.sendPlayerListOfWornHats(player, false);
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) 
	{
		if(SessionState.serverHatMode == 5 && SessionState.currentKing.equalsIgnoreCase(player.username))
		{
			//King logged out
			List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;
			List<EntityPlayerMP> list = new ArrayList(players);
			list.remove(player);
			if(!list.isEmpty())
			{
				EntityPlayer newKing = list.get(player.worldObj.rand.nextInt(list.size()));
				Hats.proxy.tickHandlerServer.updateNewKing(newKing.username, null, true);
				Hats.proxy.tickHandlerServer.updateNewKing(newKing.username, newKing, true);
				FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromTranslationWithSubstitutions("hats.kingOfTheHat.update.playerLeft", new Object[] { player.username, newKing.username }));
			}
		}	
		
		TimeActiveInfo info = Hats.proxy.tickHandlerServer.playerActivity.get(player.username);

		if(info != null)
		{
			info.active = false;
		}
		
		Hats.proxy.playerWornHats.remove(player.username);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) 
	{
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) 
	{
	}

}
