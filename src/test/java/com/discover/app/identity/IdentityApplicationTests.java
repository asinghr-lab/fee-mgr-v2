package com.discover.app.identity;

import com.discover.app.identity.domain.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class IdentityApplicationTests {
    @Test void userCanReceiveRoles() {
        User user = new User("test", "encoded", true);
        user.addRole(Role.STAFF);
        assertThat(user.getUserRoles()).extracting(UserRole::getRole).containsExactly(Role.STAFF);
    }
}
