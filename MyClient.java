import java.util.Scanner;

public class MyClient extends Client {
    public MyClient() {
        super("192.168.198.55", 1025);
        scanner();
    }
    
    public void processMessage(String pMessage) {
        pMessage = pMessage.replace("\\n", "\n");
        System.out.println("----------------------------------\nClient: " + pMessage + "\n----------------------------------");
    }
    
    public void scanner() {
        Scanner scanner = new Scanner(System.in);
        String s = "";
        while(!s.equals("quit")) {
            s = scanner.next();
            s+=scanner.nextLine();
            send(s);
        }
    }
}