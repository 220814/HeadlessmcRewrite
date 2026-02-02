package io.github.headlesshq.headlessmc.auth;

// Bỏ chữ .logging ở giữa đi
import net.lenni0451.commons.httpclient.IHttpClientLogger;

public enum NoLogging implements IHttpClientLogger {
    INSTANCE;

    @Override
    public void info(String s) {
    }

    @Override
    public void warn(String s) {
    }

    @Override
    public void error(String s) {
    }

    @Override
    public void error(String s, Throwable t) {
    }
}
