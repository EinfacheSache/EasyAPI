package de.einfachesache.api.console;

import java.util.Arrays;

class ConsoleCommand
{
    private final String[] commands;
    private final CommandExecutable commandExecutable;

    public ConsoleCommand(CommandExecutable commandExecutable, String... commands) {
        this.commands = commands;
        this.commandExecutable = commandExecutable;
    }

    public boolean equalsCommand(String cmd) {
        return Arrays.stream(commands).anyMatch((t) -> t.equalsIgnoreCase(cmd));
    }

    public void run(String[] args){
        commandExecutable.run(args);
    }
}
