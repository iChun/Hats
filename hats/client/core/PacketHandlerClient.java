package hats.client.core;

import hats.common.Hats;
import hats.common.entity.EntityHat;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandlerClient
	implements IPacketHandler 
{
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		Minecraft mc = Minecraft.getMinecraft();
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
		try
		{
			int id = stream.readByte();
			switch(id)
			{
				case 0:
				{
					Hats.proxy.tickHandlerClient.serverHasMod = true;
					Hats.proxy.tickHandlerClient.serverHatMode = stream.readByte();
					
					String availHats = stream.readUTF(); //ignored on Free Mode
					break;
				}
				case 1:
				{
					String name = stream.readUTF();
					while(!name.equalsIgnoreCase("#endPacket"))
					{
						String hatName = stream.readUTF();
						
						Hats.proxy.tickHandlerClient.playerWornHatsName.put(name, hatName);
						
						EntityHat hat = Hats.proxy.tickHandlerClient.hats.get(name);
						if(hat != null)
						{
							hat.hatName = hatName;
						}
						
						name = stream.readUTF();
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
