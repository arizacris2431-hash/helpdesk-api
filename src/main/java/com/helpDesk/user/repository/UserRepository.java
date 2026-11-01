package com.helpDesk.user.repository;

import com.helpDesk.user.entity.Role;
import com.helpDesk.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail (String email);
    boolean existsByEmail (String email);
    List<User> findByActiveTrue ();
    List<User> findByRole (Role role);
}
