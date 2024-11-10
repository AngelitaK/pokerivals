package com.smu.csd.pokerivals.entity;


import com.smu.csd.pokerivals.pokemon.entity.Ability;
import com.smu.csd.pokerivals.pokemon.entity.Move;
import com.smu.csd.pokerivals.pokemon.entity.Pokemon;
import com.smu.csd.pokerivals.pokemon.repository.PokemonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
//        "spring.datasource.url=jdbc:mysql://localhost:3306/test",
//        "spring.jpa.hibernate.ddl-auto=update",
//        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
//        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
})
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class PokemonEntityTest {
    @Autowired
    private TestEntityManager testEM;

    @Autowired
    private PokemonRepository pokemonRepository;


    @Test
    public void testConstructor(){
        int Id = 1;
        String type = "Grass";
        String p1Name = "bulbasaur";
        String abilityName1 = "Chlorophyll";
        String abilityName2 = "Poison Touch";
        Ability ability1 = new Ability(abilityName1);
        Ability ability2 = new Ability(abilityName2);
        testEM.persist(ability1);
        testEM.persist(ability2);

        int hp = 40;
        int attack = 90;
        int defense = 85;
        int specialAttack = 60;
        int specialDefense = 50;
        int speed = 115;
        Pokemon.Stats p1Stat = new Pokemon.Stats(hp, attack , defense , specialAttack , specialDefense , speed);

        String move1Name = "vine whip";
        String move2Name = "giga drain";
        Move m1 = new Move(move1Name);
        Move m2 = new Move(move2Name);
        testEM.persist(m1);
        testEM.persist(m2);

        Pokemon pokemon = new Pokemon(Id , p1Name , type , null , p1Stat);
        testEM.persist(pokemon);
        pokemon.addMove(m1);
        pokemon.addMove(m2);

        Pokemon pokemon1 = testEM.find(Pokemon.class , Id);
        assertNotNull(pokemon1);
        assertNotNull(pokemon1.getId());

        Set<Move> p1MoveSet = pokemon1.getMoves();
        assertEquals(2, p1MoveSet.size());
        for (Move m : p1MoveSet){
            Set <Pokemon> learns = m.getLearntBy();
            for (Pokemon p : learns){
                assertEquals(p , pokemon1);
            }
        }


    }


    @Test
    public void testRemoving(){
        int Id = 1;
        String type = "Grass";
        String p1Name = "bulbasaur";
        String abilityName1 = "Chlorophyll";
        String abilityName2 = "Poison Touch";
        Ability ability1 = new Ability(abilityName1);
        Ability ability2 = new Ability(abilityName2);
        testEM.persist(ability1);
        testEM.persist(ability2);

        int hp = 40;
        int attack = 90;
        int defense = 85;
        int specialAttack = 60;
        int specialDefense = 50;
        int speed = 115;
        Pokemon.Stats p1Stat = new Pokemon.Stats(hp, attack , defense , specialAttack , specialDefense , speed);

        String move1Name = "vine whip";
        String move2Name = "giga drain";
        Move m1 = new Move(move1Name);
        Move m2 = new Move(move2Name);
        testEM.persist(m1);
        testEM.persist(m2);

        Pokemon pokemon1 = new Pokemon(Id , p1Name , type , null , p1Stat);
        pokemon1.addAbility(ability1);
        pokemon1.addAbility(ability2);

        pokemon1.addMove(m1);
        pokemon1.addMove(m2);
        testEM.persist(ability1);
        testEM.persist(ability2);
        testEM.persist(m1);
        testEM.persist(m2);
        testEM.persist(pokemon1);
        Pokemon pokemon1Found = testEM.find(Pokemon.class , 1);

        assertTrue(pokemon1Found.getAbilities().contains(ability1));
        assertTrue(pokemon1Found.getAbilities().contains(ability2));

        for (Ability a : pokemon1Found.getAbilities()){
            assertTrue(a.getPossessedBy().contains(pokemon1Found));
        }


        pokemon1.removeAbility(ability1);
        pokemon1.removeMove(m1);

        pokemon1Found = testEM.find(Pokemon.class , 1);

        assertFalse(pokemon1Found.getAbilities().contains(ability1));
        assertFalse(pokemon1Found.getMoves().contains(m1));
        assertFalse(m1.getLearntBy().contains(pokemon1Found));

    }
}
