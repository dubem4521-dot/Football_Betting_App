/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxwell.footballapp.ui;

/**
 *
 * @author maxwe
 */


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class LoginApp extends Application {

    private Stage primaryStage;
    private Map<String, String> accounts = new HashMap<>();
    private final File accountsFile = new File("accounts.json");
    private final Gson gson = new Gson();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        loadAccounts(); // Load accounts from file at startup
        showLoginScene();
    }

    private void showLoginScene() {
        Label lblUser = new Label("Username:");
        TextField tfUser = new TextField();
        Label lblPass = new Label("Password:");
        PasswordField pfPass = new PasswordField();

        Button btnLogin = new Button("Login");
        Button btnRegister = new Button("Register");

        HBox buttons = new HBox(10, btnLogin, btnRegister);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(10, lblUser, tfUser, lblPass, pfPass, buttons);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20;");

        btnLogin.setOnAction(e -> {
            String username = tfUser.getText().trim();
            String password = pfPass.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Please enter both username and password!");
                return;
            }

            if (accounts.containsKey(username) && accounts.get(username).equals(password)) {
                showMainApp(username);
            } else {
                showAlert("Invalid username or password!");
            }
        });

        btnRegister.setOnAction(e -> {
            String username = tfUser.getText().trim();
            String password = pfPass.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Please enter both username and password!");
                return;
            }

            if (accounts.containsKey(username)) {
                showAlert("Username already exists!");
            } else {
                accounts.put(username, password);
                saveAccounts(); // Save new account to file
                showAlert("Account created successfully!");
            }
        });

        Scene scene = new Scene(root, 400, 250);
        primaryStage.setTitle("Football App Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showMainApp(String username) {
        Platform.runLater(() -> {
            try {
                // Create a new Footballapp instance and open the stage
                Footballapp app = new Footballapp();
                app.setUsername(username); // pass the username
                Stage mainStage = new Stage();
                app.start(mainStage);      // call start() instead of launch()
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error opening main app: " + ex.getMessage());
            }
        });
        primaryStage.close(); // close login stage
    }


    private void loadAccounts() {
        if (!accountsFile.exists()) return;

        try (FileReader reader = new FileReader(accountsFile)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> loaded = gson.fromJson(reader, type);
            if (loaded != null) accounts.putAll(loaded);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading accounts: " + e.getMessage());
        }
    }

    private void saveAccounts() {
        try (FileWriter writer = new FileWriter(accountsFile)) {
            gson.toJson(accounts, writer);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error saving accounts: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, msg).showAndWait());
    }

    public static void main(String[] args) {
        launch(args);
    }
}



