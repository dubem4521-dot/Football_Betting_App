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

    public Footballapp() {} // default constructor


    public Footballapp(String username) {
        this.username = username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void start(Stage primaryStage) {
        if (username == null || username.isEmpty()) {
            showAlert("Username not set! Please login first");
            return;
        }

        // Match labels
        Label lblHome = new Label("Home: ");
        Label lblAway = new Label("Away: ");
        Label lblStatus = new Label("Status: ");
        Label lblScore = new Label("Score: ");

        Button btnHome = new Button("Bet Home Win");
        Button btnAway = new Button("Bet Away Win");
        Button btnDraw = new Button("Bet Draw");
        HBox betButtons = new HBox(10, btnHome, btnDraw, btnAway);

        VBox matchDetailsBox = new VBox(10, lblHome, lblAway, lblStatus, lblScore, betButtons);
        matchDetailsBox.setStyle("-fx-padding: 20; -fx-font-size: 14;");

        matchList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblHome.setText("Home: " + newVal.getHome());
                lblAway.setText("Away: " + newVal.getAway());
                lblStatus.setText("Status: " + newVal.getStatus());
                lblScore.setText("Score: " + newVal.getScore());
            }
        });

        btnHome.setOnAction(e -> placeBet("HOME"));
        btnAway.setOnAction(e -> placeBet("AWAY"));
        btnDraw.setOnAction(e -> placeBet("DRAW"));

        VBox leaderboardBox = new VBox(10, new Label("Leaderboard"), leaderboardList);
        leaderboardBox.setStyle("-fx-padding: 20; -fx-font-size: 14;");

        SplitPane splitPane = new SplitPane(matchList, matchDetailsBox, leaderboardBox);
        splitPane.setDividerPositions(0.4, 0.7);

        Scene scene = new Scene(splitPane, 1000, 500);
        primaryStage.setTitle("Football App with Real-Time Betting");
        primaryStage.setScene(scene);
        primaryStage.show();

        loadMatches();
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
                    System.out.println("Connected to leaderboard server!");
                    send("JOIN:" + username);
                }

                @Override
                public void onMessage(String message) {
                    try {
                        List<Map<String, Object>> leaderboard = gson.fromJson(
                                message, new TypeToken<List<Map<String, Object>>>() {}.getType()
                        );
                        Platform.runLater(() -> {
                            leaderboardList.getItems().clear();
                            for (Map<String, Object> entry : leaderboard) {
                                String name = (String) entry.get("name");
                                int points = ((Double) entry.get("points")).intValue();
                                leaderboardList.getItems().add(name + " - " + points + " pts");
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
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
            showAlert("Error connecting to WebSocket: " + e.getMessage());
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






