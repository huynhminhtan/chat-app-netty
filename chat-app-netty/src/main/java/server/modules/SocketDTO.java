package server.modules;

import java.util.ArrayList;

public class SocketRequestDTO {

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
}
