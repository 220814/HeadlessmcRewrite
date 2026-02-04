package io.github.headlesshq.headlessmc.launcher;

import lombok.CustomLog;
import lombok.experimental.UtilityClass;
import io.github.headlesshq.headlessmc.api.HeadlessMcApi;
import io.github.headlesshq.headlessmc.api.exit.ExitManager;
import io.github.headlesshq.headlessmc.auth.AbstractLoginCommand;
import io.github.headlesshq.headlessmc.launcher.auth.AuthException;
import io.github.headlesshq.headlessmc.launcher.launch.ExitToWrapperException;
import io.github.headlesshq.headlessmc.launcher.version.VersionUtil;

/**
 * Main entry point for HeadlessMc.
 */
@CustomLog
@UtilityClass
public final class Main {
    public static void main(String[] args) {
        ExitManager exitManager = new ExitManager();
        Throwable throwable = null;
        try {
            runHeadlessMc(exitManager, args);
        } catch (Throwable t) {
            throwable = t;
        } finally {
            exitManager.onMainThreadEnd(throwable);
            if (throwable instanceof ExitToWrapperException) {
                HeadlessMcApi.setInstance(null);
                LauncherApi.setLauncher(null);
            } else {
                try {
                    if (throwable == null) {
                        exitManager.exit(0);
                    } else {
                        log.error(throwable);
                        exitManager.exit(-1);
                    }
                } catch (Throwable exitThrowable) {
                    if (throwable != null && exitThrowable.getClass() == throwable.getClass()) {
                        log.error("Failed to exit!", exitThrowable);
                    }
                }
            }
        }
    }

    private static void runHeadlessMc(ExitManager exitManager, String... args) throws AuthException {
        LauncherBuilder builder = new LauncherBuilder();
        builder.exitManager(exitManager);
        builder.initLogging();
        
        // remove: AbstractLoginCommand.replaceLogger() because MinecaftAuth v5.0.1 does have
        
        if (Main.class.getClassLoader() == ClassLoader.getSystemClassLoader()) {
            log.warn("Not running from the headlessmc-launcher-wrapper. No plugin support and in-memory launching.");
        }

        Launcher launcher = builder.buildDefault();
        if (!QuickExitCliHandler.checkQuickExit(launcher, args)) {
            log.info(String.format("Detected: %s", builder.os()));
            log.info(String.format("Minecraft Dir: %s", launcher.getMcFiles()));
            launcher.log(VersionUtil.makeTable(VersionUtil.releases(launcher.getVersionService().getContents())));
            launcher.getCommandLine().read(launcher.getHeadlessMc());
        }
    }
}
