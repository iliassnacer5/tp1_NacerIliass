package ma.emsig2.tp1_naceriliass.jsf;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Dependent
public class JsonUtilPourGemini implements Serializable {

    private String systemRole;
    private JsonObject requeteJson;
    private String texteRequeteJson;

    @Inject
    private LlmClient geminiClient;

    public void setSystemRole(String systemRole) { this.systemRole = systemRole; }
    public String getTexteRequeteJson() { return texteRequeteJson; }

    public LlmInteraction envoyerRequete(String question) throws RequeteException {
        if (question == null || question.trim().isEmpty())
            throw new IllegalArgumentException("Question vide.");

        String requestBody = (this.requeteJson == null) ? creerRequeteJson(systemRole, question)
                : ajouteQuestionDansJsonRequete(question);

        this.texteRequeteJson = prettyPrinting(requeteJson);

        Entity<String> entity = Entity.entity(requestBody, MediaType.APPLICATION_JSON_TYPE);
        try (Response response = geminiClient.envoyerRequete(entity)) {
            String texteReponseJson = response.readEntity(String.class);

            if (response.getStatus() == 200) {
                String reponseExtraite = extractReponse(texteReponseJson);
                return new LlmInteraction(this.texteRequeteJson, texteReponseJson, reponseExtraite);
            } else {
                throw new RequeteException("Erreur API " + response.getStatus(), texteRequeteJson);
            }
        }
    }

    private String creerRequeteJson(String systemRole, String question) {
        JsonObjectBuilder rootBuilder = Json.createObjectBuilder();
        if (systemRole != null && !systemRole.trim().isEmpty()) {
            rootBuilder.add("system_instruction",
                    Json.createObjectBuilder().add("parts",
                            Json.createArrayBuilder()
                                    .add(Json.createObjectBuilder().add("text", systemRole))
                    ));
        }

        JsonObject userPart = Json.createObjectBuilder().add("text", question).build();
        JsonObject userContent = Json.createObjectBuilder().add("role", "user")
                .add("parts", Json.createArrayBuilder().add(userPart)).build();

        JsonObject rootJson = rootBuilder.add("contents", Json.createArrayBuilder().add(userContent)).build();
        this.requeteJson = rootJson;
        return rootJson.toString();
    }

    private String ajouteQuestionDansJsonRequete(String nouvelleQuestion) {
        JsonObject newPart = Json.createObjectBuilder().add("text", nouvelleQuestion).build();
        JsonObject newContent = Json.createObjectBuilder().add("role", "user")
                .add("parts", Json.createArrayBuilder().add(newPart)).build();

        this.requeteJson = Json.createPointer("/contents/-").add(this.requeteJson, newContent);
        return this.requeteJson.toString();
    }

    private String extractReponse(String json) throws RequeteException {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonObject obj = reader.readObject();
            if (!obj.containsKey("candidates") || obj.getJsonArray("candidates").isEmpty())
                throw new RequeteException("Réponse vide.", json);

            JsonObject modelContent = obj.getJsonArray("candidates").getJsonObject(0).getJsonObject("content");
            this.requeteJson = Json.createPointer("/contents/-").add(this.requeteJson, modelContent);
            return modelContent.getJsonArray("parts").getJsonObject(0).getString("text");
        } catch (Exception e) {
            throw new RequeteException("Erreur extraction réponse JSON.", json);
        }
    }

    private String prettyPrinting(JsonObject jsonObject) {
        if (jsonObject == null) return "{}";
        Map<String, Boolean> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter writer = writerFactory.createWriter(stringWriter)) {
            writer.write(jsonObject);
        }
        return stringWriter.toString();
    }
}
