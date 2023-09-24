package de.cubeattack.api.command;

import de.cubeattack.api.API;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ConsoleListener
{

    {
        run();
    }

    public static void main(String[] args) {
        new ConsoleListener().registerCommand(args1 -> System.out.println("Ausgabe"), "test");
        //new ConsoleListener().registerCommand(args1 -> System.out.println("test1"), "test");
    }

    private final List<ConsoleCommand> commands = new ArrayList<>();

    public ConsoleListener registerCommand(CommandExecutable runnable, String... cmd ) {
        commands.add(new ConsoleCommand(runnable, cmd));
        return this;
    }

    private void run() {
        API.getExecutorService().submit(() -> {
                    try {
                        System.out.println("trying to start terminal");

                        // Erstelle ein Terminal
                        Terminal terminal = TerminalBuilder.builder()
                                .system(true)
                                .build();

                        // Erstelle einen LineReader für die Eingabe
                        LineReader reader = LineReaderBuilder.builder()
                                .terminal(terminal)
                                .build();

                        // Schleife zum Lesen von Eingaben
                        String prompt = "> ";

                        System.out.println("Started Terminal finished askopdiojasoijdoijasx");
                        while (true) {
                            String line;
                            try {
                                line = reader.readLine();
                            } catch (UserInterruptException e) {
                                // Benutzer hat Strg+C gedrückt
                                continue;
                            } catch (EndOfFileException e) {
                                // Benutzer hat Strg+D gedrückt oder die Eingabe wurde beendet
                                break;
                            }
                            if (line.equalsIgnoreCase("Hallo")) {
                                System.out.println("Hey wie geht es dir?");
                                continue;
                            }

                            // Hier kannst du die eingegebene Zeile verarbeiten
                            System.out.println("Eingabe: " + line);
                        }

                        // Beende das Terminal
                        //terminal.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        /*
        API.getExecutorService().submit(() -> {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {

                while ( (line = reader.readLine()) != null) {
                    String cmd = line.toLowerCase().split(" ")[0];
                    String[] args = line.replace(line.split(" ")[0] + " ", "").split(" ");

                    if(commands.stream().noneMatch(consoleCommand -> consoleCommand.equalsCommand(cmd))) {
                        LogManager.getLogger().warn("Command not found");
                        continue;
                    }

                    for (ConsoleCommand command : commands) {
                        if(command.equalsCommand(cmd)) {
                            command.run(args);
                            //System.out.print("\b\b/>");
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                LogManager.getLogger().error("Error whiles reading command : " + ex.getLocalizedMessage());
            }
        });
         */
    }
}