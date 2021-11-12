public class NickCommand extends Command {
    private static final int id = 1;
    private static final String token = "NICK";
    private static final String description = "Ändert den Name, unter dem andere Nutzer deine Nachrichten erhalten.\nSyntax: \"NICK NEUER_NICKNAME\"";

    public NickCommand() {
        super(id, token, description);
    }

    public void execute(String args, SClient client, MyServer server) {
        System.err.println("\"NICK\"-Befehl aufgerufen: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        if(args != null && !args.isEmpty()) {
            client.nick = args;
            System.err.println("Neuer Nickname festgelegt: " + client.nick);
            server.send(client.ip, client.port, "Nickname geändert zu: \"" + client.nick + "\"");
        } else {
            server.send(client.ip, client.port, "Aktueller Nickname: \"" + client.nick + "\"");
        }
        return;
    }
}