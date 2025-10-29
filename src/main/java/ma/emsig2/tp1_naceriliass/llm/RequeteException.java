package ma.emsig2.tp1_naceriliass.jsf;

public class RequeteException extends Exception {
    private final String jsonRequete;

    public RequeteException(String message, String jsonRequete) {
        super(message);
        this.jsonRequete = jsonRequete;
    }

    public String getJsonRequete() { return jsonRequete; }
}
