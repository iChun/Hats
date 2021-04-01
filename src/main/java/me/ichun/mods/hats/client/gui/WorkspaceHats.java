package me.ichun.mods.hats.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.hats.client.gui.window.WindowHalfGreyout;
import me.ichun.mods.hats.client.gui.window.WindowHatsList;
import me.ichun.mods.hats.client.gui.window.WindowInputReceiver;
import me.ichun.mods.hats.client.gui.window.WindowSidebar;
import me.ichun.mods.hats.client.gui.window.element.ElementHatRender;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.packet.PacketHatCustomisation;
import me.ichun.mods.hats.common.packet.PacketHatLauncherCustomisation;
import me.ichun.mods.hats.common.world.HatsSavedData;
import me.ichun.mods.ichunutil.client.gui.bns.Workspace;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.common.iChunUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class WorkspaceHats extends Workspace
        implements IHatSetter
{
    public static final DecimalFormat FORMATTER = new DecimalFormat("#,###,###");

    public final boolean fallback;
    public final @Nonnull LivingEntity hatEntity;
    public final HatsSavedData.HatPart hatDetails;
    public final ItemStack hatLauncher;

    public int age;

    public WindowInputReceiver windowInput;
    public WindowHatsList windowHatsList;
    public WindowSidebar windowSidebar;

    public ArrayList<HatsSavedData.HatPart> changedHats = new ArrayList<>(); //TODO Update hat when you have your own hat replaced and you are in the GUI

    public boolean confirmed = false;

    public WorkspaceHats(Screen lastScreen, boolean fallback, @Nonnull LivingEntity hatEntity, @Nullable ItemStack hatLauncher) //TODO new hat tutorial.
    {
        super(lastScreen, new TranslationTextComponent("hats.gui.selection.title"), Hats.configClient.guiMinecraftStyle);
        windows.add(windowInput = new WindowInputReceiver(this));

        this.fallback = fallback || hatEntity != Minecraft.getInstance().player || hatLauncher != null;
        this.hatEntity = hatEntity;
        this.hatLauncher = hatLauncher;

        if(hatLauncher != null)
        {
            this.hatDetails = HatHandler.getHatPart(hatLauncher).createCopy();
        }
        else
        {
            this.hatDetails = HatHandler.getHatPart(hatEntity).createCopy();
        }

        addWindow(windowHatsList = new WindowHatsList(this));
        addWindow(windowSidebar = new WindowSidebar(this));
    }

    @Override
    protected void init()
    {
        int padding = 10;
        windowHatsList.constraints().right(this, Constraint.Property.Type.RIGHT, padding + 22).top(this, Constraint.Property.Type.TOP, padding).bottom(this, Constraint.Property.Type.BOTTOM, padding);
        windowHatsList.setWidth((int)Math.floor((getWidth() / 2F)) - (padding + 22));
        windowHatsList.constraint.apply();

        //space from the list = 2 px
        windowSidebar.constraints().left(windowHatsList, Constraint.Property.Type.RIGHT, 2).top(windowHatsList, Constraint.Property.Type.TOP, 0).bottom(windowHatsList, Constraint.Property.Type.BOTTOM, 0);
        windowSidebar.setWidth(20);
        windowSidebar.constraint.apply();

        super.init();
    }

    @Override
    public void resize(Minecraft mc, int width, int height)
    {
        int padding = 10;
        windowHatsList.setWidth((int)Math.floor((width / 2F)) - (padding + 22));
        super.resize(mc, width, height);
        windowSidebar.resize(mc, width, height);
    }

    @Override
    public void renderWindows(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderHelper.setupGuiFlatDiffuseLighting();

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

            float zoom = (windowInput.camDist * 40);
            int x = (int)((windowHatsList.getLeft() / 2F) - windowInput.x * 40);
            int y = (int)((getHeight() / 4 * 3F + windowInput.y * 40) + (hatEntity.getHeight() / 2) * 50F - zoom);
            if(hatLauncher != null)
            {
                stack.push();

                y = (int)((getHeight() / 2F + windowInput.y * 40) - zoom);

                stack.translate(x, y, 0F);

                float scale = -160F;
                stack.translate(0F, 0F, 200F);
                stack.scale(scale, scale, scale);
                stack.rotate(Vector3f.YP.rotationDegrees((iChunUtil.eventHandlerClient.ticks + partialTick + (windowInput.driftYaw * 2)) * 0.5F));

                IRenderTypeBuffer.Impl bufferSource = minecraft.getRenderTypeBuffers().getBufferSource();
                minecraft.getItemRenderer().renderItem(hatLauncher, ItemCameraTransforms.TransformType.GUI, 0xF000F0, OverlayTexture.NO_OVERLAY, stack, bufferSource);

                bufferSource.finish();
                stack.pop();
            }
            else
            {
                RenderSystem.translatef(0F, 0F, 200F);
                InventoryScreen.drawEntityOnScreen(x, y, Math.max(80 - (int)(hatEntity.getWidth() * 20 + zoom), 10), x - mouseX, (getHeight() / 2F) - mouseY, hatEntity);
            }
            RenderSystem.popMatrix();

            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.pushMatrix();
        super.renderWindows(stack, mouseX, mouseY, partialTick);
        RenderSystem.popMatrix();

        if(invisibleEnt)
        {
            hatEntity.setInvisible(true);
        }
    }

    public boolean usePlayerInventory()
    {
        return HatHandler.useInventory(Minecraft.getInstance().player);
    }

    public ArrayList<HatsSavedData.HatPart> getHatPartSource() //Also used in the Hat render
    {
        ArrayList<HatsSavedData.HatPart> source = HatHandler.getHatSource(Minecraft.getInstance().player);
        HatResourceHandler.combineLists(source, changedHats);
        return source; //TODO sort in order of have and don't have.
    } //TODO should this be cached?? this is really bad performance

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
    public void addWindowWithGreyout(Window<?> window) //TODO window/tab for ALL available hats.
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
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
        }

        RenderSystem.pushMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    public void resetBackground()
    {
        RenderSystem.popMatrix();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        super.render(stack, mouseX, mouseY, partialTick);

        if(!fallback && Hats.configClient.forceRenderToasts && minecraft.gameSettings.hideGUI)
        {
            minecraft.gameSettings.hideGUI = false;
            minecraft.getToastGui().func_238541_a_(new MatrixStack());
            minecraft.gameSettings.hideGUI = true;
        }
    }

    @Override
    public void tick()
    {
        super.tick();

        age++;
    }

    @Override
    public void onClose() //TODO update/refresh our hat cache if our hat changes whilst we're in menu
    {
        super.onClose();

        if(!confirmed)
        {
            //Reset the item's part
            if(hatLauncher != null)
            {
                HatHandler.setHatPart(hatLauncher, hatDetails);
            }
            else
            {
                HatHandler.assignSpecificHat(hatEntity, hatDetails); //Reset
            }
        }

        //Send to the server our customisations, and our new hat if we hit confirmed
        if(!changedHats.isEmpty() || confirmed) //TODO recalculate scroll dist when resized
        {
            if(hatLauncher != null) //TODO the server needs to check if we have this hat or not!
            {
                Hats.channel.sendToServer(new PacketHatLauncherCustomisation(HatHandler.getHatPart(hatLauncher)));

            }
            //Send the details of what we changed to the server. Server end only copies the customisation, not the count as well.
            //Don't send the new hat that we selected to the server if we're editing the item.
            Hats.channel.sendToServer(new PacketHatCustomisation(changedHats, confirmed, confirmed && hatLauncher == null ? HatHandler.getHatPart(hatEntity) : new HatsSavedData.HatPart()));

            //Update our inventory with what has been changed
            for(HatsSavedData.HatPart hatPart : Hats.eventHandlerClient.hatsInventory.hatParts)
            {
                for(int i = changedHats.size() - 1; i >= 0; i--)
                {
                    HatsSavedData.HatPart changedHat = changedHats.get(i);
                    if(hatPart.copyPersonalisation(changedHat))
                    {
                        changedHats.remove(i);
                    }
                }
            }

            //If we somehow modified hats we don't own
            for(HatsSavedData.HatPart customisedHat : changedHats) //these are hats we don't own.
            {
                HatsSavedData.HatPart copy = customisedHat.createCopy();
                copy.setCountOfAllTo(0);
                Hats.eventHandlerClient.hatsInventory.hatParts.add(copy);
            }

            changedHats.clear();
            confirmed = false;
        }

        Hats.eventHandlerClient.closeHatsMenu();
    }

    public void notifyChanged(@Nonnull HatsSavedData.HatPart part)
    {
        boolean found = false;
        for(int i = changedHats.size() - 1; i >= 0; i--)
        {
            if(changedHats.get(i).name.equals(part.name))
            {
                found = true;
                changedHats.remove(i);
                changedHats.add(i, part);
                break;
            }
        }

        if(!found)
        {
            changedHats.add(part);
        }
    }

    public void setNewHat(@Nullable HatsSavedData.HatPart newHat, boolean notify)
    {
        if(newHat == null)
        {
            if(hatLauncher != null)
            {
                HatHandler.setHatPart(hatLauncher, new HatsSavedData.HatPart());
            }
            else
            {
                HatHandler.assignSpecificHat(hatEntity, null); //remove the current hat
            }

            for(Element<?> element : windowHatsList.getCurrentView().list.elements)
            {
                if(element instanceof ElementHatRender)
                {
                    ((ElementHatRender<?>)element).toggleState = false; //Deselect all the hat clicky thingimagigs
                }
            }
        }
        else
        {
            if(notify)
            {
                notifyChanged(newHat);
            }

            if(hatLauncher != null)
            {
                HatHandler.setHatPart(hatLauncher, newHat);
            }
            else
            {
                HatHandler.assignSpecificHat(hatEntity, newHat); //remove the current hat
            }
        }

        onNewHatSet(newHat);
    }
}
