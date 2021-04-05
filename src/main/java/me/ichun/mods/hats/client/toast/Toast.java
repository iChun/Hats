package me.ichun.mods.hats.client.toast;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class Toast implements IToast //Mostly taken from SystemToast
{
    private ITextComponent title;
    private ITextComponent subtitle;
    private long firstDrawTime;
    private boolean newDisplay;
    private int texture;

    public Toast(ITextComponent titleComponent, @Nullable ITextComponent subtitleComponent, int texture)
    {
        this.title = titleComponent;
        this.subtitle = subtitleComponent;
        this.texture = texture;
    }

    @Override
    public int func_238540_d_() { //get height
        List<ITextProperties> subtitles = subtitle == null ? ImmutableList.of() : Minecraft.getInstance().fontRenderer.getCharacterManager().func_238362_b_(subtitle, 130, Style.EMPTY);
        return 32 + Math.max(0, subtitles.size() - 1) * 12;
    }

    @Override
    public Visibility func_230444_a_(MatrixStack stack, ToastGui toastGui, long delta) {
        if (this.newDisplay) {
            this.firstDrawTime = delta;
            this.newDisplay = false;
        }

        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        int i = this.func_230445_a_();
        int j = 12;
        List<IReorderingProcessor> subtitles = subtitle == null ? ImmutableList.of() : toastGui.getMinecraft().fontRenderer.trimStringToWidth(subtitle, 130);
        if (i == 160 && subtitles.size() <= 1) {
            toastGui.blit(stack, 0, 0, 0, texture * 32, i, this.func_238540_d_());
        } else {
            int k = this.func_238540_d_();
            int l = 28;
            int i1 = Math.min(4, k - 28);
            this.func_238533_a_(stack, toastGui, i, 0, 0, 28);

            for(int j1 = 28; j1 < k - i1; j1 += 10) {
                this.func_238533_a_(stack, toastGui, i, 16, j1, Math.min(16, k - j1 - i1));
            }

            this.func_238533_a_(stack, toastGui, i, 32 - i1, k - i1, i1);
        }

        if (this.subtitle == null) {
            toastGui.getMinecraft().fontRenderer.drawText(stack, this.title, 18.0F, 12.0F, -256);
        } else {
            toastGui.getMinecraft().fontRenderer.drawText(stack, this.title, 18.0F, 7.0F, -256);

            for(int k1 = 0; k1 < subtitles.size(); ++k1) {
                toastGui.getMinecraft().fontRenderer.func_238422_b_(stack, subtitles.get(k1), 18.0F, (float)(18 + k1 * 12), -1);
            }
        }

        return delta - this.firstDrawTime < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }

    private void func_238533_a_(MatrixStack stack, ToastGui toastGui, int p_238533_3_, int p_238533_4_, int p_238533_5_, int p_238533_6_) {
        int texturePos = texture * 32;
        int i = p_238533_4_ == 0 ? 20 : 5;
        int j = Math.min(60, p_238533_3_ - i);
        toastGui.blit(stack, 0, p_238533_5_, 0, texturePos + p_238533_4_, i, p_238533_6_);

        for(int k = i; k < p_238533_3_ - j; k += 64) {
            toastGui.blit(stack, k, p_238533_5_, 32, texturePos + p_238533_4_, Math.min(64, p_238533_3_ - k - j), p_238533_6_);
        }

        toastGui.blit(stack, p_238533_3_ - j, p_238533_5_, 160 - j, texturePos + p_238533_4_, j, p_238533_6_);
    }
}