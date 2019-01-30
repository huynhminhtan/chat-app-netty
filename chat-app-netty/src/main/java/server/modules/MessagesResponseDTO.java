package server.modules;

public class MessagesResponseDTO {

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public MessagesResponseDTO() {

    }

    public MessagesResponseDTO(String senderID, String senderName, String content, String time, String conversationID) {
        this.senderID = senderID;
        this.senderName = senderName;
        this.content = content;
        this.time = time;
        this.conversationID = conversationID;
    }

    private String senderID;
    private String senderName;
    private String content;
    private String time;
    private String conversationID;
}
