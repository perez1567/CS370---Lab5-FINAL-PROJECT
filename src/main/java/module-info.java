module com.example.postsurgeryrecoverylog {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.example.postsurgeryrecoverylog to javafx.fxml;
    exports com.example.postsurgeryrecoverylog;
}