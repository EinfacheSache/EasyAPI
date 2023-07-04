package de.cubeattack.api.command;

@FunctionalInterface
public interface Executable
{
    void run(String[] args);
}
