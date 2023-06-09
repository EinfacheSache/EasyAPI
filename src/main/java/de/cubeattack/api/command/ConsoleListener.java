package de.cubeattack.api.command;

import de.cubeattack.api.logger.LogManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@SuppressWarnings("unused")
public class ConsoleListener
{

    {
        run();
    }


    private final List<ConsoleCommand> commands = new ArrayList<>();

    public ConsoleListener registerCommand(Executable runnable, String... cmd ) {
        commands.add(new ConsoleCommand(runnable, cmd));
        return this;
    }

    private void run() {
        new Thread(() -> {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while ((line = reader.readLine()) != null) {
                    String cmd = line.toLowerCase().split(" ")[0];
                    String[] args = line.replace(line.split(" ")[0] + " ", "").split(" ");

                    if(commands.stream().noneMatch(consoleCommand -> consoleCommand.equalsCommand(cmd))) {
                        LogManager.getLogger().warn("Command not found");
                        continue;
                    }

                    for (ConsoleCommand command : commands) {
                        if(command.equalsCommand(cmd)) {
                            command.run(args);
                            System.out.print("\b\b/>");
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                LogManager.getLogger().error("Error whiles reading command : " + ex.getLocalizedMessage());
            }
        }).start();
    }
}