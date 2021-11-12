public class PingCommand extends Command {
    private static final int id = 4;
    private static final String token = "PING";
    private static final String description = "Gibt deinen aktuellen Ping zurück...oder?\nSyntax: \"PING\"";

    public PingCommand() {
        super(id, token, description);
    }

    public void execute(String args, SClient client, MyServer server) {
        System.err.println("\"PING\"-Befehl aufgerufen: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        server.send(client.ip, client.port, "PONG 69ms");
        return;
    }
}