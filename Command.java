public abstract class Command implements Executable {
    private final int id;
    private final String token;
    private final String description;
    

    public Command(int id, String token, String description) {
        this.id = id;
        this.token = token;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getToken() {
        return token;
    }
    
    public String getDescription() {
        return description;
    }
}