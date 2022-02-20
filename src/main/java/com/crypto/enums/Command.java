package com.crypto.enums;

import java.util.Arrays;

public enum Command {
    TEAM("/team"),
    TASKS("/tasks"),
    TOKENS_INFO("/tokens"),
    CALENDAR("/calendar"),
    EXCEL("/excel"),
    TAG_IN("/in"),
    TAG_ALL("/everyone"),
    RATING("/rating"),
    ADD_RATING("/up"),
    LOWER_RATING("/down");

    public String command;

    Command(String s) {
        this.command = s;
    }

    public String getCommand() {
        return command;
    }

    public static Command getCommandoEnum(String value) {
        return Arrays.stream(Command.values()).filter(e -> value.contains(e.getCommand())).findFirst().orElseThrow();
    }
}
