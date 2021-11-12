public class JoinCommand extends Command {
    private static final int id = 3;
    private static final String token = "JOIN";
    private static String description = "Coming soon!";

    public JoinCommand() {
        super(id, token, description);
    }

    public void execute(String args, SClient client, MyServer server) {
        // System.err.println("\"JOIN\"-Befehl aufgerufen: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        // send(client.ip, client.port, "Coming soon!");
        // return;
    }
}