package com.game.exposed.dto;

public class MoveRequestDto {
    private CardRequest card;
    private String action;
    private Integer facingSeat;

    public CardRequest getCard() {
        return card;
    }

    public void setCard(CardRequest card) {
        this.card = card;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getFacingSeat() {
        return facingSeat;
    }

    public void setFacingSeat(Integer facingSeat) {
        this.facingSeat = facingSeat;
    }

    public static class CardRequest {
        private Integer number;
        private String suit;

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        public String getSuit() {
            return suit;
        }

        public void setSuit(String suit) {
            this.suit = suit;
        }
    }
}
