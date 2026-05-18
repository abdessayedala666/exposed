package com.game.exposed.Exceptions;

public class UsernameNotFoundException extends IllegalArgumentException {
    public UsernameNotFoundException(){
        super("this user doesnt exist") ;
    }
    
}
