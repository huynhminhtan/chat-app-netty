package server.modules;

public class UserResponseDTO {
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    private String userName;
    private String phone;

    public UserResponseDTO() {

    }

    public UserResponseDTO(String userName, String phone) {
        this.userName = userName;
        this.phone = phone;
    }
}
