package me.ichun.mods.hats.client.gui.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.hats.client.gui.WorkspaceHats;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import me.ichun.mods.hats.common.packet.PacketHatsList;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

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
            button.constraints().bottom(this, Constraint.Property.Type.BOTTOM, padding);
            elements.add(button);

            ElementButton<?> btnStackLast;

            ElementButton<?> btnReload = new ElementButton<>(this, "hats.gui.window.management.reloadAllHats", btn -> {
                HatResourceHandler.HATS.forEach((name, info) -> info.destroy());

                HatResourceHandler.loadAllHats();
                if(Hats.eventHandlerClient.serverHasMod)
                {
                    HatHandler.allocateHatPools();
                }
                parent.parent.refreshHats();

                parent.parent.popup(0.6D, 0.6D, w -> {}, I18n.format("hats.gui.window.management.reloadedAllHats"));
            });
            btnReload.setSize(80, 20);
            btnReload.constraints().width(this, Constraint.Property.Type.WIDTH, 60).top(this, Constraint.Property.Type.TOP, padding);
            elements.add(btnReload);
            btnStackLast = btnReload;

            if(Hats.eventHandlerClient.serverHasMod && !(ServerLifecycleHooks.getCurrentServer() != null && ServerLifecycleHooks.getCurrentServer().isSinglePlayer())) //server has the mod and we're not in single player
            {
                ElementButton<?> btnSync = new ElementButton<>(this, "hats.gui.window.management.synchroniseWithServer", btn -> {
                    parent.parent.popup(0.6D, 0.6D, w -> {}, I18n.format("hats.gui.window.management.sync.waiting"));

                    Hats.channel.sendToServer(new PacketHatsList(HatResourceHandler.compileHatNames(), false));
                });
                btnSync.setSize(80, 20);
                btnSync.constraints().width(this, Constraint.Property.Type.WIDTH, 60).top(btnStackLast, Constraint.Property.Type.BOTTOM, padding);
                elements.add(btnSync);
                btnStackLast = btnSync;
            }

            ElementButton<?> btnRestartTuto = new ElementButton<>(this, "hats.gui.tutorial.restart", btn -> {
                parent.parent.removeWindow(parent);

                parent.parent.age = Hats.configClient.guiAnimationTime + 20;
                Hats.configClient.shownTutorial = false;
            });
            btnRestartTuto.setSize(80, 20);
            btnRestartTuto.constraints().width(this, Constraint.Property.Type.WIDTH, 60).top(btnStackLast, Constraint.Property.Type.BOTTOM, padding);
            elements.add(btnRestartTuto);
            btnStackLast = btnRestartTuto;
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
