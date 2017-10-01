package me.ichun.mods.hats.common.packet;

import io.netty.buffer.ByteBuf;
import me.ichun.mods.hats.client.gui.GuiTradeWindow;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketTradeReadyInfo extends AbstractPacket
{
    public String trader1Name;
    public boolean trader1Ready;
    public String trader2Name;
    public boolean trader2Ready;

    public PacketTradeReadyInfo(){}

    public PacketTradeReadyInfo(String t1Name, boolean t1Ready, String t2Name, boolean t2Ready)
    {
        this.trader1Name = t1Name;
        this.trader1Ready = t1Ready;
        this.trader2Name = t2Name;
        this.trader2Ready = t2Ready;
    }

    @Override
    public void writeTo(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, trader1Name);
        buffer.writeBoolean(trader1Ready);
        ByteBufUtils.writeUTF8String(buffer, trader2Name);
        buffer.writeBoolean(trader2Ready);
    }

    @Override
    public void readFrom(ByteBuf buffer)
    {
        trader1Name = ByteBufUtils.readUTF8String(buffer);
        trader1Ready = buffer.readBoolean();
        trader2Name = ByteBufUtils.readUTF8String(buffer);
        trader2Ready = buffer.readBoolean();
    }

    @Override
    public void execute(Side side, EntityPlayer player)
    {
        if(side.isClient())
        {
            handleClient(side, player);
        }
    }

    @Override
    public Side receivingSide()
    {
        return Side.CLIENT;
    }

    @SideOnly(Side.CLIENT)
    public void handleClient(Side side, EntityPlayer player)
    {
        if(Minecraft.getMinecraft().currentScreen instanceof GuiTradeWindow)
        {
            GuiTradeWindow trade = (GuiTradeWindow)Minecraft.getMinecraft().currentScreen;

            if(trader1Name.equalsIgnoreCase(Minecraft.getMinecraft().player.getName()))
            {
                if(trade.selfReady && !trader1Ready)
                {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                trade.selfReady = trader1Ready;
                trade.theirReady = trader2Ready;
            }
            else
            {
                if(trade.selfReady && !trader2Ready)
                {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                trade.selfReady = trader2Ready;
                trade.theirReady = trader1Ready;
            }
            if(!trade.theirReady)
            {
                trade.pointOfNoReturn = false;
                trade.clickedMakeTrade = false;
            }
        }
    }

}
