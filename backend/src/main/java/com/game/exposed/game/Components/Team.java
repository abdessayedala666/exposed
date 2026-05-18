package com.game.exposed.game.Components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Team {
    private final List<Player> teammates = new ArrayList<>();
    private int totalScore ;
    public Team(List<Player> teammates ){
        if (teammates.size() != 2){
            throw new IllegalArgumentException("invalid argmuent");
        }
        for (Player player : teammates){
        this.teammates.add(player) ;}
        this.totalScore = 0 ;

    }
    public int calculateSetScore(){
        int setScore = 0 ;
        for (Player player : this.teammates){
            setScore+=player.calculateScore() ;
        }
        return setScore ;
    }
    public void  CalculateTotalScore (){
        int setScore = calculateSetScore() ;
        this.totalScore += setScore ;  
    }
    public int getTotalScore(){
        return this.totalScore ; 
    }
    public boolean hasWon(int threshHold){
        return totalScore >= threshHold ; 
    }
    public List<Player> getTeammates() {
        return Collections.unmodifiableList(teammates) ;
    }
    public void resetTotalScore(){
        this.totalScore = 0 ;
    }
    public Player getPlayer1(){
        return teammates.get(0);
    }
    public Player getPlayer2(){
        return teammates.get(1) ;
    }
}

    
    

