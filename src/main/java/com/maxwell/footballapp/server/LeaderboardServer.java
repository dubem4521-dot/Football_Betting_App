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
import com.maxwell.footballapp.model.Match;
import com.maxwell.footballapp.service.FootballApiService;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import java.net.InetSocketAddress;

// Player data with password hash
class Player {
    String username;
    String passwordHash;
    Map<Integer, String> bets = new HashMap<>();
    int points = 0;

    Player(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
}

public class LeaderboardServer extends WebSocketServer {

    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();
    private final FootballApiService apiService = new FootballApiService();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Path playerFile = Paths.get("players.json");

    public LeaderboardServer(int port) {
        super(new InetSocketAddress(port));
        loadPlayers();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome! Send your username and password to join (JOIN:username:password).");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            if (message.startsWith("JOIN:")) {
                String[] parts = message.split(":");
                String username = parts[1];
                String password = parts[2];
                String hash = hash(password);

                Player p = players.get(username);
                if (p == null) {
                    // New player
                    p = new Player(username, hash);
                    players.put(username, p);
                    conn.send("JOIN_SUCCESS");
                    savePlayers();
                } else {
                    // Existing player
                    if (!p.passwordHash.equals(hash)) {
                        conn.send("JOIN_FAIL:Incorrect password");
                        return;
                    }
                    conn.send("JOIN_SUCCESS");
                }
                broadcastLeaderboard();

            } else if (message.startsWith("BET:")) {
                String[] parts = message.split(":");
                String username = parts[1];
                int matchId = Integer.parseInt(parts[2]);
                String bet = parts[3];

                Player p = players.get(username);
                if (p != null) {
                    p.bets.put(matchId, bet);
                    savePlayers();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) { }

    @Override
    public void onError(WebSocket conn, Exception ex) { ex.printStackTrace(); }

    @Override
    public void onStart() {
        System.out.println("Leaderboard server started!");
        scheduler.scheduleAtFixedRate(this::updateResults, 0, 1, TimeUnit.MINUTES);
    }

    private void updateResults() {
        try {
            String json = apiService.fetchFixtures();
            String bootstrapJson = apiService.fetchBootstrap();
            List<Match> matches = Match.fromJson(json, bootstrapJson);

            for (Match m : matches) {
                if (!m.getStatus().equalsIgnoreCase("FINISHED")) continue;

                for (Player p : players.values()) {
                    String bet = p.bets.get(m.getId());
                    if (bet == null) continue;

                    String result = calculateResult(m);
                    if (bet.equals(result)) p.points += 3;
                }
            }
            savePlayers();
            broadcastLeaderboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String calculateResult(Match m) {
        String[] scores = m.getScore().split("[:\\-]");
        int home = Integer.parseInt(scores[0].trim());
        int away = Integer.parseInt(scores[1].trim());
        if (home > away) return "HOME";
        else if (away > home) return "AWAY";
        else return "DRAW";
    }

    private void broadcastLeaderboard() {
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        for (Player p : players.values()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("username", p.username);
            entry.put("points", p.points);
            leaderboard.add(entry);
        }
        leaderboard.sort((a, b) -> (int)b.get("points") - (int)a.get("points"));
        String json = gson.toJson(leaderboard);

        for (WebSocket conn : getConnections()) {
            conn.send(json);
        }
    }

    private void savePlayers() {
        try (Writer writer = Files.newBufferedWriter(playerFile)) {
            gson.toJson(players, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlayers() {
        if (!Files.exists(playerFile)) return;
        try (Reader reader = Files.newBufferedReader(playerFile)) {
            Map<String, Player> loaded = gson.fromJson(reader, new TypeToken<Map<String, Player>>(){}.getType());
            if (loaded != null) players.putAll(loaded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String hash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashed = md.digest(input.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashed) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static void main(String[] args) {
        new LeaderboardServer(8887).start();
    }
}





