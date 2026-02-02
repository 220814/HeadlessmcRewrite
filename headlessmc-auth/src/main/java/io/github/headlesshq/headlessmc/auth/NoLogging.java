package io.github.headlesshq.headlessmc.auth;

public enum NoLogging {
    INSTANCE;

    public void info(String s) {
    }

    public void warn(String s) {
    }

    public void error(String s) {
    }

    public void error(String s, Throwable t) {
    }
}
