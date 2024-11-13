package com.smu.csd.pokerivals.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class Player extends User {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private double points = 800.0;

    public Player(String username, String googleId) {
        super(username, googleId);
    }
    public Player (String username, String googleId , double points){
        super(username , googleId);
        this.points = points;
    }
    @Getter
    @ManyToMany
    @JsonIgnore
    private Set<Player> befriendedBy = new HashSet<>();

    @Getter
    @ManyToMany(mappedBy = "befriendedBy")
    @JsonIgnore
    private Set<Player> friendsWith = new HashSet<>();

    public void addFriend(Player p){
        this.befriendedBy.add(p);
        p.friendsWith.add(this);

        p.befriendedBy.add(this);
        this.friendsWith.add(p);
    }

    public void removeFriend(Player p){
        this.befriendedBy.remove(p);
        p.friendsWith.remove(this);

        this.friendsWith.remove(p);
        p.befriendedBy.remove(this);
    }

    @ManyToOne
    @JoinColumn(name = "fk_clan")
    @Getter
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Clan clan;

    public void addToClan(Clan c){
        if (this.clan != null ) {
            throw new IllegalArgumentException("user is already associated to a clan");
        }
        this.clan = c;
        c.getMembers().add(this);
    }


    public int getNoOfFriends(){
        if (befriendedBy.size() != friendsWith.size()){
            throw new IllegalStateException();
        }
        return befriendedBy.size();
    }

    private static final int AdjustmentRating = 20;

    /**
     * Change the elo of only THIS PLAYER
     * @param winProbability probability of THIS PLAYER winning
     * @param win whether THIS PLAYER won
     */
    public void changeElo(double winProbability, boolean win){
        this.points+= AdjustmentRating * ((win ? 1: 0) - winProbability );
    }
}
