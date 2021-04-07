package me.ichun.mods.hats.client.gui;

import me.ichun.mods.hats.client.gui.window.WindowTutorial;
import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatHandler;
import me.ichun.mods.hats.common.packet.PacketGiveHat;
import net.minecraft.client.resources.I18n;

public class TutorialHandler
{
    public static void startTutorial(WorkspaceHats workspace)
    {
        if(Hats.eventHandlerClient.serverHasMod && HatHandler.getPlayerInventory(workspace.getMinecraft().player).isEmpty())
        {
            workspace.popup(0.6D, 0.5D, w -> cameraIntro((WorkspaceHats)w), I18n.format("hats.gui.tutorial.noHat"));

            Hats.channel.sendToServer(new PacketGiveHat());
        }
        else
        {
            cameraIntro(workspace);
        }
    }

    private static void cameraIntro(WorkspaceHats workspace)
    {
        WindowTutorial camTut = new WindowTutorial(workspace, WindowTutorial.Direction.LEFT, workspace.windowHatsList.getLeft() - 10, workspace.windowHatsList.getTop() + (workspace.windowHatsList.getHeight() / 2), (int)(workspace.windowHatsList.getWidth() * 0.8F), 200, w ->
        {
            WindowTutorial hatsTut = new WindowTutorial(workspace, WindowTutorial.Direction.RIGHT, workspace.windowHatsList.getLeft() + 40, workspace.windowHatsList.getTop() + (workspace.windowHatsList.getHeight() / 2), (int)(workspace.windowHatsList.getLeft() * 0.8F), 100, w2 -> {
                if(workspace.windowHatsList.getCurrentView().list.elements.isEmpty())
                {
                    workspace.popup(0.6D, 0.5D, w1 -> hatSelectIntro((WorkspaceHats)w1), I18n.format("hats.gui.tutorial.clickForHatMissing"));
                }
                else
                {
                    hatSelectIntro(workspace);
                }

            }, I18n.format("hats.gui.tutorial.hatsHere"));
            workspace.addWindowWithGreyout(hatsTut);
            hatsTut.init();

        }, I18n.format(workspace.fallback ? "hats.gui.tutorial.cameraFallback" : "hats.gui.tutorial.camera"));
        workspace.addWindowWithHalfGreyout(camTut);
        camTut.init();
    }

    private static void hatSelectIntro(WorkspaceHats workspace)
    {
        WindowTutorial hatSelectTut = new WindowTutorial(workspace, WindowTutorial.Direction.RIGHT, workspace.windowHatsList.getCurrentView().list.getLeft() + 25, workspace.windowHatsList.getCurrentView().list.getTop() + 40, (int)(workspace.windowHatsList.getLeft() * 0.8F), 125, w -> {
            WindowTutorial searchTut = new WindowTutorial(workspace, WindowTutorial.Direction.RIGHT, workspace.windowHatsList.getCurrentView().textField.getLeft(), workspace.windowHatsList.getCurrentView().textField.getTop() + (workspace.windowHatsList.getCurrentView().textField.getHeight() / 2), (int)(workspace.windowHatsList.getLeft() * 0.8F), 150, w1 -> {
                WindowTutorial addBtnTut = new WindowTutorial(workspace, WindowTutorial.Direction.RIGHT, workspace.windowSidebar.getCurrentView().randomButton.getLeft(), workspace.windowSidebar.getCurrentView().randomButton.getBottom(), (int)(workspace.windowHatsList.getLeft() * 0.8F), 150, w2 -> {
                    WindowTutorial dragTut = new WindowTutorial(workspace, WindowTutorial.Direction.RIGHT, workspace.windowHatsList.getLeft(), workspace.windowHatsList.getTop() + (workspace.windowHatsList.getHeight() / 2), (int)(workspace.windowHatsList.getLeft() * 0.8F), 200, w7 -> {
                        workspace.popup(0.6D, 0.5D, w4 -> {
                            workspace.finishTutorial();
                        }, I18n.format("hats.gui.tutorial.conclusion"));

                    }, I18n.format("hats.gui.tutorial.dragBar"));
                    workspace.addWindowWithGreyout(dragTut);
                    dragTut.init();

                }, I18n.format("hats.gui.tutorial.additionalButtons"));
                workspace.addWindowWithGreyout(addBtnTut);
                addBtnTut.init();

            }, I18n.format("hats.gui.tutorial.search"));
            workspace.addWindowWithGreyout(searchTut);
            searchTut.init();

        }, I18n.format("hats.gui.tutorial.clickForHat"));
        workspace.addWindowWithGreyout(hatSelectTut);
        hatSelectTut.init();
    }
}
