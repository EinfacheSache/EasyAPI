package de.cubeattack.api.command;

@FunctionalInterface
public interface CommandExecutable
{
    void run(String[] args);
}
