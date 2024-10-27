package com.smu.csd.pokerivals.user.repository;

import com.smu.csd.pokerivals.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface UserRepository extends JpaRepository<User,String> {
    Optional<User> findOneByGoogleSub(String googleSub);

    @Modifying
    @Transactional
    void deleteByGoogleSub(String googleSub);
}
