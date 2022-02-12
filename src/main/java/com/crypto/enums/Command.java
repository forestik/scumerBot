package com.crypto.enums;

import java.util.Arrays;

public enum Command {
    TEAM(0, "/team"),
    TASKS(1, "/tasks"),
    TOKENS_INFO(2, "/tokens"),
    CALENDAR(3, "/calendar"),
    EXCEL(3, "/excel");

    public String command;

    Command(int i, String s) {
        this.command = s;
    }

    public String getCommand() {
        return command;
    }

    public static Command getCommandoEnum(String value) {
        return Arrays.stream(Command.values()).filter(e -> value.contains(e.getCommand())).findFirst().orElseThrow();
    }
}
