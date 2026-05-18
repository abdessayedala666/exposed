package com.game.exposed.dto;

import java.util.ArrayList;
import java.util.List;

public class TeamDTO {

    private List<NameDTO> team = new ArrayList<>() ;
    public TeamDTO(List <NameDTO> team){
        this.team = team ;
    }
    public List<NameDTO>  getTeam(){
        return this.team ;
    }
    public void setTeam(List<NameDTO> team){
        this.team = team ;
    }
    
    
}
