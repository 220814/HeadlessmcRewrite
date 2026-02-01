package io.github.headlesshq.headlessmc.launcher.command;

import io.github.headlesshq.headlessmc.api.command.CommandException;
import io.github.headlesshq.headlessmc.launcher.Launcher;
import io.github.headlesshq.headlessmc.launcher.LauncherProperties;
import io.github.headlesshq.headlessmc.auth.ValidatedAccount;
import io.github.headlesshq.headlessmc.api.config.Property;
import static io.github.headlesshq.headlessmc.api.config.PropertyTypes.string;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Command to log in using a pre-defined offline account in the config.properties file.
 * Usage: offline <id>
 */
public class OfflineCommand extends AbstractLauncherCommand {

    public OfflineCommand(Launcher ctx) {
        super(ctx, "offline", "Log in with an offline account from the configuration file.");
    }

    @Override
    public void execute(String line, String... args) throws CommandException {
        if (args.length == 0) {
            throw new CommandException("Usage: offline <id> (e.g., offline 1)");
        }

        String id = args[0];
        String keyName = LauncherProperties.OFFLINE_ACCOUNT_PREFIX.getName() + id;
        Property<String> dynamicProperty = string(keyName);
        
        String username = ctx.getConfig().get(dynamicProperty, "");

        if (username == null || username.isEmpty()) {
            throw new CommandException("No offline account found for key: " + keyName + " in config.properties");
        }

        String uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)).toString();
        
        // Use the newly implemented offline constructor in ValidatedAccount
        ValidatedAccount offlineAccount = new ValidatedAccount(username, uuid, "0");
        
        ctx.getAccountManager().addAccount(offlineAccount);
        ctx.log("Successfully logged in with offline account: " + username);
    }
}
