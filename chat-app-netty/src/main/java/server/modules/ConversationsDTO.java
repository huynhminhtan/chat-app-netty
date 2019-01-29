package server.modules;

import java.util.ArrayList;

public class ConversationsDTO {
    public String getConversationsName() {
        return conversationsName;
    }

    public void setConversationsName(String conversationsName) {
        this.conversationsName = conversationsName;
    }

    public String getConversationsID() {
        return conversationsID;
    }

    public void setConversationsID(String conversationsID) {
        this.conversationsID = conversationsID;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    private String conversationsName;
    private String conversationsID;
    private ArrayList<String> users;

    public ConversationsDTO( String conversationsID, String conversationsName, ArrayList<String> users){
        this.conversationsID = conversationsID;
        this.conversationsName = conversationsName;
        this.users = users;
    }

    public  ConversationsDTO(){

    }
}
