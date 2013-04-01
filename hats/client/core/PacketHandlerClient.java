package hats.client.core;

import hats.common.Hats;
import hats.common.core.HatHandler;
import hats.common.core.HatInfo;
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
						int r = stream.readInt();
						int g = stream.readInt();
						int b = stream.readInt();
						
						Hats.proxy.tickHandlerClient.playerWornHats.put(name, new HatInfo(hatName, r, g, b));
						
						EntityHat hat = Hats.proxy.tickHandlerClient.hats.get(name);
						if(hat != null)
						{
							hat.hatName = hatName;
							hat.colourR = r;
							hat.colourG = g;
							hat.colourB = b;
						}
						
						name = stream.readUTF();
					}
					break;
				}
				case 2:
				{
					HatHandler.receiveHatData(stream, null, false);
					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}		
}
