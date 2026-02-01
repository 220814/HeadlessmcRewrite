package io.github.headlesshq.headlessmc.launcher.command;

import io.github.headlesshq.headlessmc.api.command.CommandException;
import io.github.headlesshq.headlessmc.launcher.Launcher;
import io.github.headlesshq.headlessmc.launcher.LauncherProperties;
import io.github.headlesshq.headlessmc.launcher.auth.LaunchAccount;
import io.github.headlesshq.headlessmc.api.config.Property;
import io.github.headlesshq.headlessmc.auth.ValidatedAccount;
import static io.github.headlesshq.headlessmc.api.config.PropertyTypes.string;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OfflineCommand extends AbstractLauncherCommand {

    public OfflineCommand(Launcher ctx) {
        super(ctx, "offline", "Log in with an offline account from the configuration file.");
    }

    @Override
    public void execute(String line, String... args) throws CommandException {
        // 1. Get the suffix ID
        String id = args.length > 0 ? args[0] : "";
        String keyName = LauncherProperties.OFFLINE_ACCOUNT_PREFIX.getName() + id;
        Property<String> dynamicProperty = string(keyName);
        
        // 2. Fetch username from config
        String username = ctx.getConfig().get(dynamicProperty, "");

        if (username == null || username.isEmpty()) {
            throw new CommandException("No offline account found for key: " + keyName);
        }

        // 3. Generate deterministic UUID
        String uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)).toString();
        
        // 4. Create LaunchAccount (5 arguments as per Rewrite version)
        LaunchAccount offlineAccount = new LaunchAccount(username, uuid, "offline", "offline", "0");
        
        // 5. Update AccountManager and AccountStore
        // First, set the active account in the manager
        ctx.getAccountManager().setCurrentAccount(offlineAccount);
        
        // Second, save to disk. Since save() requires a List, we fetch all current accounts and add ours
        try {
            List<ValidatedAccount> accounts = new ArrayList<>(ctx.getAccountManager().getAccountStore().load());
            // Avoid duplicates: remove old account with same name if it exists
            accounts.removeIf(acc -> acc.getName().equalsIgnoreCase(username));
            accounts.add(offlineAccount);
            
            // Save the updated list back to .accounts.json
            ctx.getAccountManager().getAccountStore().save(accounts);
        } catch (IOException e) {
            ctx.log("Warning: Could not save offline account to .accounts.json: " + e.getMessage());
        }

        ctx.log("Successfully logged in with offline account: " + username);
    }
}
