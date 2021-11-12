import java.util.concurrent.Semaphore;

public class MyServer extends Server {
    private Semaphore mutex = new Semaphore(1);
    
    private class Chatroom {
        public String name;
        public int id;
        public List<SClient> lClientsInRoom = new List<SClient>();
        
        public Chatroom(String pName) {
            name = pName;
            lChatrooms.append(this);
        }
    }
    

    /*
        TODO: 
        + Den HELP-Befehl mit anderen kombinierbar machen.
        + inMenu an entprechenden Stellen auf false setzen.
        - processClosingConnection() implementieren. Liste mit Offline-Clients?
        - Chatrooms.
        - "SCRTMSG"-Befehl für Chatrooms.
        - Allgemeinen Chatroom mittels sendToAll(), in den neu verbundenen Clients automatisch reinkommen. Fängt eine Message nicht mit einem validen Befehl an, wird diese roh (bzw. mit Nick) in den Raum gesendet ODER Per ALL-Befehl (ALLMSG/MSGALL?) kann ein Client in diesen Chatroom schreiben.
        - Wenn überflüssige Parameterwerte nach einem Befehl stehen, nicht ignorieren, sondern "Error"-Message zurücksenden.
        - Wenn ungültige Parameterwerte (z.B. einen nicht existenten Zielclient bei PRIVMSG), nicht ignorieren und nichts tun, sondern "Error"-Message zurücksenden.
        - Ganzen Code auf NullPointer-Exceptions vorbereiten (if(x == null) {...}).
    */

    List<SClient> lClients = new List<SClient>();
    List<Chatroom> lChatrooms = new List<Chatroom>();
    int clientCounter = 0;
    String hr = "\n----------------------------------";

    List<Command> lCommands = new List<Command>();

    public MyServer() {
        super(1025);
        loadCommands(lCommands);
    }

    private void loadCommands(List<Command> commands) {
        commands.append(new HelpCommand());
        commands.append(new NickCommand());
        commands.append(new PrivMsgCommand());
        commands.append(new JoinCommand());
        commands.append(new PingCommand());
    }

    public void processNewConnection(String pClientIP, int pClientPort) {
        mutex.acquireUninterruptibly(); // needs to be executed at the very beginning
        SClient client = findOrCreateSClient(pClientIP, pClientPort);
        System.err.println("Neuer Client verbunden: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        mutex.release(); // needs to be executed at the very end
    }

    public void processClosingConnection(String pClientIP, int pClientPort) {
        mutex.acquireUninterruptibly(); // needs to be executed at the very beginning
        mutex.release(); // needs to be executed at the very end
    }

    public void processMessage(String pClientIP, int pClientPort, String pMessage) {
        mutex.acquireUninterruptibly(); // needs to be executed at the very beginning
        SClient client = findOrCreateSClient(pClientIP, pClientPort);
        String cmd = pMessage.toLowerCase(), args = "";
        if(pMessage.indexOf(" ") != -1) {
            cmd = pMessage.substring(0, pMessage.indexOf(" ")).toLowerCase();
            args = pMessage.substring(pMessage.indexOf(" ")+1);
        }
        
        System.err.println("Neue Message: \"" + pMessage + "\" von " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        
        switch(client.state) {
            case "inHelpMenu":
                switch(cmd) {
                    case "0":
                        send(client.ip, client.port, HelpCommand.getDescription());
                        break;
                    case "1":
                        send(client.ip, client.port, NickCommand.getDescription());
                        break;
                    case "2":
                        send(client.ip, client.port, PrivMsgCommand.getDescription());
                        break;
                    case "3":
                        send(client.ip, client.port, JoinCommand.getDescription());
                        break;
                    case "4":
                        send(client.ip, client.port, PingCommand.getDescription());
                        break;
                    case "help":
                        client.state = "";
                        HelpCommand.execute(args, client, this);
                        break;
                    case "nick":
                        client.state = "";
                        NickCommand.execute(args, client, this);
                        break;
                    case "privmsg":
                        client.state = "";
                        PrivMsgCommand.execute(args, client, this);
                        break;
                    case "join":
                        client.state = "";
                        JoinCommand.execute(args, client, this);
                        break;
                    case "ping":
                        client.state = ""; 
                        PingCommand.execute(args, client, this);
                        break;
                    default:
                        send(client.ip, client.port, "Es existiert kein Befehl mit dieser Nummer.");
                        break;       
                }
                break;
            default:
                switch(cmd) {
                    case "help":
                        CMDhelp(args, client);
                        break;
                    case "nick":
                        CMDnick(args, client);
                        break;
                    case "privmsg":
                        CMDprivmsg(args, client);
                        break;
                    case "join":
                        CMDjoin(args, client);
                        break;
                    case "ping":
                        CMDping(args, client);
                        break;
                    default:
                        send(client.ip, client.port, "Kein valider Befehl. Benutze \"HELP\" für eine Liste valider Befehle.");
                        break;
                }
        }
        
        mutex.release(); // needs to be executed at the very end
    }

    private SClient findOrCreateSClient(String pIP, int pPort) {
        for(lClients.toFirst(); lClients.hasAccess(); lClients.next()) {
            if(lClients.getContent().ip.equals(pIP) && lClients.getContent().port == pPort) {
                return lClients.getContent();
            }
        }
        SClient client = new SClient(pIP, pPort, ++clientCounter);
        lClients.append(client);
        return client;
    }
    
    public SClient getSClientByNick(String nick) {
        for(lClients.toFirst(); lClients.hasAccess(); lClients.next()) {
            if(lClients.getContent().nick.equals(nick)) {
                return lClients.getContent();
            }
        }
        return null;
    }

    @Override
    public void send(String pClientIP, int pClientPort, String pMessage) {
        super.send(pClientIP, pClientPort, pMessage.replace("\n", "\\n"));
    }
}