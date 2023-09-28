package de.cubeattack.api.command;

public class ConsoleReaderExample {


    public static void main(String[] args) {

        //new ConsoleListener().registerCommand(args1 -> System.out.println("Ausgabe"), "test");
/*
        try {
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
            while (true) {
                String line;
                try {
                    line = reader.readLine(prompt);
                } catch (UserInterruptException e) {
                    // Benutzer hat Strg+C gedrückt
                    continue;
                } catch (EndOfFileException e) {
                    // Benutzer hat Strg+D gedrückt oder die Eingabe wurde beendet
                    break;
                }
                if(line.equalsIgnoreCase("Hallo")){
                    System.out.println("Hey wie geht es dir?");
                    continue;
                }

                // Hier kannst du die eingegebene Zeile verarbeiten
                System.out.println("Eingabe: " + line);
            }

            // Beende das Terminal
            terminal.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
 */
    }

}