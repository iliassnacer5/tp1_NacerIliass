package ma.emsig2.tp1_naceriliass.jsf;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;

@Dependent
public class LlmClient implements Serializable {

    private static final String API_KEY_ENV_VAR = "GEMINI_KEY";
    private static final String GEMINI_PRO_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private final String key;
    private Client clientRest;
    private final WebTarget target;

    public LlmClient() {
        this.key = System.getenv(API_KEY_ENV_VAR);
        if (this.key == null || this.key.trim().isEmpty())
            throw new IllegalStateException("Variable d'environnement '" + API_KEY_ENV_VAR + "' non définie.");

        this.clientRest = ClientBuilder.newClient();
        this.target = clientRest.target(GEMINI_PRO_URL).queryParam("key", this.key);
    }

    public Response envoyerRequete(Entity requestEntity) {
        return target.request(MediaType.APPLICATION_JSON_TYPE).post(requestEntity);
    }

    public void closeClient() {
        if (clientRest != null) clientRest.close();
    }
}
