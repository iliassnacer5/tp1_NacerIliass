package ma.emsig2.tp1_naceriliass;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

@Dependent
public class LlmClientPourGemini {
    private final String GEMINI_API_KEY;

    public LlmClientPourGemini() {
        GEMINI_API_KEY = System.getenv("GEMINI_KEY");
    }

    public Response envoyerRequete(Entity<String> entity) {
        Client client = ClientBuilder.newClient();
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=";
        return client.target(endpoint + GEMINI_API_KEY)
                .request()
                .post(entity);
    }
}
