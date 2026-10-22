module com.example.postsurgeryrecoverylog {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.postsurgeryrecoverylog to javafx.fxml;
    exports com.example.postsurgeryrecoverylog;
}