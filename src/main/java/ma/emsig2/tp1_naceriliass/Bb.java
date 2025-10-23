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
import java.util.*;
// -----------------------------------


@Named
@ViewScoped
public class Bb implements Serializable {

    // --- DICTIONNAIRE AJOUTÉ POUR LA SIMULATION ---
    private static final Map<String, String> DICTIONARY = new HashMap<>();

    static {
        // Entrées Français -> Anglais avec exemples (requis par le rôle)
        DICTIONARY.put("bonjour", "hello. Example: Hello, how are you?");
        DICTIONARY.put("a bientot", "see you soon. Example: See you soon, I'll call you later.");
        DICTIONARY.put("merci", "thank you. Example: Thank you for your help.");
        DICTIONARY.put("soir", "evening. Example: I'll see you this evening.");

        // Entrées Anglais -> Français
        DICTIONARY.put("thank you", "merci. Exemple: Merci beaucoup.");
        DICTIONARY.put("goodbye", "au revoir. Exemple: Au revoir, bonne journée.");
    }
    // ----------------------------------------------

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    @Inject
    private FacesContext facesContext;

    public Bb() {
    }

    // --- Getters et Setters (inchangés) ---
    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }
    // ----------------------------------------

    /**
     * NOUVELLE MÉTHODE : Détermine le moment de la journée basé sur l'heure locale.
     * @return String (Matin, Après-midi ou Soir)
     */
    private String getMomentDeLaJournee() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();

        if (hour >= 5 && hour < 12) {
            return "C'est le Matin (Heure locale : " + now.format(DateTimeFormatter.ofPattern("HH:mm")) + ").";
        } else if (hour >= 12 && hour < 18) {
            return "C'est l'Après-midi (Heure locale : " + now.format(DateTimeFormatter.ofPattern("HH:mm")) + ").";
        } else {
            return "C'est le Soir ou la Nuit (Heure locale : " + now.format(DateTimeFormatter.ofPattern("HH:mm")) + ").";
        }
    }


    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        String simulatedResponse;

        // Normalisation de la question pour la recherche (enlève la ponctuation)
        String normalizedQuestion = question.trim().toLowerCase(Locale.FRENCH)
                .replaceAll("[^a-z0-9àâäéèêëîïôöùûüÿç ]", "");

        // Récupérer la valeur du rôle de traducteur pour la comparaison
        String roleTraducteurValue = getRolesSysteme().stream()
                .filter(item -> "Traducteur Anglais-Français".equals(item.getLabel()))
                .findFirst()
                .map(item -> (String) item.getValue())
                .orElse("");


        if (roleSysteme != null && roleSysteme.equals(roleTraducteurValue)) {
            // Logique spécifique pour le rôle "Traducteur Anglais-Français"
            if (DICTIONARY.containsKey(normalizedQuestion)) {
                simulatedResponse = DICTIONARY.get(normalizedQuestion);
            } else {
                simulatedResponse = "Désolé, je ne peux pas simuler la traduction pour: " + question +
                        ". (Le dictionnaire de simulation est limité).";
            }
        } else {
            // Logique de simulation générique pour les autres rôles
            simulatedResponse = question.toLowerCase(Locale.FRENCH);
        }

        // --- Construction de la réponse finale ---
        this.reponse = "||";

        // Ajouter le rôle système seulement si la conversation est vide
        if (this.conversation.isEmpty()) {
            this.reponse += roleSysteme.toUpperCase(Locale.FRENCH) + "\n";
            this.roleSystemeChangeable = false;
        }

        // AJOUT : Ajouter le moment de la journée à la réponse
        this.reponse += "**Information contextuelle : " + getMomentDeLaJournee() + "**\n\n";

        // Ajouter la réponse simulée
        this.reponse += simulatedResponse + "||";
        // -----------------------------------------

        afficherConversation();
        return null;
    }

    /**
     * Pour un nouveau chat.
     * @return "index"
     */
    public String nouveauChat() {
        return "index";
    }

    /**
     * Pour afficher la conversation dans le textArea de la page JSF.
     */
    private void afficherConversation() {
        this.conversation.append("* User:\n").append(question).append("\n* Serveur:\n").append(reponse).append("\n");
    }

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();

            String roleAssistant = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user type a question, you answer it.
                    """;
            this.listeRolesSysteme.add(new SelectItem(roleAssistant, "Assistant"));

            String roleTraducteur = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user type a French text, you translate it into English.
                    If the user type an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage of these words in English.
                    """;
            this.listeRolesSysteme.add(new SelectItem(roleTraducteur, "Traducteur Anglais-Français"));

            String roleGuide = """
                    Your are a travel guide. If the user type the name of a country or of a town,
                    you tell them what are the main places to visit in the country or the town
                    are you tell them the average price of a meal.
                    """;
            this.listeRolesSysteme.add(new SelectItem(roleGuide, "Guide touristique"));

            // Initialisation du rôle par défaut
            if (this.roleSysteme == null && !this.listeRolesSysteme.isEmpty()) {
                this.roleSysteme = roleTraducteur;
            }
        }

        return this.listeRolesSysteme;
    }

}
