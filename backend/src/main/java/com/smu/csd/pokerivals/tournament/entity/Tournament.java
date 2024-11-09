package com.smu.csd.pokerivals.tournament.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smu.csd.pokerivals.user.entity.Admin;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Size(max=100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "admin_creator")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonIgnore
    private Admin admin;

    @JsonGetter
    public String getAdminUsername(){
        return admin.getUsername();
    }

    public void setAdmin(Admin admin){
        if(this.admin != null){
            throw new IllegalArgumentException("Unable to change admin");
        }
        this.admin=admin;
    }

    @Min(value = 0, message = "The value must be positive")
    private int maxTeamCapacity = 32;

    public void setMaxTeamCapacity(int maxTeamCapacity){
        if (this.maxTeamCapacity < maxTeamCapacity){
            this.maxTeamCapacity = maxTeamCapacity;
        } else {
            throw new IllegalArgumentException("Cannot reduce max team capacity");
        }
    }

    @Embedded
    @NotNull
    private EloLimit eloLimit;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class EloLimit {
        private double minElo;
        private double maxElo;

        public boolean checkElo(double points) {
            return points >= minElo && points <= maxElo;
        }

        public EloLimit(double min, double max) {
            this.maxElo = max;
            this.minElo = min;
        }

        @AssertTrue
        private boolean isValid() {
            return minElo <= maxElo;
        }
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationPeriod {
        private ZonedDateTime registrationBegin;
        private ZonedDateTime registrationEnd;

        @AssertTrue
        private boolean isValid() {
            return registrationBegin.isBefore(registrationEnd);
        }

        public boolean isWithinPeriod(ZonedDateTime time){
            return time.isAfter(registrationBegin) && time.isBefore(registrationEnd);
        }

        public boolean isBefore(ZonedDateTime time){
            return time.isBefore(registrationBegin);
        }

        public boolean isAfter(ZonedDateTime time){
            return time.isAfter(registrationEnd);
        }
    }

    @Embedded
    @NotNull
    private RegistrationPeriod registrationPeriod;


    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstimatedTournamentPeriod {
        private ZonedDateTime tournamentBegin;
        private ZonedDateTime tournamentEnd;

        @AssertTrue
        private boolean isValid() {
            return tournamentBegin.isBefore(tournamentEnd);
        }
    }

    @Embedded
    @NotNull
    private EstimatedTournamentPeriod estimatedTournamentPeriod ;


    /**
     * For testing purposes
     * Sets the registration period to between now and 2 hours later
     * @param name name of tournament
     * @param admin admin
     */
    public Tournament(String name, Admin admin) {
        this.name = name;
        this.admin = admin;
        this.eloLimit = new EloLimit(0, 5000);
        this.registrationPeriod = new RegistrationPeriod(
                ZonedDateTime.now(),
                ZonedDateTime.now().plusHours(2)
        );
        this.estimatedTournamentPeriod = new EstimatedTournamentPeriod(
                ZonedDateTime.now().plusHours(3),
                ZonedDateTime.now().plusHours(4)
        );
    }

    public Tournament (String name, Admin admin , EloLimit eloLimit, RegistrationPeriod registrationPeriod){
        this(name,admin);
        this.eloLimit = eloLimit;
        this.registrationPeriod = registrationPeriod;
    }

    @OneToMany(mappedBy = "tournament")
    @JsonIgnore
    private Set<Team> teams = new HashSet<>();


    @Column(name = "createdAt", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    /**
     * Team can only be added within registration period
     * @param team team joining
     * @param today today's date (this is to facilitate mocking)
     */
    public void addTeam(Team team , ZonedDateTime today){
        if (registrationPeriod.isWithinPeriod(today) && this.maxTeamCapacity > teams.size()){
            this.teams.add(team);
            team.setTournament(this);
        } else {
            throw new IllegalArgumentException("Tournament is full!");
        }
    };

    /**
     *
     * @param team remove team if in tournament
     */
    public void removeTeam(Team team ){
        if (!teams.contains(team)){
            throw new IllegalArgumentException("Team is not in tournament");
        }
        this.teams.remove(team);
        team.setTournament(null);
    };

}