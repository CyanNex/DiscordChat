package me.koenn.serverchat.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MessageCallback {

    void message(@NotNull String message, @Nullable String attachmentURL);
}
