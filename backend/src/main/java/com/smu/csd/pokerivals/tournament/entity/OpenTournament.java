package com.smu.csd.pokerivals.tournament.entity;

import com.smu.csd.pokerivals.user.entity.Admin;
import jakarta.persistence.Entity;

@Entity
public class OpenTournament extends Tournament {
    public OpenTournament() {
    }

    public OpenTournament(String name, Admin admin) {
        super(name, admin);
    }

    public OpenTournament(String name, Admin admin, EloLimit eloLimit, RegistrationPeriod registrationPeriod) {
        super(name, admin, eloLimit, registrationPeriod);
    }
}
