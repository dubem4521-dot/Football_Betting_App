/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxwell.footballapp.service;

/**
 *
 * @author maxwe
 */

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FootballApiService {

    private static final String BOOTSTRAP_URL = "https://fantasy.premierleague.com/api/bootstrap-static/";
    private static final String FIXTURES_URL = "https://fantasy.premierleague.com/api/fixtures/";

    public String fetchBootstrap() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BOOTSTRAP_URL))
                .GET().build();
        HttpClient client = HttpClient.newHttpClient();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public String fetchFixtures() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FIXTURES_URL))
                .GET().build();
        HttpClient client = HttpClient.newHttpClient();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}





