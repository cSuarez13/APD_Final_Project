module ca.senecacollege.apd_final_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;

    opens ca.senecacollege.apd_final_project to javafx.fxml;
    opens ca.senecacollege.apd_final_project.controller to javafx.fxml;
    opens ca.senecacollege.apd_final_project.controller.admin to javafx.fxml;
    opens ca.senecacollege.apd_final_project.controller.kiosk to javafx.fxml;
    opens ca.senecacollege.apd_final_project.model to javafx.base;

    exports ca.senecacollege.apd_final_project;
    exports ca.senecacollege.apd_final_project.controller;
    exports ca.senecacollege.apd_final_project.controller.admin;
    exports ca.senecacollege.apd_final_project.controller.kiosk;
    exports ca.senecacollege.apd_final_project.model;
    exports ca.senecacollege.apd_final_project.util;
    exports ca.senecacollege.apd_final_project.exception;
}