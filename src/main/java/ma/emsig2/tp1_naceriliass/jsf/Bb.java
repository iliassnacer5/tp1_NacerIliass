package ma.emsig2.tp1_naceriliass;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Named
@ViewScoped
public class Bb implements Serializable {

    @Inject
    private JsonUtilPourGemini jsonUtil;

    @Inject
    private FacesContext facesContext;

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    private String texteRequeteJson;
    private String texteReponseJson;
    private boolean debug = false;

    public Bb() {}

    public String envoyer() {
        if (question == null || question.isBlank()) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question"));
            return null;
        }

        if (conversation.isEmpty()) {
            jsonUtil.setSystemRole(this.roleSysteme);
            roleSystemeChangeable = false;
        }

        try {
            String questionAvecContexte = question +
                    "\n[Information contextuelle: " + getMomentDeLaJournee() + "]";

            LlmInteraction interaction = jsonUtil.envoyerRequete(questionAvecContexte);

            this.reponse = interaction.reponseExtraite();
            this.texteRequeteJson = interaction.questionJson();
            this.texteReponseJson = interaction.reponseJson();

        } catch (Exception e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Problème de connexion avec l'API du LLM",
                    "Problème : " + e.getMessage()));
            this.reponse = "ERREUR : Voir le message ci-dessus.";
            this.texteRequeteJson = jsonUtil.getTexteRequeteJson();
            this.texteReponseJson = "Erreur : " + e.getMessage();
            return null;
        }

        afficherConversation();
        return null;
    }

    public void toggleDebug() {
        this.setDebug(!isDebug());
    }

    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    public String getTexteRequeteJson() { return texteRequeteJson; }
    public void setTexteRequeteJson(String texteRequeteJson) { this.texteRequeteJson = texteRequeteJson; }
    public String getTexteReponseJson() { return texteReponseJson; }
    public void setTexteReponseJson(String texteReponseJson) { this.texteReponseJson = texteReponseJson; }

    public String getRoleSysteme() { return roleSysteme; }
    public void setRoleSysteme(String roleSysteme) { this.roleSysteme = roleSysteme; }
    public boolean isRoleSystemeChangeable() { return roleSystemeChangeable; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }
    public String getConversation() { return conversation.toString(); }
    public void setConversation(String conversation) { this.conversation = new StringBuilder(conversation); }

    public String nouveauChat() {
        this.conversation = new StringBuilder();
        this.reponse = null;
        this.question = null;
        this.texteRequeteJson = null;
        this.texteReponseJson = null;
        this.roleSystemeChangeable = true;
        return "index";
    }

    private void afficherConversation() {
        if (this.conversation.isEmpty()) {
            this.conversation.append("* Rôle Système (Initial):\n").append(roleSysteme).append("\n");
        }
        this.conversation.append("* User:\n").append(question).append("\n* Serveur:\n").append(reponse).append("\n");
    }

    private String getMomentDeLaJournee() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        if (hour >= 5 && hour < 12) return "C'est le Matin (Heure locale : " + now.format(DateTimeFormatter.ofPattern("HH:mm")) + ").";
        if (hour >= 12 && hour < 18) return "C'est l'Après-midi (Heure locale : " + now.format(DateTimeFormatter.ofPattern("HH:mm")) + ").";
        return "C'est le Soir ou la Nuit (Heure locale : " + now.format(DateTimeFormatter.ofPattern("HH:mm")) + ").";
    }

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();

            String roleAssistant = "You are a helpful assistant. You help the user to find the information they need.";
            this.listeRolesSysteme.add(new SelectItem(roleAssistant, "Assistant"));

            String roleTraducteur = """
                You are an interpreter. Translate English ↔ French. Short phrases get usage examples.
            """;
            this.listeRolesSysteme.add(new SelectItem(roleTraducteur, "Traducteur Anglais-Français"));

            String roleGuide = """
                You are a travel guide. Suggest places to visit and average meal prices.
            """;
            this.listeRolesSysteme.add(new SelectItem(roleGuide, "Guide touristique"));

            String roleJava = """
                You are a coding assistant specialized in Java and Jakarta EE.
                Help the user to modify beans, JSF pages, and services with clear examples.
            """;
            this.listeRolesSysteme.add(new SelectItem(roleJava, "Assistant Java/Jakarta EE"));

            if (this.roleSysteme == null && !this.listeRolesSysteme.isEmpty()) {
                this.roleSysteme = roleJava; // Nouveau rôle par défaut
            }
        }
        return this.listeRolesSysteme;
    }
}
