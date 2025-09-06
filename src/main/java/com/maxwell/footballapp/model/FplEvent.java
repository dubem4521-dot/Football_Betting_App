/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxwell.footballapp.model;

/**
 *
 * @author maxwe
 */
import java.util.List;

public class FplEvent {
    private int id; // gameweek number
    private List<FplFixture> fixtures;

    public int getId() { return id; }
    public List<FplFixture> getFixtures() { return fixtures; }
}
