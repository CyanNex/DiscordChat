package me.koenn.serverchat.api.util;

import org.jetbrains.annotations.NotNull;

public interface MessageCallback {

    void message(@NotNull String message);
}
