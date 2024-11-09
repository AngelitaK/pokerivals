package com.smu.csd.pokerivals.tournament.entity;

import com.smu.csd.pokerivals.user.entity.Admin;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Getter
@Setter
public class Tournament {
    public Tournament() {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Override
    public boolean equals(Object other){
        if (other instanceof Tournament o){
            return id.equals(o.getId());
        }
        return false;
    }
    private String name;
    private boolean invitationOnly = false;
    private boolean isOpen = true;
    @ManyToOne
    @JoinColumn(name = "admin_creator")
    private Admin admin;

    private int maxTeamCapacity = 32;

    @Embedded
    private EloLimit eloLimit;

    @Embeddable
    @Getter
    @Setter
    public static class EloLimit {

        public EloLimit(){}

        private double minElo;
        private double maxElo;

        public boolean checkElo(double points) {
            return points >= minElo && points <= maxElo;
        }

        public EloLimit(double min, double max) {
            this.maxElo = max;
            this.minElo = min;
        }
    }

    public Tournament(String name, Admin admin) {
        this.name = name;
        this.admin = admin;
        this.eloLimit = new EloLimit(0, 5000);
    }

    public Tournament (String name, Admin admin , EloLimit eloLimit){
        this.name = name;
        this.admin = admin;
        this.eloLimit = eloLimit;
    }

    @OneToMany(mappedBy = "tournament")
    private Set<Team> teams = new HashSet<>();

    public void addTeam(Team team){
        teams.add(team);
    }

    public void removeTeam(Team team){
        teams.remove(team);
    }
    public void closeTournament(){
        isOpen = false;
    }
    public void openTournament(){
        isOpen = true;
    }
}