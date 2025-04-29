package View;

public class UserSession {

    private static UserSession instance;

    private int userId;
    private String userName;
    private int userRole;
    private int userStatus; // Thêm trường trạng thái
    private boolean loggedIn;

    private UserSession() {
        // Private constructor
        clearSession();
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(int id, String name, int role, int status) {
        this.userId = id;
        this.userName = name;
        this.userRole = role;
        this.userStatus = status; // Lưu trạng thái
        this.loggedIn = true;
    }

    // Phương thức cũ (giữ lại để tương thích)
    public void setCurrentUser(int id, String name, int role) {
        setCurrentUser(id, name, role, 1); // Mặc định là trạng thái hoạt động
    }

    public void clearSession() {
        userId = 0;
        userName = "";
        userRole = 0;
        userStatus = 0;
        loggedIn = false;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isAdmin() {
        return userRole == 1; // Giả sử role=1 là admin
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public String getUserStatusText() {
        switch (userStatus) {
            case 0:
                return "Không hoạt động";
            case 1:
                return "Đang làm việc";
            case 2:
                return "Tạm nghỉ";
            default:
                return "Không xác định";
        }
    }
    // Thêm phương thức này vào class UserSession

    public void updateUserStatus(int newStatus) {
        this.userStatus = newStatus;
    }
}
