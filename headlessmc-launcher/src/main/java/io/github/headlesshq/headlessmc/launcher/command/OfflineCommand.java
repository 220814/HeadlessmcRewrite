package io.github.headlesshq.headlessmc.launcher.command;

import io.github.headlesshq.headlessmc.api.command.CommandException;
import io.github.headlesshq.headlessmc.launcher.Launcher;
import io.github.headlesshq.headlessmc.launcher.LauncherProperties;
import io.github.headlesshq.headlessmc.launcher.auth.LaunchAccount;
import io.github.headlesshq.headlessmc.api.config.Property;
import static io.github.headlesshq.headlessmc.api.config.PropertyTypes.string;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Command to log in using an offline (cracked) account defined in config.properties.
 * Usage: offline <id> (e.g., 'offline 100' looks for 'hmc.offline.account100')
 */
public class OfflineCommand extends AbstractLauncherCommand {

    public OfflineCommand(Launcher ctx) {
        super(ctx, "offline", "Log in with an offline account from the configuration file.");
    }

    @Override
    public void execute(String line, String... args) throws CommandException {
        // 1. Determine the suffix ID from arguments
        String id = args.length > 0 ? args[0] : "";
        
        // 2. Resolve the dynamic property key based on the prefix and ID
        String keyName = LauncherProperties.OFFLINE_ACCOUNT_PREFIX.getName() + id;
        Property<String> dynamicProperty = string(keyName);
        
        // 3. Fetch the username associated with the resolved key
        String username = ctx.getConfig().get(dynamicProperty, "");

        if (username == null || username.isEmpty()) {
            throw new CommandException("No offline account found for configuration key: " + keyName);
        }

        // 4. Generate a deterministic Version 3 UUID for the offline player
        String uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)).toString();
        
        // 5. Initialize a new LaunchAccount for the rewrite version (5-argument constructor)
        // Format: username, uuid, accessToken, userType, xuid
        LaunchAccount offlineAccount = new LaunchAccount(username, uuid, "offline", "offline", "0");
        
        // 6. Update the account store and set the active session
        ctx.getAccountManager().getAccountStore().add(offlineAccount);
        ctx.getAccountManager().getAccountStore().setCurrent(offlineAccount);

        ctx.log("Successfully logged in with offline account: " + username);
    }
}
