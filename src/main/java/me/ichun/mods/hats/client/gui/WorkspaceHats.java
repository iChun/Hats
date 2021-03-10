package me.ichun.mods.hats.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.client.gui.window.WindowHatsList;
import me.ichun.mods.hats.client.gui.window.WindowInputReceiver;
import me.ichun.mods.hats.client.gui.window.WindowSidebar;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class WorkspaceHats extends Workspace
{
    public static final DecimalFormat FORMATTER = new DecimalFormat("#,###,###");

    public final boolean fallback;
    public final @Nonnull LivingEntity hatEntity;
    public final String hatDetails;

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
        this.hatDetails = HatHandler.getHatDetails(hatEntity);

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
    public void renderWindows(MatrixStack stack, int mouseX, int mouseY, float partialTick)
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
            RenderSystem.translatef(0F, 0F, 400F);
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
    public void renderBackground(MatrixStack stack)
    {
        if(fallback)
        {
            this.renderBackground(stack, 0);
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
