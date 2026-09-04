package com.discover.app.identity.repository;

import com.discover.app.identity.domain.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = "userRoles")
    Optional<User> findByUsername(String username);
    @EntityGraph(attributePaths = "userRoles")
    Page<User> findAll(Pageable pageable);
    @EntityGraph(attributePaths = "userRoles")
    @Query("select u from User u where u.id = :id")
    Optional<User> findDetailedById(Long id);
}
