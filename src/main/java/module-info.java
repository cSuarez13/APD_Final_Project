module ca.senecacollege.apd_final_project {
    requires javafx.controls;
    requires javafx.fxml;


    opens ca.senecacollege.apd_final_project to javafx.fxml;
    exports ca.senecacollege.apd_final_project;
}