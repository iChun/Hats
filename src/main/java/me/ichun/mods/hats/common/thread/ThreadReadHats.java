package me.ichun.mods.hats.common.thread;

import me.ichun.mods.hats.common.Hats;
import me.ichun.mods.hats.common.hats.HatResourceHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.concurrent.CountDownLatch;

//Most of this class is ichttt's. Thanks buddy!
public class ThreadReadHats extends Thread implements Thread.UncaughtExceptionHandler
{
    public final CountDownLatch latch = new CountDownLatch(1);

    public ThreadReadHats()
    {
        this.setName("Hats Hat Reader Thread");
        this.setDaemon(true);
    }

    @Override
    public void run()
    {
        HatResourceHandler.loadAllHats();
        latch.countDown();
    }

    @Override
    public UncaughtExceptionHandler getUncaughtExceptionHandler()
    {
        return this;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
        Hats.LOGGER.fatal("Hats Hat Reader Thread crashed!", e);
        latch.countDown();
        CrashReport report = new CrashReport("Hats Hat Reader Thread crashed!", e);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Minecraft.getInstance().crashed(report));
    }
}
