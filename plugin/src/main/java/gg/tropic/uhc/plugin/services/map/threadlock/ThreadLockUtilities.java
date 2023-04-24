package gg.tropic.uhc.plugin.services.map.threadlock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static net.evilblock.cubed.util.bukkit.Tasks.sync;

/**
 * @author GrowlyX
 * @since 4/24/2023
 */
public class ThreadLockUtilities {
    public static void runMainLock(Runnable runnable) {
        final CountDownLatch latch = new CountDownLatch(1);
        sync(() -> {
            try {
                runnable.run();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            latch.countDown();
            return null;
        });

        try {
            latch.await(1L, TimeUnit.SECONDS);
        } catch (Exception ignored) {

        }
    }
}
