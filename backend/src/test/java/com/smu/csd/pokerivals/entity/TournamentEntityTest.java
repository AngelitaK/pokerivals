package com.smu.csd.pokerivals.entity;

import com.smu.csd.pokerivals.tournament.entity.Team;
import com.smu.csd.pokerivals.tournament.entity.Tournament;
import com.smu.csd.pokerivals.tournament.entity.Tournament.EloLimit;
import com.smu.csd.pokerivals.tournament.repository.TeamRepository;
import com.smu.csd.pokerivals.tournament.repository.TournamentRepository;
import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.repository.AdminRepository;
import com.smu.csd.pokerivals.user.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/test",
        "spring.jpa.hibernate.ddl-auto=update"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TournamentEntityTest {
    @Autowired
    private TestEntityManager testEM;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    public void testConstructor(){
        String adminName = "yourMother";
        Admin u1 = new Admin(adminName , "motherKnowsBest");
        testEM.persist(u1);

        String t1Name = "worlds";
        Tournament t1 = new Tournament(t1Name , u1);

        tournamentRepository.save(t1);
        Tournament t1Found = testEM.find(Tournament.class , t1.getId());

        assertNotNull(t1.getId());
        assertEquals(t1.getId() , t1Found.getId());


        EloLimit t1EloLimit = t1.getEloLimit();
        assertEquals(5000, t1EloLimit.getMaxElo());
        assertEquals(0, t1EloLimit.getMinElo());
    }

    @Test
    public void testUniqueId(){

        String adminName = "test";
        Admin u1 = new Admin(adminName, adminName);
        testEM.persist(u1);

        String t1Name = "worlds";
        String t2Name = "ti24";

        Tournament t1 = new Tournament(t1Name, u1);
        Tournament t2 = new Tournament(t2Name,u1);

        tournamentRepository.save(t1);
        tournamentRepository.save(t2);

        assertNotEquals(t1.getId() , t2.getId());
        assertEquals(t1.getAdmin(), t2.getAdmin());
    }

    @Test
    public void testEloLimits(){
        String adminName = "test";
        Admin u1 = new Admin(adminName, adminName);
        testEM.persist(u1);

        EloLimit e1 = new EloLimit(0, 500);

        String t1Name = "test";
        String t2Name = "test";

        Tournament t1 = new Tournament(t1Name, u1, e1);
        Tournament t2 = new Tournament(t2Name, u1);

        tournamentRepository.save(t1);
        tournamentRepository.save(t2);

        assertNotEquals(t1, t2);
        assertNotEquals(t1.getId(), t2.getId());

        assertEquals(t1.getEloLimit().getMaxElo(), e1.getMaxElo());
        assertEquals(t1.getEloLimit().getMinElo(), e1.getMinElo());
    }

    @Test
    public void testSignUp(){
        String adminName = "yourMother";
        Admin u1 = new Admin(adminName , "motherKnowsBest");
        testEM.persist(u1);

        String t1Name = "worlds";
        Tournament t1 = new Tournament(t1Name , u1);

        tournamentRepository.save(t1);

        String playerName = "louis teo";
        Player p1 = new Player(playerName, playerName);

        testEM.persist(p1);
        Team.TeamId teamId1 = new Team.TeamId(p1 , t1);
        Team team1 = new Team(p1, t1 , teamId1);
        Team.TeamId t1Id= team1.getTeamId();

        teamRepository.save(team1);

        assertNotNull(team1);
        assertNotNull(team1.getTeamId());

        Team team1Found = testEM.find(Team.class , t1Id);
        assertEquals(team1Found , team1);
    }


}
