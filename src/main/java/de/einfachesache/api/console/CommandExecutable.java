package de.einfachesache.api.console;

@FunctionalInterface
public interface CommandExecutable
{
    void run(String[] args);
}
