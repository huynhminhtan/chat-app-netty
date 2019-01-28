package server.modules;

public class UserDTO {
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    private String userName;
    private String password;
    private String phone;

    public  UserDTO() {

    }

    public UserDTO(String userName, String password, String phone) {
        this.userName = userName;
        this.password = password;
        this.phone = phone;
    }

    public UserDTO(String userName, String phone) {
        this.userName = userName;
        this.password = null;
        this.phone = phone;
    }
}
