package com.smu.csd.pokerivals.entity;

import com.smu.csd.pokerivals.user.entity.Admin;
import com.smu.csd.pokerivals.user.entity.Clan;
import com.smu.csd.pokerivals.user.entity.Player;
import com.smu.csd.pokerivals.user.entity.User;
import com.smu.csd.pokerivals.user.repository.AdminRepository;
import com.smu.csd.pokerivals.user.repository.PlayerPagingRepository;
import com.smu.csd.pokerivals.user.repository.PlayerRepository;
import com.smu.csd.pokerivals.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties ={
//    "spring.datasource.url=jdbc:mysql://localhost:3306/test",
//    "spring.jpa.hibernate.ddl-auto=update"
})
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserEntitiesTest {
    @Autowired
    private TestEntityManager testEM;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testInheritance(){
        String u1_uname = "marco";
        String u2_uname = "joshua";
        User u1 = new Player(u1_uname, "kuokkuok");
        User u2 = new Admin(u2_uname, "apple fall");

        testEM.persist(u1);
        testEM.persist(u2);

        User u1_found = testEM.find(User.class, u1_uname);
        User u2_found = testEM.find(User.class, u2_uname);

        assertEquals(u1_found.getDescription(), u1.getDescription());
        assertEquals(u2_found.getDescription(), u2.getDescription());
        
        if (u1_found instanceof Player p){
            assertEquals(p.getPoints(), 800.0);
        } else {
            assertEquals("apple", "banana");
        }
                
        if (u2_found instanceof Admin a){
           assertNull(a.getActiveSince());
        } else {
            assertEquals("apple", "banana");
            // TODO: find better way to handle this
        }

    }

    @Test
    public void testAdminInvite(){
        String u1_uname = "marco";
        String u2_uname = "apple";
        String u3_uname = "tohkit";
        Admin u1 = new Admin(u1_uname, "kuokkuok");
        Admin u2 = new Admin(u2_uname, "apple fall");
        Admin u3 = new Admin(u3_uname, "tohkittohkit");

        u1.addInvitee(u3);
        u1.addInvitee(u2);

        testEM.persist(u1);
        testEM.persist(u2);
        testEM.persist(u3);

        u1 = null;
        u2 = null;
        u3 = null;

        Admin u1_found = testEM.find(Admin.class, u1_uname);
        Admin u2_found = testEM.find(Admin.class, u2_uname);
        Admin u3_found = testEM.find(Admin.class, u3_uname);

        // getWhoAmI u1
        assertEquals(u1_found.getInvitees().size(), 2);
        Set<String> usernames = new HashSet<>();
        usernames.add(u3_uname); usernames.add(u2_uname);
        for( Admin a: u1_found.getInvitees()){
            if (usernames.contains(a.getUsername())){
                usernames.remove(a.getUsername());
            }
        }
        assertEquals(usernames.size(), 0);

        // getWhoAmI u2
        assertEquals(u2_found.getInvitedBy().getUsername(), u1_uname);

        // getWhoAmI u3
        assertEquals(u3_found.getInvitedBy().getUsername(), u1_uname);
    }

    @Test
    public void testPlayerFriend(){
        String u1_uname = "marco";
        String u2_uname = "apple";
        String u3_uname = "tohkit";
        String u4_uname = "sathwik";
        Player u1 = new Player(u1_uname, "kuokkuok");
        Player u2 = new Player(u2_uname, "apple fall");
        Player u3 = new Player(u3_uname, "tohkittohkit");
        Player u4 = new Player(u4_uname, "chiluveru");

        u1.addFriend(u3);
        u1.addFriend(u2);
        u3.addFriend(u2);
        u4.addFriend(u1);

        testEM.persist(u1);
        testEM.persist(u2);
        testEM.persist(u3);
        testEM.persist(u4);

        // u1 = null;
        // u2 = null;
        // u3 = null;
        // u4  = null;

        Player u1_found = testEM.find(Player.class, u1_uname);
        Player u2_found = testEM.find(Player.class, u2_uname);
        Player u3_found = testEM.find(Player.class, u3_uname);
        Player u4_found = testEM.find(Player.class, u4_uname);

        // getWhoAmI u1
        assertEquals(u1_found.getFriendsWith().size(), u1_found.getBefriendedBy().size() );
        assertEquals(u1_found.getFriendsWith().size(), 3 );

        // getWhoAmI u2
        assertEquals(u2_found.getFriendsWith().size(), u2_found.getBefriendedBy().size() );
        assertEquals(u2_found.getFriendsWith().size(), 2 );


        // getWhoAmI u3
        assertEquals(u3_found.getFriendsWith().size(), u3_found.getBefriendedBy().size() );
        assertEquals(u3_found.getFriendsWith().size(), 2 );

        // getWhoAmI u4
        assertEquals(u4_found.getFriendsWith().size(), u4_found.getBefriendedBy().size() );
        assertEquals(u4_found.getFriendsWith().size(), 1 );
    }

    @Test
    public void testClan(){
        String u1_uname = "marco";
        String u2_uname = "apple";

        String clan_name = "clan";

        Player u1 = new Player(u1_uname, "kuokkuok");
        Player u2 = new Player(u2_uname, "apple fall");
        Clan c = new Clan(clan_name);

        testEM.persist(u1);
        testEM.persist(u2);
        testEM.persist(c);

        u1 = testEM.find(Player.class, u1_uname);
        u2 = testEM.find(Player.class, u2_uname);
        c = testEM.find(Clan.class, clan_name);

        u1.addToClan(c);
        u2.addToClan(c);
        u1.addToClan(c);

        testEM.persist(u1);
        testEM.persist(u2);
        testEM.persist(c);

        u1 = testEM.find(Player.class, u1_uname);
        u2 = testEM.find(Player.class, u2_uname);
        c = testEM.find(Clan.class, clan_name);

        assertEquals(2, c.getMembers().size()); // how do i lock it down
        assertEquals(c, u1.getClan());
        assertEquals(c, u2.getClan());

    }

    @Test
    public void testChangeClan(){
        int noOfPlayers = 1;
        int noOfClans = 2;

        // create new ones

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < noOfPlayers; i++){
            String username = String.format("Player-%d",i);
            players.add(new Player(username, username));
        }

        List<Clan> clans = new ArrayList<>();
        for (int i = 0; i < noOfClans; i++){
            String clanName = String.format("Clan-%d",i);
            clans.add(new Clan(clanName));
        }

        // persist

        players.forEach( (player) -> testEM.persist(player));
        clans.forEach(clan -> testEM.persist(clan));

        // end- persist

        // clear and get it back
        players = new ArrayList<>();
        clans= new ArrayList<>();

        for (int i = 0; i < noOfPlayers; i++){
            String username = String.format("Player-%d",i);
            players.add(testEM.find(Player.class, username));
        }

        for (int i = 0; i < noOfClans; i++){
            String clanName = String.format("Clan-%d",i);
            clans.add(testEM.find(Clan.class, clanName));
        }

        // - end get it back

        players.get(0).addToClan(clans.get(0));
        
        // persist

        players.forEach( (player) -> testEM.persist(player));
        clans.forEach(clan -> testEM.persist(clan));

        // end- persist

        // clear and get it back
        players = new ArrayList<>();
        clans= new ArrayList<>();

        for (int i = 0; i < noOfPlayers; i++){
            String username = String.format("Player-%d",i);
            players.add(testEM.find(Player.class, username));
        }

        for (int i = 0; i < noOfClans; i++){
            String clanName = String.format("Clan-%d",i);
            clans.add(testEM.find(Clan.class, clanName));
        }

        // - end get it back

        assertEquals("Clan-0", players.get(0).getClan().getName());

        players.get(0).addToClan(clans.get(1));

        // persist

        players.forEach( (player) -> testEM.persist(player));
        clans.forEach(clan -> testEM.persist(clan));

        // end- persist

        // clear and get it back
        players = new ArrayList<>();
        clans= new ArrayList<>();

        for (int i = 0; i < noOfPlayers; i++){
            String username = String.format("Player-%d",i);
            players.add(testEM.find(Player.class, username));
        }

        for (int i = 0; i < noOfClans; i++){
            String clanName = String.format("Clan-%d",i);
            clans.add(testEM.find(Clan.class, clanName));
        }

        // - end get it back

        assertEquals("Clan-1", players.get(0).getClan().getName());

    }

    @Test
    public void testRepository(){
        Admin u1 = new Admin("joshua", "123");
        Player u2 = new Player("marco", "456");
        userRepository.save(u1);
        userRepository.save(u2);
        
        Optional<User> u1_get = userRepository.findOneByGoogleSub("123");
        Optional<User> u2_get = userRepository.findOneByGoogleSub("456");

        assertEquals(true, u1_get.isPresent());
        assertEquals(true, u2_get.isPresent());

        assertEquals(true, u1_get.get() instanceof Admin);
        assertEquals(true, u2_get.get() instanceof Player);
    }

    @Autowired
    private AdminRepository adminRepository;

    @Test
    public void testQuery(){
        for (int i = 0; i<3;i++){
            adminRepository.save(new Admin("user-"+i, "google"+i));
        }



        for (int i = 1; i<3;i++){
            Admin u1 = adminRepository.findById("user-0").orElseThrow();
            Admin a = adminRepository.findById("user-" + i).orElseThrow();
            u1.addInvitee(a);
           adminRepository.save(u1);
        }

        Set<Admin> result = new HashSet<>();
        result.add(adminRepository.findById("user-1").orElseThrow());
        result.add(adminRepository.findById("user-2").orElseThrow());

        assertEquals(result,new HashSet<>(adminRepository.findAdminsInvitedBy("user-0")));


    }

    @Autowired
    private PlayerPagingRepository playerPagingRepository;
    @Autowired
    private PlayerRepository playerRepository;


    @Test
    public void testPaging(){
        String[] fruits = new String[]{
                "banana", "mango", "apple", "kiwi"
        };

        for (String s: fruits){
            for (int i = 0; i<100;i++){
                playerRepository.save(new Player(s+"-"+i, "google"+s+i));
            }
        }

        Player player = playerRepository.findById("banana-0").orElseThrow();
        for (int i = 0; i<100;i++){
            player.addFriend(playerRepository.findById("kiwi-"+i).orElseThrow());
            playerRepository.save(player);
        }
        player = playerRepository.findById("banana-0").orElseThrow();
        assertEquals(100, player.getNoOfFriends());

        for (int i = 0; i<100;i++){
            Player p = playerRepository.findById("kiwi-"+i).orElseThrow();
            assertEquals(1,p.getNoOfFriends());
        }


        Set<Integer> collected = new HashSet<>();

        int recordsPerPage = 9;
        int recordsSeen =0;
        for (int pageNo =0; ; pageNo++){
            List<Player> friends = playerPagingRepository.findFriendsOfPlayer("banana-0", PageRequest.of(pageNo, recordsPerPage));

            if (recordsSeen == 100){
                assertEquals(0, friends.size());
                break;
            }

            recordsSeen += friends.size();
            assertTrue(recordsPerPage >= friends.size());

            for (Player p: friends){
                collected.add(Integer.parseInt(p.getUsername().split("-")[1]));
            }


        }


        assertEquals(100, collected.size());

        // getWhoAmI remove
        for (int i = 0; i<100;i++){
            Player p1 = playerRepository.findById("kiwi-"+i).orElseThrow();
            Player p2 = playerRepository.findById("banana-0").orElseThrow();
            p1.removeFriend(p2);
        }

        Player p2 = playerRepository.findById("banana-0").orElseThrow();
        assertEquals(0,p2.getNoOfFriends());
    }

    @Test
    public void testSaveSameId(){
        var copy1 = new Player("joshua", "222");
        playerRepository.save(copy1);
        final var copy2 = new Player("joshua", "222");
        assertThrows(DuplicateKeyException.class,()->{
            playerRepository.save(copy2);
        });
    }

}
