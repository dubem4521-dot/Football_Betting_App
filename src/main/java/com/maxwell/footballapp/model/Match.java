/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxwell.footballapp.model;

/**
 *
 * @author maxwe
 */


import com.google.gson.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Match {

    private final int id;
    private final String home;
    private final String away;
    private final String status;
    private final String score;

    public Match(int id, String home, String away, String status, String score) {
        this.id = id;
        this.home = home;
        this.away = away;
        this.status = status;
        this.score = score;
    }

    public int getId() { return id; }
    public String getHome() { return home; }
    public String getAway() { return away; }
    public String getStatus() { return status; }
    public String getScore() { return score; }

    public boolean isUpcoming() {
        return "UPCOMING".equalsIgnoreCase(status);
    }

    public boolean isFinished() {
        return "FINISHED".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return home + " " + score + " " + away + " (" + status + ")";
    }

    /**
     * Converts JSON from API to List<Match> with team names
     * @param fixturesJson JSON array from fixtures API
     * @param bootstrapJson JSON object from bootstrap API (contains team info)
     */
    public static List<Match> fromJson(String fixturesJson, String bootstrapJson) {
        List<Match> matches = new ArrayList<>();
        Gson gson = new Gson();

        JsonArray fixtures = JsonParser.parseString(fixturesJson).getAsJsonArray();
        JsonObject bootstrap = JsonParser.parseString(bootstrapJson).getAsJsonObject();
        JsonArray teams = bootstrap.getAsJsonArray("teams");

        // Build team ID → name map
        Map<Integer, String> teamMap = new HashMap<>();
        for (JsonElement teamEl : teams) {
            JsonObject teamObj = teamEl.getAsJsonObject();
            int id = teamObj.get("id").getAsInt();
            String name = teamObj.get("name").getAsString();
            teamMap.put(id, name);
        }

        // Parse fixtures
        for (JsonElement fixtureEl : fixtures) {
            JsonObject f = fixtureEl.getAsJsonObject();
            int id = f.get("id").getAsInt();
            int homeId = f.get("team_h").getAsInt();
            int awayId = f.get("team_a").getAsInt();
            boolean finished = f.get("finished").getAsBoolean();
            String status = finished ? "FINISHED" : "UPCOMING";

            String score;
            if (finished) {
                score = f.get("team_h_score").getAsInt() + " : " + f.get("team_a_score").getAsInt();
            } else {
                score = "0 : 0";
            }

            String homeName = teamMap.getOrDefault(homeId, "Unknown");
            String awayName = teamMap.getOrDefault(awayId, "Unknown");

            matches.add(new Match(id, homeName, awayName, status, score));
        }

        return matches;
    }
}









