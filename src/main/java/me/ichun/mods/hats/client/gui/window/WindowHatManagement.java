package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;

public class WindowHatManagement extends Window<WorkspaceHats>
{
    //title will be localised, text won't.
    public WindowHatManagement(WorkspaceHats parent)
    {
        super(parent);

        disableDockingEntirely();
        disableDrag();
        disableTitle();

        setView(new ViewHatManagement(this));
    }

    public static class ViewHatManagement extends View<WindowHatManagement>
    {
        public ViewHatManagement(@Nonnull WindowHatManagement parent)
        {
            super(parent, "hats.gui.window.management.title");

            int padding = 8;

            ElementButton<?> button = new ElementButton<>(this, I18n.format("gui.done"), btn ->
            {
                parent.parent.removeWindow(parent);
            });
            button.setSize(60, 20);
            button.constraints().bottom(this, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, padding);
            elements.add(button);

            ElementButton<?> btnReload = new ElementButton<>(this, "hats.gui.window.management.reloadAllHats", btn -> {
                HatResourceHandler.HATS.forEach((name, info) -> info.destroy());

                HatResourceHandler.loadAllHats();
                HatHandler.allocateHatPools();
                parent.parent.refreshHats();

                WindowPopup.popup(parent.parent, 0.6D, 0.6D, w -> {}, I18n.format("hats.gui.window.management.reloadedAllHats"));
            });
            btnReload.setSize(80, 20);
            btnReload.constraints().width(this, Constraint.Property.Type.WIDTH, 60).top(this, Constraint.Property.Type.TOP, padding);
            elements.add(btnReload);

            //TODO not necessary on single player.
            ElementButton<?> btnSync = new ElementButton<>(this, "hats.gui.window.management.synchroniseWithServer", btn -> {
                //TODO remember to reset the toast when sync complete.
            });
            btnSync.setSize(80, 20);
            btnSync.constraints().width(this, Constraint.Property.Type.WIDTH, 60).top(btnReload, Constraint.Property.Type.BOTTOM, padding);
            elements.add(btnSync);
        }

        @Override
        public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
        {
            stack.push();
            stack.translate(0F, 0F, 375F); //silly ElementHatRender

            super.render(stack, mouseX, mouseY, partialTick);

            stack.pop();
        }
    }
}
