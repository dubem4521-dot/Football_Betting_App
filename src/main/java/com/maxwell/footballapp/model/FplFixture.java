/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxwell.footballapp.model;

/**
 *
 * @author maxwe
 */
public class FplFixture {
    private int team_h;
    private int team_a;
    private Integer team_h_score; // may be null if not played
    private Integer team_a_score; // may be null if not played

    public int getTeam_h() { return team_h; }
    public int getTeam_a() { return team_a; }
    public Integer getTeam_h_score() { return team_h_score; }
    public Integer getTeam_a_score() { return team_a_score; }
}