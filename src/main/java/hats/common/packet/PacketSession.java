package hats.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import hats.common.core.HatHandler;
import hats.common.core.SessionState;
import ichun.common.core.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class PacketSession extends AbstractPacket
{
    public int hatMode;
    public boolean hasVisited;
    public String serverHat;
    public String currentKing;
    public String playerHats;

    public PacketSession(){}

    public PacketSession(int serverHatMode, boolean visited, String serverHat, String currentKing, String hats)
    {
        this.hatMode = serverHatMode;
        this.hasVisited = visited;
        this.serverHat = serverHat;
        this.currentKing = currentKing;
        this.playerHats = hats;
    }

    //TODO test the length of super long hats list to see if it won't be too long
    @Override
    public void writeTo(ByteBuf buffer, Side side)
    {
        buffer.writeInt(hatMode);
        buffer.writeBoolean(hasVisited);
        ByteBufUtils.writeUTF8String(buffer, serverHat);
        ByteBufUtils.writeUTF8String(buffer, currentKing);
        ByteBufUtils.writeUTF8String(buffer, playerHats);
    }

    @Override
    public void readFrom(ByteBuf buffer, Side side, EntityPlayer player)
    {
        SessionState.serverHasMod = true;
        SessionState.serverHatMode = buffer.readInt();
        SessionState.hasVisited = buffer.readBoolean();
        SessionState.serverHat = ByteBufUtils.readUTF8String(buffer);
        SessionState.currentKing = ByteBufUtils.readUTF8String(buffer);

        SessionState.showJoinMessage = SessionState.serverHatMode >= 4 && !SessionState.hasVisited;

        String availHats = ByteBufUtils.readUTF8String(buffer); //ignored on Free Mode
        if(SessionState.serverHatMode >= 4)
        {
            HatHandler.populateHatsList(availHats);
        }

    }
}
