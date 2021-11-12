public class PrivMsgCommand extends Command {
    private static final int id = 2;
    private static final String token = "PRIVMSG";
    private static final String description = "Sendet eine Nachricht an eine bestimmte Zielperson.\nSyntax: \"PRIVMSG ZIEL :NACHRICHT\"";

    public PrivMsgCommand() {
        super(id, token, description);
    }

    public void execute(String args, SClient client, MyServer server) {
        String ziel = args.substring(0, args.indexOf(" "));
        SClient zielClient = server.getSClientByNick(ziel);
        String pm = args.substring(args.indexOf(":")+1);
        // System.err.println("\"PRIVMSG\"-Befehl aufgerufen:\nIP: " + client.ip 
        //                 + "\nPort: " + client.port 
        //                 + "\nID: " + client.id
        //                 + "\nNickname: \"" + client.nick 
        //                 + "\"\nZiel-IP: " + zielClient.ip 
        //                 + "\nZiel-Port: " + zielClient.port 
        //                 + "\nZiel-ID: " + zielClient.id
        //                 + "\nZiel-Nickname: \"" + zielClient.nick 
        //                 + "\"\nNachricht: \"" + pm + "\"" + hr);
        if(zielClient == null) {
            server.send(client.ip, client.port, "Nutzer \"" + ziel + "\" nicht gefunden. Achte auf die Groß-/Kleinschreibung!");
            return;
        }
        server.send(zielClient.ip, zielClient.port, client.nick + " --> " + zielClient.nick + ": " + pm);
        server.send(client.ip, client.port, client.nick + " --> " + zielClient.nick + ": " + pm);
        return;
    }
}