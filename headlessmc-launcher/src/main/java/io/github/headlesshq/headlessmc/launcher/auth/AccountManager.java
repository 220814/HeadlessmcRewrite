package io.github.headlesshq.headlessmc.launcher.auth;

import lombok.*;
import io.github.headlesshq.headlessmc.api.config.Config;
import io.github.headlesshq.headlessmc.auth.ValidatedAccount;
import io.github.headlesshq.headlessmc.launcher.LauncherProperties;
import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.MinecraftAuth;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@CustomLog
@RequiredArgsConstructor
public class AccountManager {
    private static final String OFFLINE_UUID = "22689332a7fd41919600b0fe1135ee34";

    private final List<ValidatedAccount> accounts = new ArrayList<>();
    private final AccountValidator accountValidator;
    private final OfflineChecker offlineChecker;
    private final AccountStore accountStore;

    @Synchronized
    public @Nullable ValidatedAccount getPrimaryAccount() {
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    @Synchronized
    public void addAccount(ValidatedAccount account) {
        removeAccount(account);
        accounts.add(0, account);
        save();
    }

    @Synchronized
    public void removeAccount(ValidatedAccount account) {
        accounts.remove(account);
        accounts.removeIf(s -> Objects.equals(account.getName(), s.getName()));
        save();
    }

    @Synchronized
    public ValidatedAccount refreshAccount(ValidatedAccount account, @Nullable Config config) throws AuthException {
        log.warn("Account refresh is not yet implemented for the new auth library. Skipping refresh for: " + account.getName());
        return account;
    }

    @Deprecated
    @Synchronized
    public ValidatedAccount refreshAccount(ValidatedAccount account) throws AuthException {
        return refreshAccount(account, null);
    }

    @Synchronized
    public void load(Config config) throws AuthException {
        try {
            List<ValidatedAccount> accounts = accountStore.load();
            this.accounts.clear();
            this.accounts.addAll(accounts);
        } catch (IOException e) {
            throw new AuthException(e.getMessage());
        }

        String email = config.get(LauncherProperties.EMAIL);
        String password = config.get(LauncherProperties.PASSWORD);
        if (email != null && password != null) {
            log.warn("Email/Password login is no longer supported in this version. Please use Device Code login.");
        }

        if (config.get(LauncherProperties.REFRESH_ON_LAUNCH, false)) {
            ValidatedAccount primary = getPrimaryAccount();
            if (primary != null) {
                try {
                    refreshAccount(primary, config);
                } catch (AuthException e) {
                    log.error("Failed to refresh account " + primary.getName(), e);
                }
            }
        }
    }

    private void save() {
        try {
            accountStore.save(accounts);
        } catch (IOException e) {
            log.error(e);
        }
    }

    public LaunchAccount getOfflineAccount(Config config) throws AuthException {
        return new LaunchAccount(
            config.get(LauncherProperties.OFFLINE_TYPE, "msa"),
            config.get(LauncherProperties.OFFLINE_USERNAME, "Offline"),
            config.get(LauncherProperties.OFFLINE_UUID, OFFLINE_UUID),
            config.get(LauncherProperties.OFFLINE_TOKEN, ""),
            config.get(LauncherProperties.XUID, ""));
    }
}
                              
