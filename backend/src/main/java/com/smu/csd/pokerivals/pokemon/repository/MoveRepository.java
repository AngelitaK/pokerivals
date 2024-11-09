package com.smu.csd.pokerivals.pokemon.repository;

import com.smu.csd.pokerivals.pokemon.entity.Move;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveRepository extends JpaRepository<Move, String> {

}
