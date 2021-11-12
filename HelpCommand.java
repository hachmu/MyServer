public class HelpCommand extends Command {
    private static final int id = 0;
    private static final String token = "HELP";
    private static String description = "Eine Liste aller validen Befehle.\nTipp: Kombiniere \"HELP\" mit einem anderen Befehl, um direkt eine kurze Erklärung zu erhalten.\nSyntax: \"HELP\" / \"HELP BEFEHL\"";

    public HelpCommand() {
        super(id, token, description);
    }

    public void execute(String args, SClient client, MyServer server) {
        System.err.println("\"HELP\"-Befehl aufgerufen: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        switch(args.toLowerCase()) {
            case "help":
                server.send(client.ip, client.port, "Eine Liste aller validen Befehle.\nTipp: Kombiniere \"HELP\" mit einem anderen Befehl, um direkt eine kurze Erklärung zu erhalten.\nSyntax: \"HELP\" / \"HELP BEFEHL\"");
                break;
            case "nick":
                server.send(client.ip, client.port, "Ändert den Name, unter dem andere Nutzer deine Nachrichten erhalten.\nSyntax: \"NICK NEUER_NICKNAME\"");
                break;
            case "privmsg":
                server.send(client.ip, client.port, "Sendet eine Nachricht an eine bestimmte Zielperson.\nSyntax: \"PRIVMSG ZIEL :NACHRICHT\"");
                break;
            case "join":
                server.send(client.ip, client.port, "Coming soon!");
                break;
            case "ping":
                server.send(client.ip, client.port, "Gibt deinen aktuellen Ping zurück...oder?\nSyntax: \"PING\"");
                break;
            case "":
                client.state = "inHelpMenu";
                String listCommands = "\nNr. | Befehl\n----------------\n0   | \"HELP\"\n1   | \"NICK\"\n2   | \"PRIVMSG\"\n3   | \"JOIN\"\n4   | \"PING\"\n\nAntworte mit der entsprechenden Nummer für eine kurze Erklärung.\nSyntax: \"HELP\" / \"HELP BEFEHL\"";
                /*
                String listCommands =  """

                Nr. | Befehl
                ------------------------
                0   | "HELP"
                1   | "NICK"
                2   | "PRIVMSG"
                3   | "JOIN"
                4   | "PING"

                Antworte mit der entprechenden Nummer für eine kurze Erklärung.
                """;
                */
                server.send(client.ip, client.port, listCommands);
                break;
            default:
                server.send(client.ip, client.port, "\"" + args + "\" ist kein valider Befehl. Benutze \"HELP\" für eine Liste aller valider Befehle.");
                break;
        }
        return;
    }
}