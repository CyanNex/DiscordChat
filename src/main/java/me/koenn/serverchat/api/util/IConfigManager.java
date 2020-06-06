package me.koenn.serverchat.api.util;

import org.jetbrains.annotations.NotNull;

public interface IConfigManager {

    @NotNull String getString(@NotNull String key, @NotNull String... path);
}
