package com.example.dto;

import lombok.Data;

@Data
public class ReassignPlacementRequest {
    private BrokerTeamDto broker_team;
    private BrokerUserDto broker_user;

    @Data
    public static class BrokerTeamDto {
        private String team_id;
    }

    @Data
    public static class BrokerUserDto {
        private String user_email;
    }
} 