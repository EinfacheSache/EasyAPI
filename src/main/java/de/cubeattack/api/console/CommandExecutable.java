package de.cubeattack.api.console;

@FunctionalInterface
public interface CommandExecutable
{
    void run(String[] args);
}
