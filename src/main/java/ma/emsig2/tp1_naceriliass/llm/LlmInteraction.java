package ma.emsig2.tp1_naceriliass.jsf;

public class LlmInteraction {
    private final String requestJson;
    private final String responseJson;
    private final String extractedText;

    public LlmInteraction(String requestJson, String responseJson, String extractedText) {
        this.requestJson = requestJson;
        this.responseJson = responseJson;
        this.extractedText = extractedText;
    }

    public String reponseExtraite() { return extractedText; }
    public String questionJson() { return requestJson; }
    public String reponseJson() { return responseJson; }
}
