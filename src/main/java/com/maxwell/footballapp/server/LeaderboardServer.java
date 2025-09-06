/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxwell.footballapp.server;

/**
 *
 * @author maxwe
 */


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class LeaderboardServer extends WebSocketServer {

    private final Map<String, Integer> userPoints = new HashMap<>();
    private final Map<String, String> bets = new HashMap<>(); // "username:matchId" -> betType
    private final Gson gson = new Gson();
    private final File pointsFile = new File("points.json");
    private final File betsFile = new File("bets.json");

    public LeaderboardServer(int port) {
        super(new InetSocketAddress(port));
        loadPoints();
        loadBets();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Client connected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            if (message.startsWith("JOIN:")) {
                String username = message.substring(5);
                userPoints.putIfAbsent(username, 0); // new users get 0 points
                sendLeaderboard();
            } else if (message.startsWith("BET:")) {
                // Format: BET:username:matchId:betType
                String[] parts = message.split(":");
                if (parts.length == 4) {
                    String username = parts[1];
                    String matchId = parts[2];
                    String betType = parts[3];

                    bets.put(username + ":" + matchId, betType);
                    saveBets();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Leaderboard server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    private void sendLeaderboard() {
        try {
            List<Map<String, Object>> leaderboard = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : userPoints.entrySet()) {
                Map<String, Object> user = new HashMap<>();
                user.put("name", entry.getKey());
                user.put("points", entry.getValue());
                leaderboard.add(user);
            }

            String json = gson.toJson(leaderboard);
            broadcast(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPoints() {
        if (!pointsFile.exists()) return;
        try (Reader reader = new FileReader(pointsFile)) {
            Map<String, Double> map = gson.fromJson(reader, new TypeToken<Map<String, Double>>() {}.getType());
            if (map != null) {
                map.forEach((k, v) -> userPoints.put(k, v.intValue()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePoints() {
        try (Writer writer = new FileWriter(pointsFile)) {
            gson.toJson(userPoints, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBets() {
        if (!betsFile.exists()) return;
        try (Reader reader = new FileReader(betsFile)) {
            Map<String, String> map = gson.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
            if (map != null) {
                bets.putAll(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveBets() {
        try (Writer writer = new FileWriter(betsFile)) {
            gson.toJson(bets, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Call this method after updating match results to update points
    public void updatePoints(String username, int pointsEarned) {
        userPoints.put(username, userPoints.getOrDefault(username, 0) + pointsEarned);
        savePoints();
        sendLeaderboard();
    }

    public static void main(String[] args) {
        int port = 8887;
        LeaderboardServer server = new LeaderboardServer(port);
        server.start();
    }
}






