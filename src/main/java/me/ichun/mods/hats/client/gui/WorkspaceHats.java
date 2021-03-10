package me.ichun.mods.hats.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.client.gui.window.WindowHalfGreyout;
import me.ichun.mods.hats.client.gui.window.WindowHatsList;
import me.ichun.mods.hats.client.gui.window.WindowInputReceiver;
import me.ichun.mods.hats.client.gui.window.WindowSidebar;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class WorkspaceHats extends Workspace
{
    public static final DecimalFormat FORMATTER = new DecimalFormat("#,###,###");

    public final boolean fallback;
    public final @Nonnull LivingEntity hatEntity;
    public final HatsSavedData.HatPart hatDetails;

    public int age;

    public WindowInputReceiver windowInput;
    public WindowHatsList windowHatsList;
    public WindowSidebar windowSidebar;

    public WorkspaceHats(Screen lastScreen, boolean fallback, @Nonnull LivingEntity hatEntity)
    {
        super(lastScreen, new TranslationTextComponent("hats.gui.selection.title"), Hats.configClient.guiMinecraftStyle);
        windows.add(windowInput = new WindowInputReceiver(this));

        this.fallback = fallback || hatEntity != Minecraft.getInstance().player;
        this.hatEntity = hatEntity;
        this.hatDetails = HatHandler.getHatPart(hatEntity).createCopy();

        addWindow(windowHatsList = new WindowHatsList(this));
        addWindow(windowSidebar = new WindowSidebar(this));
    }

    @Override
    protected void init()
    {
        //TODO ADD the hats list

        int padding = 10;
        windowHatsList.constraints().right(this, Constraint.Property.Type.RIGHT, padding + 22).top(this, Constraint.Property.Type.TOP, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding);
        windowHatsList.setWidth((int)Math.floor((getWidth() / 2F)) - (padding + 22));
        windowHatsList.constraint.apply();

        //space from the list = 2 px
        windowSidebar.constraints().left(windowHatsList, Constraint.Property.Type.RIGHT, 2).top(windowHatsList, Constraint.Property.Type.TOP, 0).bottom(windowHatsList, Constraint.Property.Type.BOTTOM, 0);
        windowSidebar.setWidth(20);
        windowSidebar.constraint.apply();

        super.init(); //TODO render the player in fallback mode.
    }

    @Override
    public void resize(Minecraft mc, int width, int height)
    {
        int padding = 10;
        windowHatsList.setWidth((int)Math.floor((width / 2F)) - (padding + 22));
        super.resize(mc, width, height);
    }

    @Override
    public void renderWindows(MatrixStack stack, int mouseX, int mouseY, float partialTick) //TODO alert when new hat unlocked??
    {
        boolean invisibleEnt = hatEntity.isInvisible();
        if(invisibleEnt)
        {
            hatEntity.setInvisible(false);
        }
        if(fallback)
        {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);

            RenderSystem.pushMatrix();
            RenderSystem.translatef(0F, 0F, 200F);
            float zoom = (windowInput.camDist * 40);
            int x = (int)((windowHatsList.getLeft() / 2F) - windowInput.x * 40);
            int y = (int)((getHeight() / 4 * 3F + windowInput.y * 40) + (hatEntity.getHeight() / 2) * 50F - zoom);
            InventoryScreen.drawEntityOnScreen(x, y, Math.max(80 - (int)(hatEntity.getWidth() * 20 + zoom), 10), x - mouseX, (getHeight() / 2F) - mouseY, hatEntity);
            RenderSystem.popMatrix();

            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
        }
        super.renderWindows(stack, mouseX, mouseY, partialTick);

        if(invisibleEnt)
        {
            hatEntity.setInvisible(true);
        }
    }

    public boolean usePlayerInventory()
    {
        return !(Minecraft.getInstance().player.isCreative() && !Hats.configServer.enableCreativeModeHadHunting);
    }

    public ArrayList<HatsSavedData.HatPart> getHatPartSource() //TODO TOOLTIP for hats: name + rarity + accessory count?
    {
        return usePlayerInventory() ? Hats.eventHandlerClient.hatsInventory.hatParts : HatResourceHandler.HAT_PARTS;
    }

    @Override
    public boolean canDockWindows()
    {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double distX, double distY)
    {
        return (this.getListener() != null && this.isDragging()) && this.getListener().mouseDragged(mouseX, mouseY, button, distX, distY);
    }

    @Override
    public void addWindowWithGreyout(Window<?> window) //TODO why are users randomly being allocated hats.
    {
        WindowHalfGreyout greyout = new WindowHalfGreyout(this, window);
        addWindow(greyout);
        greyout.init();

        addWindow(window);
    }

    @Override
    public void renderBackground(MatrixStack stack) //TODO test B&S theme.
    {
        if(fallback)
        {
            this.renderBackground(stack, 0);
        }
        else
        {
            //taken from AbstractGui.fillGradient
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425);
            Tessellator tessellator = Tessellator.getInstance();
            Matrix4f matrix = stack.getLast().getMatrix();
            BufferBuilder builder = tessellator.getBuffer();
            builder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            //draw the original bits
            int z = getBlitOffset();
            fillGradient(matrix, builder, windowHatsList.getRight() - (int)(windowHatsList.getWidth() / 2F), 0, width, height, z, 0xc0101010, 0xd0101010);

            float x1 = windowHatsList.getLeft() - 20;
            float x2 = windowHatsList.getRight() - (int)(windowHatsList.getWidth() / 2F);
            float y1 = 0;
            float y2 = height;

            int colorA = 0xc0101010;
            int colorB = 0xd0101010;
            float f = (float)(colorA >> 24 & 255) / 255.0F;
            float f1 = (float)(colorA >> 16 & 255) / 255.0F;
            float f2 = (float)(colorA >> 8 & 255) / 255.0F;
            float f3 = (float)(colorA & 255) / 255.0F;
            float f4 = (float)(colorB >> 24 & 255) / 255.0F;
            float f5 = (float)(colorB >> 16 & 255) / 255.0F;
            float f6 = (float)(colorB >> 8 & 255) / 255.0F;
            float f7 = (float)(colorB & 255) / 255.0F;
            builder.pos(matrix, x2, y1, (float)z).color(f1, f2, f3, f).endVertex();
            builder.pos(matrix, x1, y1, (float)z).color(f1, f2, f3, 0).endVertex();
            builder.pos(matrix, x1, y2, (float)z).color(f5, f6, f7, 0).endVertex();
            builder.pos(matrix, x2, y2, (float)z).color(f5, f6, f7, f4).endVertex();

            tessellator.draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
        }

        RenderSystem.pushMatrix();
    }

    @Override
    public void resetBackground()
    {
        RenderSystem.popMatrix();
    }

    @Override
    public void tick()
    {
        super.tick();

        age++;
    }

    @Override
    public void onClose()
    {
        super.onClose();

        HatHandler.assignSpecificHat(hatEntity, hatDetails); //Reset

        Hats.eventHandlerClient.closeHatsMenu();
    }
}
