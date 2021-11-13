import java.util.concurrent.Semaphore;

public class MyServer extends Server {
    public static void main(String[] args) {
        MyServer server = new MyServer();
        MyClient client = new MyClient();
    }
    private Semaphore mutex = new Semaphore(1);
    
    private class SClient {
        public String ip;
        public int port;
        public String nick;
        public int id;
        public String state = "";
        // public String rank = "none";
        public Chatroom currentChatroom;
        
        public SClient(String pIP, int pPort, int pID) {
            ip = pIP;
            port = pPort;
            id = pID;
            nick = "User" + id; // rank als Teil des Nicks? Z.B.: "[ADMIN]UserX" / "[ADMIN] UserX"
        }
    }
    
    private class Chatroom {
        public String name;
        public List<SClient> lClientsInRoom = new List<SClient>();
        
        public Chatroom(String pName) {
            name = pName;
        }

        public void sendToChatroom(String pMessage) {
            for(this.lClientsInRoom.toFirst();this.lClientsInRoom.hasAccess();this.lClientsInRoom.next()) {
                send(this.lClientsInRoom.getContent().ip, this.lClientsInRoom.getContent().port, pMessage);
            }
        }
    }
    
    private class Command {
        public String description;
        
        public Command(String pDescription) {
            description = pDescription;
        }
    }

    /*
        TODO: 
        - XXX-Stellen
        + Den HELP-Befehl mit anderen kombinierbar machen.
        (+ inMenu an entprechenden Stellen auf false setzen.)
        + processClosingConnection() implementieren. (Liste mit Offline-Clients? Verworfen)
        - Chatrooms. Passwort-geschützt?
        - "MSG"-Befehl für Chatrooms. Schreibt in den aktuellen Chatroom.
        - "SCRTMSG"-Befehl für Chatrooms.
        - "INVITE"-Befehl für Chatrooms.
        - Allgemeinen Chatroom mittels sendToAll(), in den neu verbundenen Clients automatisch reinkommen. Fängt eine Message nicht mit einem validen Befehl an, wird diese roh (bzw. mit Nick) in den Raum gesendet ODER Per ALL-Befehl (ALLMSG/MSGALL?) kann ein Client in diesen Chatroom schreiben.
        - Wenn überflüssige Parameterwerte nach einem Befehl stehen, nicht ignorieren, sondern "Error"-Message zurücksenden.
        - Wenn ungültige Parameterwerte (z.B. einen nicht existenten Zielclient bei PRIVMSG), nicht ignorieren und nichts tun, sondern "Error"-Message zurücksenden.
        - Ganzen Code auf NullPointer-Exceptions vorbereiten (if(x == null) {...}).
        - Spamfilter/-schutz.
        - Ranks, z.B.: "[ADMIN]UserX". Erhalt per Passwort nach RANKUP-Befehl. Farbliche Hervorhebung der Nachrichten?
        - Für PRIVMSG: Wenn man kein Ziel angibt, wird automatisch an das letzte Ziel gesendet (sofern vorhanden). Alternativ oder als Lösungsansatz: Neue innere private Klasse Chat: Wird erstellt, 
          wenn jemand jemanden zum ersten Mal per PRIVMSG anschreibt. Attribute dementsprechend: SClient user1 = Sender der 1.PRIVMSG, user2 = Empfänger der 1.PRIVMSG;.
    */

    List<SClient> lClients = new List<SClient>();
    List<Chatroom> lChatrooms = new List<Chatroom>();
    List<Chatroom> lPublicChatrooms = new List<Chatroom>();
    int clientCounter = 0;
    String hr = "\n----------------------------------";
    Command help = new Command("Eine Liste aller validen Befehle.\nTipp: Kombiniere \"HELP\" mit einem anderen Befehl, um direkt eine kurze Erklärung zu erhalten.\nSyntax: \"HELP\" / \"HELP BEFEHL\"");
    /*
    String description = """
    Eine Liste aller validen Befehle.
    Tipp: Kombiniere "HELP" mit einem anderen Befehl, um direkt eine kurze Erklärung zu erhalten.
    Syntax: "HELP" / "HELP BEFEHL"
    """;
    */
    Command nick = new Command("Ändert den Namen, unter dem andere Nutzer deine Nachrichten erhalten.\nSyntax: \"NICK NEUER_NICKNAME\"");
    /*
    String description = """
    Ändert den Namen, unter dem andere Nutzer deine Nachrichten erhalten.
    Syntax: "NICK NEUER_NICKNAME"
    """;
    */
    Command privmsg = new Command("Sendet eine Nachricht an eine bestimmte Zielperson.\nSyntax: \"PRIVMSG ZIEL :NACHRICHT\"");
    /*
    String description = """
    Sendet eine Nachricht an eine bestimmte Zielperson.
    Syntax: "PRIVMSG ZIEL :NACHRICHT"
    """;
    */
    Command join = new Command("Lässt dich dem angegebenen Chatroom beitreten.\nWichtig: Du kannst immer nur in einem Chatroom gleichzeitig sein.\n         Du verlässt dadurch also automatisch den vorherigen Chatroom.\nTipp: Schreibe nur \"JOIN\", um eine Liste aller öffentlichen Chatrooms zu erhalten.\nSyntax: \"JOIN CHATROOM\"");
    /*
    String description = """
    Lässt dich dem angegebenen Chatroom beitreten. 
    Wichtig: Du kannst immer nur in einem Chatroom gleichzeitig sein. 
             Du verlässt dadurch also automatisch den vorherigen Chatroom.
    Tipp: Schreibe nur "JOIN", um eine Liste aller öffentlichen Chatrooms zu erhalten.
    Syntax: "JOIN CHATROOM" / "JOIN"
    """;
    */
    Command open = new Command("Eröffnet einen neuen Chatroom mit dem angegebenen Namen.\nWichtig: Du trittst diesem automatisch bei und verlässt den vorherigen Chatroom.\nTipp: Schreibe \"PRIV\" als zusätzliches Argument, um einen privaten Chatroom zu erstellen.\n      Dieser wird in keiner öffentlichen Liste stehen.\nSyntax: \"OPEN NAME\" / \"OPEN NAME PRIV\"");
    /*
    String description = """
    Eröffnet einen neuen Chatroom mit dem angegebenen Namen. 
    Wichtig: Du trittst diesem automatisch bei und verlässt den vorherigen Chatroom.
    Tipp: Schreibe "PRIV" als zusätzliches Argument, um einen privaten Chatroom zu erstellen. 
          Dieser wird in keiner öffentlichen Liste stehen.
    Syntax: "OPEN NAME" / "OPEN NAME PRIV"
    """;
    */
    Command ping = new Command("Gibt deinen aktuellen Ping zurück...oder?\nSyntax: \"PING\"");
    /*
    String description = """
    Gibt deinen aktuellen Ping zurück...oder?
    Syntax: "PING"
    """;
    */

    public MyServer() {
        super(1025);
        // XXX Per open() besser.
        Chatroom lobby = new Chatroom("Lobby");
        lChatrooms.append(lobby);
        lPublicChatrooms.append(lobby);
    }

    public void processNewConnection(String pClientIP, int pClientPort) {
        mutex.acquireUninterruptibly(); // needs to be executed at the very beginning
        SClient client = findOrCreateSClient(pClientIP, pClientPort);
        System.err.println("Neuer Client verbunden: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        mutex.release(); // needs to be executed at the very end
    }

    public void processClosingConnection(String pClientIP, int pClientPort) {
        mutex.acquireUninterruptibly(); // needs to be executed at the very beginning
        SClient client = getSClient(pClientIP, pClientPort);
        removeSClientFromList(pClientIP, pClientPort, lClients);
        removeSClientFromChatroom(pClientIP, pClientPort);
        System.err.println("Verbindung zu Client getrennt: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
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
        /* XXX
        Für case "inHelpMenu": case:Commands: raus aus dem 1. switch(cmd), break; nach dem 1. switch(cmd) weg. 
        Wird dann automatisch default ausgeführt? Wenn ja, kann man so dafür sorgen, dass man auch im helpMenu die normalen Befehle ausführen kann, ohne diese als cases doppelt im Quellcode zu haben.
        Man müsste allerdings noch etwas die jeweiligen defaults anpassen, damit in den richtigen Momenten auch immer noch die richtigen Fehlermeldungen/Antworten kommen, 
        bzw. die richtigen weiteren Prozesse ausgeführt werden.
        */
        switch(client.state) {
            case "inHelpMenu":
                switch(cmd) {
                    case "0":
                        send(client.ip, client.port, help.description);
                        break;
                    case "1":
                        send(client.ip, client.port, nick.description);
                        break;
                    case "2":
                        send(client.ip, client.port, privmsg.description);
                        break;
                    case "3":
                        send(client.ip, client.port, join.description);
                        break;
                    case "4":
                        send(client.ip, client.port, ping.description);
                        break;
                    case "help":
                        client.state = "";
                        CMDhelp(args, client);
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
                        send(client.ip, client.port, "Kein valider Befehl. Benutze \"HELP\" für eine Liste validen Befehle.");
                        break;
                }
        }
        
        mutex.release(); // needs to be executed at the very end
    }

    public void CMDhelp(String args, SClient client) {
        System.err.println("\"HELP\"-Befehl aufgerufen: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        switch(args.toLowerCase()) {
            case "help":
                send(client.ip, client.port, help.description);
                break;
            case "nick":
                send(client.ip, client.port, nick.description);
                break;
            case "privmsg":
                send(client.ip, client.port, privmsg.description);
                break;
            case "join":
                send(client.ip, client.port, join.description);
                break;
            case "ping":
                send(client.ip, client.port, ping.description);
                break;
            case "":
            // XXX Auslagern in printlCommands() Mehtode. Braucht natürlich neue Liste lCommands.
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
                send(client.ip, client.port, "\"" + args + "\" ist kein valider Befehl. Benutze \"HELP\" für eine Liste aller validen Befehle.");
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
        System.err.println("\"PRIVMSG\"-Befehl aufgerufen: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
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
        System.err.println("\"JOIN\"-Befehl aufgerufen: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        switch(args) {
            case "":
                send(client.ip, client.port, printlPublicChatrooms());
            default: // XXX Was wenn noch weitere ungültige Parameter mitgegeben werden? Z.B.: "JOIN Lobby sdlfskjfd 98zwehd"
                if(inlChatrooms(args)) {
                    Chatroom cr = getChatroom(args);
                    if(cr != client.currentChatroom) {
                        removeSClientFromChatroom(client.ip, client.port);
                        cr.lClientsInRoom.append(client); // XXX Ist cr verknüpft mit seinem Äquivalent in der Liste("getChatroom(args)")?
                        client.currentChatroom = cr;
                        cr.sendToChatroom(client.nick + " ist dem Chatroom beigetreten.");
                    }
                } else {
                    send(client.ip, client.port, "Chatroom \"" + args + "\" nicht gefunden. Achte auf die Groß-/Kleinschreibung! Benutze \"JOIN\" für eine Liste aller öffentlichen Chatrooms.");
                }
        }
        return;
    }

    // XXX Funktioniert noch nicht! (siehe Kommentare)
    public void CMDopen(String args, SClient client) {
        System.err.println("\"OPEN\"-Befehl aufgerufen: " + client.nick + "(" + client.id + ")@" + client.ip + ":" + client.port);
        /*
        Es muss überprüft werden, ob nach dem ersten " " in args (sofern vorhanden) "priv" steht. Was wenn (nicht)?
        */
        if(args.substring(args.indexOf(" ")) != -1) // args = "Name PRIV" => this = "priv" || args = "Name" => this = !!!!! NICHT GEWOLLT !!!!! "name"
        switch(args.substring(args.indexOf(" ")+1).toLowerCase()) {
            case "priv":

        }
        if(getChatroom(args) == null) { // XXX "priv"? (Annahme hier erstmal: priv steht nicht dabei) && Namen überprüfen auf ungültige Zeichen?
            Chatroom cr = new Chatroom(args);
            lChatrooms.append(cr);
            lPublicChatrooms.append(cr);
            CMDjoin(args, client);
        } else {
            send(client.ip, client.port, "Es existiert bereits ein Chatroom mit diesem Namen.");
        }
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

    private void removeSClientFromList(String pIP, int pPort, List<SClient> list) {
        for(list.toFirst(); list.hasAccess(); list.next()) {
            if(list.getContent().ip.equals(pIP) && list.getContent().port == pPort) {
                list.remove();
                return;
            }
        }
        return;
    }

    private SClient getSClient(String pIP, int pPort) {
        for(lClients.toFirst(); lClients.hasAccess(); lClients.next()) {
            if(lClients.getContent().ip.equals(pIP) && lClients.getContent().port == pPort) {
                return lClients.getContent();
            }
        }
        return null;
    }
    
    private SClient getSClientByNick(String nick) {
        for(lClients.toFirst(); lClients.hasAccess(); lClients.next()) {
            if(lClients.getContent().nick.equals(nick)) {
                return lClients.getContent();
            }
        }
        return null;
    }

    private void removeSClientFromChatroom(String pIP, int pPort) {
        SClient client = getSClient(pIP, pPort);
        for(lChatrooms.toFirst(); lChatrooms.hasAccess(); lChatrooms.next()) {
            if(lChatrooms.getContent() == client.currentChatroom) {
                Chatroom cr = lChatrooms.getContent();
                for(cr.lClientsInRoom.toFirst(); cr.lClientsInRoom.hasAccess(); cr.lClientsInRoom.next()) {
                    if(cr.lClientsInRoom.getContent() == client) {
                        cr.lClientsInRoom.remove();
                        cr.sendToChatroom(client.nick + " hat den Chatroom verlassen.");
                        return;
                    }
                }
            }
        }
        return;
    }

    private Chatroom getChatroom(String pName) {
        for(lChatrooms.toFirst(); lChatrooms.hasAccess(); lChatrooms.next()) {
            if(lChatrooms.getContent().name == pName) {
                return lChatrooms.getContent();
            }
        }
        return null;
    }
    
    private String printlPublicChatrooms() {
        String l = "\nNr. | Chatroom\n------------------------";
        int i = 0;
        for(lPublicChatrooms.toFirst(); lPublicChatrooms.hasAccess(); lPublicChatrooms.next()) {
            l += "\n" + i + "   | \"" + lPublicChatrooms.getContent().name + "\"";
        }
        l += "\n\nAntworte mit der entsprechenden Nummer für ein paar Infos zum Chatroom.";
        /*
        String l = """
        
        Nr. | Chatroom
        ------------------------
        0   | "Lobby"
        1   | "..."
        2   | "..."
        .   |   .
        .   |   .
        .   |   .
        
        Antworte mit der entsprechenden Nummer für ein paar Infos zum Chatroom.
        """;
        */
        return l;
    }

    private boolean inlChatrooms(String pName) {
        for(lChatrooms.toFirst(); lChatrooms.hasAccess(); lChatrooms.next()) {
            if(lChatrooms.getContent().name.equals(pName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void send(String pClientIP, int pClientPort, String pMessage) {
        super.send(pClientIP, pClientPort, pMessage.replace("\n", "\\n"));
    }
}