package server.modules;

import java.util.ArrayList;

public class SocketDTO {

    public class SocketCreateConversation {
        public String getConversationName() {
            return conversationName;
        }

        public void setConversationName(String conversationName) {
            this.conversationName = conversationName;
        }

        public ArrayList<String> getUsers() {
            return users;
        }

        public void setUsers(ArrayList<String> users) {
            this.users = users;
        }

        private String conversationName;
        private ArrayList<String> users;

    }

    public class SocketRequestTypeDTO {
        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public Object getContent() {
            return content;
        }

        public void setContent(Object content) {
            this.content = content;
        }

        private String messageType;
        private Object content;

    }

    public class SocketCreateConversationDTO {
        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public SocketCreateConversation getContent() {
            return content;
        }

        public void setContent(SocketCreateConversation content) {
            this.content = content;
        }

        private String messageType;
        private SocketCreateConversation content;

    }

    // First request
    public class SocketFirstRequestDTO{

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public SocketFirstRequest getContent() {
            return content;
        }

        public void setContent(SocketFirstRequest content) {
            this.content = content;
        }

        private String messageType;
        private SocketFirstRequest content;
    }

    public class SocketFirstRequest {
        public String getUserID() {
            return userID;
        }

        public void setUserID(String userID) {
            this.userID = userID;
        }

        private String userID;
    }

    // Load all conversations for user
    public static class LoadAllConversationsForUser {
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

        public LoadAllConversationsForUser() {

        }

        public LoadAllConversationsForUser(String conversationID, String conversationName) {
            this.conversationID = conversationID;
            this.conversationName = conversationName;
        }
    }

    public static class LoadAllConversationsForUserDTO {

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public ArrayList<LoadAllConversationsForUser> getContent() {
            return content;
        }

        public void setContent(ArrayList<LoadAllConversationsForUser> content) {
            this.content = content;
        }

        private String messageType;
        private ArrayList<LoadAllConversationsForUser> content;
    }

    // Send message

    public static class SendMessage {
        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getConversationID() {
            return conversationID;
        }

        public void setConversationID(String conversationID) {
            this.conversationID = conversationID;
        }

        private String sender;
            private String content;
            private String conversationID;
    }

    public static class SendMessageDTO {

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public SendMessage getContent() {
            return content;
        }

        public void setContent(SendMessage content) {
            this.content = content;
        }

        private String messageType;
        private SendMessage content;
    }

    // Response message
    public static class SendMessageResponseDTO {

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }


        private String messageType;

        public MessagesResponseDTO getContent() {
            return content;
        }

        public void setContent(MessagesResponseDTO content) {
            this.content = content;
        }

        private MessagesResponseDTO content;
    }

}
