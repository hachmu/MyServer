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

    /*
    String description = """
    Eine Liste aller validen Befehle.
    Tipp: Kombiniere "HELP" mit einem anderen Befehl, um direkt eine kurze Erklärung zu erhalten.
    Syntax: "HELP" / "HELP BEFEHL"
    """;
    */
    //Command nick = new Command("Ändert den Name, unter dem andere Nutzer deine Nachrichten erhalten.\nSyntax: \"NICK NEUER_NICKNAME\"");
    /*
    String description = """
    Ändert den Namen, unter dem andere Nutzer deine Nachrichten erhalten.
    Syntax: "NICK NEUER_NICKNAME"
    """;
    */
    //Command privmsg = new Command("Sendet eine Nachricht an eine bestimmte Zielperson.\nSyntax: \"PRIVMSG ZIEL :NACHRICHT\"");
    /*
    String description = """
    Sendet eine Nachricht an eine bestimmte Zielperson.
    Syntax: "PRIVMSG ZIEL :NACHRICHT"
    """;
    */
    //Command join = new Command("Coming soon!");
    //Command ping = new Command("Gibt deinen aktuellen Ping zurück...oder?\nSyntax: \"PING\"");
    /*
    String description = """
    Gibt deinen aktuellen Ping zurück...oder?
    Syntax: "PING"
    """;
    */

    public MyServer() {
        super(1025);
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
                        CMDnick(args, client);
                        break;
                    case "privmsg":
                        client.state = "";
                        CMDprivmsg(args, client);
                        break;
                    case "join":
                        client.state = "";
                        CMDjoin(args, client);
                        break;
                    case "ping":
                        client.state = ""; 
                        CMDping(args, client);
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

    public void CMDhelp(String args, SClient client) {
        System.err.println("\"HELP\"-Befehl aufgerufen: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        switch(args.toLowerCase()) {
            case "help":
                send(client.ip, client.port, "Eine Liste aller validen Befehle.\nTipp: Kombiniere \"HELP\" mit einem anderen Befehl, um direkt eine kurze Erklärung zu erhalten.\nSyntax: \"HELP\" / \"HELP BEFEHL\"");
                break;
            case "nick":
                send(client.ip, client.port, "Ändert den Name, unter dem andere Nutzer deine Nachrichten erhalten.\nSyntax: \"NICK NEUER_NICKNAME\"");
                break;
            case "privmsg":
                send(client.ip, client.port, "Sendet eine Nachricht an eine bestimmte Zielperson.\nSyntax: \"PRIVMSG ZIEL :NACHRICHT\"");
                break;
            case "join":
                send(client.ip, client.port, "Coming soon!");
                break;
            case "ping":
                send(client.ip, client.port, "Gibt deinen aktuellen Ping zurück...oder?\nSyntax: \"PING\"");
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
                send(client.ip, client.port, listCommands);
                break;
            default:
                send(client.ip, client.port, "\"" + args + "\" ist kein valider Befehl. Benutze \"HELP\" für eine Liste aller valider Befehle.");
                break;
        }
        return;
    }

    public void CMDnick(String args, SClient client) {
        System.err.println("\"NICK\"-Befehl aufgerufen: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        if(args != null && !args.isEmpty()) {
            client.nick = args;
            System.err.println("Neuer Nickname festgelegt: " + client.nick);
            send(client.ip, client.port, "Nickname geändert zu: \"" + client.nick + "\"");
        } else {
            send(client.ip, client.port, "Aktueller Nickname: \"" + client.nick + "\"");
        }
        return;
    }

    public void CMDprivmsg(String args, SClient client) {
        String ziel = args.substring(0, args.indexOf(" "));
        SClient zielClient = getSClientByNick(ziel);
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
            send(client.ip, client.port, "Nutzer \"" + ziel + "\" nicht gefunden. Achte auf die Groß-/Kleinschreibung!");
            return;
        }
        send(zielClient.ip, zielClient.port, client.nick + " --> " + zielClient.nick + ": " + pm);
        send(client.ip, client.port, client.nick + " --> " + zielClient.nick + ": " + pm);
        return;
    }

    public void CMDjoin(String args, SClient client) {
        // System.err.println("\"JOIN\"-Befehl aufgerufen: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        // send(client.ip, client.port, "Coming soon!");
        // return;

        
    }

    public void CMDping(String args, SClient client) {
        System.err.println("\"PING\"-Befehl aufgerufen: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        send(client.ip, client.port, "PONG 69ms");
        return;
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

    // private SClient getSClient(String pIP, int pPort) {
    //     for(lClients.toFirst(); lClients.hasAccess(); lClients.next()) {
    //         if(lClients.getContent().ip.equals(pIP) && lClients.getContent().port == pPort) {
    //             return lClients.getContent();
    //         }
    //     }
    //     return null;
    // }
    
    private SClient getSClientByNick(String nick) {
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