module demo20 {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.example.demo20;

    opens com.example.demo20 to javafx.fxml;
    opens com.example.demo20.controller to javafx.fxml;
}
