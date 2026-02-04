package io.github.headlesshq.headlessmc.launcher.command.login;

import lombok.CustomLog;
import io.github.headlesshq.headlessmc.api.command.CommandException;
import io.github.headlesshq.headlessmc.auth.AbstractLoginCommand;
import io.github.headlesshq.headlessmc.launcher.Launcher;
import io.github.headlesshq.headlessmc.launcher.auth.AuthException;
import io.github.headlesshq.headlessmc.auth.ValidatedAccount;
import net.raphimc.minecraftauth.java.model.MinecraftProfile;
import net.raphimc.minecraftauth.java.model.MinecraftToken;

@CustomLog
public class LoginCommand extends AbstractLoginCommand {
    private final Launcher launcher;

    public LoginCommand(Launcher ctx) {
        super(ctx);
        this.launcher = ctx;
    }

    @Override
    public void execute(String line, String... args) throws CommandException {
        super.execute(line, args);
    }

    @Override
    protected void onSuccessfulLogin(MinecraftProfile profile, MinecraftToken token) {
        ValidatedAccount validatedAccount;
        try {
            // clion
            validatedAccount = launcher.getAccountManager().getAccountValidator().validate(profile, token);
        } catch (AuthException e) {
            ctx.log(e.getMessage());
            return;
        }

        launcher.log("Logged into account " + validatedAccount.getName() + " successfully!");
        launcher.getAccountManager().addAccount(validatedAccount);
    }
 }
