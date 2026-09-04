package com.discover.app.identity.repository;

import com.discover.app.identity.domain.User;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = "userRoles")
    Optional<User> findByUsername(String username);
}
