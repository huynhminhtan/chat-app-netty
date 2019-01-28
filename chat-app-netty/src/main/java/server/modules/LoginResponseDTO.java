package server.modules;

public class LoginResponseDTO {

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserDTO getContent() {
        return content;
    }

    public void setContent(UserDTO content) {
        this.content = content;
    }

    private String status;
    private UserDTO content;

    public LoginResponseDTO() {

    }

    public LoginResponseDTO(String status, UserDTO content) {
        this.status = status;
        this.content = content;
    }

}
