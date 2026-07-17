package com.game.exposed.dto;

    public  class SeatAction {
        private int index;
        private String action;
        private String sessionId;
        private String name;

        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
