package hats.common.core;

import hats.common.Hats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class ConnectionHandler 
	implements IConnectionHandler, IPlayerTracker 
{

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) //client: remove server
	{
		Hats.proxy.tickHandlerClient.serverHasMod = false;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) //client: local server
	{
		Hats.proxy.tickHandlerClient.serverHasMod = false;
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
		}
	}


	//IPlayerTracker area
	
	@Override
	public void onPlayerLogin(EntityPlayer player) 
	{
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) 
	{
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
