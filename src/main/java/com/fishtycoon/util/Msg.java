package com.fishtycoon.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Msg {
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private Msg() {}

    public static Component c(String message) {
        return MINI.deserialize(message);
    }
}
