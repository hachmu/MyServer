public class SClient {
    public String ip;
    public int port;
    public String nick;
    public int id;
    public String state = "";
    
    public SClient(String pIP, int pPort, int pID) {
        ip = pIP;
        port = pPort;
        id = pID;
        nick = "User" + id;
    }
}
