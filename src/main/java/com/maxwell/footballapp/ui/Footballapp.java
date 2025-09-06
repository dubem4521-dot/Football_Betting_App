/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.maxwell.footballapp.ui;

/**
 *
 * @author maxwe
 */


import com.maxwell.footballapp.model.Match;
import com.maxwell.footballapp.service.FootballApiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class Footballapp extends Application {

    private final FootballApiService apiService = new FootballApiService();
    private final Gson gson = new Gson();

    private ListView<Match> matchList = new ListView<>();
    private ListView<String> leaderboardList = new ListView<>();
    private WebSocketClient wsClient;
    private String username;
    private String password;

    @Override
    public void start(Stage primaryStage) {
        // Prompt for login
        TextInputDialog usernameDialog = new TextInputDialog();
        usernameDialog.setTitle("Login");
        usernameDialog.setHeaderText("Enter username");
        username = usernameDialog.showAndWait().orElse("Player1");

        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Login");
        passwordDialog.setHeaderText("Enter password");
        password = passwordDialog.showAndWait().orElse("password");

        // Match details labels
        Label lblHome = new Label("Home: ");
        Label lblAway = new Label("Away: ");
        Label lblStatus = new Label("Status: ");
        Label lblScore = new Label("Score: ");

        // Betting buttons
        Button btnHome = new Button("Bet Home Win");
        Button btnAway = new Button("Bet Away Win");
        Button btnDraw = new Button("Bet Draw");
        HBox betButtons = new HBox(10, btnHome, btnDraw, btnAway);

        VBox matchDetailsBox = new VBox(10, lblHome, lblAway, lblStatus, lblScore, betButtons);
        matchDetailsBox.setStyle("-fx-padding: 20; -fx-font-size: 14;");

        // Update match details on selection
        matchList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblHome.setText("Home: " + newVal.getHome());
                lblAway.setText("Away: " + newVal.getAway());
                lblStatus.setText("Status: " + newVal.getStatus());
                lblScore.setText("Score: " + newVal.getScore());
            }
        });

        // Betting actions
        btnHome.setOnAction(e -> placeBet("HOME"));
        btnAway.setOnAction(e -> placeBet("AWAY"));
        btnDraw.setOnAction(e -> placeBet("DRAW"));

        // Leaderboard section
        VBox leaderboardBox = new VBox(10, new Label("Leaderboard"), leaderboardList);
        leaderboardBox.setStyle("-fx-padding: 20; -fx-font-size: 14;");

        // Layout
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(matchList, matchDetailsBox, leaderboardBox);
        splitPane.setDividerPositions(0.4, 0.7);

        Scene scene = new Scene(splitPane, 1000, 500);
        primaryStage.setTitle("Football App with Real-Time Betting");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load matches
        loadMatches();

        // Connect WebSocket
        connectWebSocket();
    }

    private void loadMatches() {
        matchList.getItems().clear();
        try {
            String bootstrapJson = apiService.fetchBootstrap();
            String fixturesJson = apiService.fetchFixtures();

            List<Match> matches = Match.fromJson(fixturesJson, bootstrapJson);
            matchList.getItems().addAll(matches);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error fetching matches: " + e.getMessage());
        }
    }

    private void connectWebSocket() {
        try {
            wsClient = new WebSocketClient(new URI("ws://localhost:8887")) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("Connected to server");
                    send("JOIN:" + username + ":" + password);
                }

                @Override
                public void onMessage(String message) {
                    if (message.startsWith("JOIN_FAIL")) {
                        Platform.runLater(() -> {
                            showAlert(message);
                            System.exit(0);
                        });
                        return;
                    }

                    List<Map<String, Object>> leaderboard = gson.fromJson(
                            message, new TypeToken<List<Map<String, Object>>>() {}.getType()
                    );

                    Platform.runLater(() -> {
                        leaderboardList.getItems().clear();
                        for (Map<String, Object> entry : leaderboard) {
                            String name = (String) entry.get("username");
                            int points = ((Double) entry.get("points")).intValue();
                            leaderboardList.getItems().add(name + " - " + points + " pts");
                        }
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            wsClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error connecting to server: " + e.getMessage());
        }
    }

    private void placeBet(String betType) {
        Match selected = matchList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a match first!");
            return;
        }

        String msg = "BET:" + username + ":" + selected.getId() + ":" + betType;
        wsClient.send(msg);
        showAlert("Bet placed: " + selected.getHome() + " vs " + selected.getAway() + " -> " + betType);
    }

    private void showAlert(String msg) {
        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, msg).showAndWait());
    }

    public static void main(String[] args) {
        launch(args);
    }
}




