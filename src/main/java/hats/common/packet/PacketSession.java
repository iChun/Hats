package hats.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import hats.common.Hats;
import hats.common.core.HatHandler;
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
    public void readFrom(ByteBuf buffer, Side side)
    {

        Hats.config.updateSession("serverHasMod", 1);
        Hats.config.updateSession("playerHatsMode", buffer.readInt());
        Hats.config.updateSession("hasVisited", buffer.readBoolean() ? 1 : 0);
        Hats.config.updateSession("lockedHat", ByteBufUtils.readUTF8String(buffer));
        Hats.config.updateSession("currentKing", ByteBufUtils.readUTF8String(buffer));

        Hats.config.updateSession("showJoinMessage", Hats.config.getSessionInt("playerHatsMode") >= 4 && Hats.config.getSessionInt("hasVisited") == 0);

        String availHats = ByteBufUtils.readUTF8String(buffer); //ignored on Free Mode
        if(Hats.config.getSessionInt("playerHatsMode") >= 4)
        {
            HatHandler.populateHatsList(availHats);
        }

    }

    @Override
    public void execute(Side side, EntityPlayer player){} //Hacky fix
}
