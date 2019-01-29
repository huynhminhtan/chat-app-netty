package server.modules;

public class ConversationsResponseDTO {
    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

    private String conversationID;
    private String conversationName;

    public ConversationsResponseDTO() {

    }

    public ConversationsResponseDTO(String conversationID, String conversationName) {
        this.conversationID = conversationID;
        this.conversationName = conversationName;
    }
}
