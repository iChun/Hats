package me.ichun.mods.hats.client.toast;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatInfo;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class NewHatPartToast implements IToast
{
    private final HatsSavedData.HatPart hatDetails;
    private final ITextComponent title;
    private final List<ITextComponent> subtitles;
    private long firstDrawTime;
    private boolean newDisplay;

    public NewHatPartToast(HatsSavedData.HatPart hatDetails, boolean newHat, ArrayList<String> names)
    {
        this.hatDetails = hatDetails;
        String type = I18n.format(newHat ? "hats.toast.unlock.hat" : "hats.toast.unlock.accessory");
        int template = Hats.configClient.hatUnlockString == 0 ? ((new Random()).nextInt(5) + 1) : Hats.configClient.hatUnlockString;
        this.title = new TranslationTextComponent("hats.toast.unlock.template" + template, type);
        this.subtitles = new ArrayList<>();
        for(String name : names)
        {
            this.subtitles.add(new StringTextComponent(name));
        }
    }

    @Override
    public int func_238540_d_() { //get height
        return 32 + Math.max(0, subtitles.size() - 1) * 12;
    }

    @Override
    public IToast.Visibility func_230444_a_(MatrixStack stack, ToastGui toastGui, long delta) {
        if (this.newDisplay) {
            this.firstDrawTime = delta;
            this.newDisplay = false;
        }

        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        int i = this.func_230445_a_();
        int height = this.func_238540_d_();
        if (i == 160 && subtitles.size() <= 1) {
            toastGui.blit(stack, 0, 0, 0, 0, i, height);
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

        HatInfo hatInfo = HatResourceHandler.getInfoAndSetToPart(hatDetails);
        if(hatInfo != null) //TODO test plumbob
        {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);

            stack.push();

            stack.translate(15F, (height / 2F) + 5F, 150F);
            stack.scale(16.0F, 16.0F, 16.0F);
            stack.rotate(Vector3f.XP.rotationDegrees(-15F));
            stack.rotate(Vector3f.YP.rotationDegrees(225F -(delta - this.firstDrawTime) / 10F));

            IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            hatInfo.getModel().render(stack, irendertypebuffer$impl.getBuffer(RenderType.getEntityTranslucent(hatInfo.project.getNativeImageResourceLocation())), 15728880, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
            irendertypebuffer$impl.finish();

            stack.pop();

            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
        }

        float spacing = 28.0F;
        toastGui.getMinecraft().fontRenderer.func_243248_b(stack, this.title, spacing, 7.0F, -256);

        for(int k1 = 0; k1 < subtitles.size(); ++k1) {
            toastGui.getMinecraft().fontRenderer.func_243248_b(stack, subtitles.get(k1), spacing, (float)(18 + k1 * 12), -1);
        }

        return delta - this.firstDrawTime < 5000L ? IToast.Visibility.SHOW : IToast.Visibility.HIDE;
    }

    private void func_238533_a_(MatrixStack stack, ToastGui toastGui, int p_238533_3_, int p_238533_4_, int p_238533_5_, int p_238533_6_) {
        int texturePos = 0;
        int i = p_238533_4_ == 0 ? 20 : 5;
        int j = Math.min(60, p_238533_3_ - i);
        toastGui.blit(stack, 0, p_238533_5_, 0, texturePos + p_238533_4_, i, p_238533_6_);

        for(int k = i; k < p_238533_3_ - j; k += 64) {
            toastGui.blit(stack, k, p_238533_5_, 32, texturePos + p_238533_4_, Math.min(64, p_238533_3_ - k - j), p_238533_6_);
        }

        toastGui.blit(stack, p_238533_3_ - j, p_238533_5_, 160 - j, texturePos + p_238533_4_, j, p_238533_6_);
    }
}
