package com.discover.app.identity.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints =
    @UniqueConstraint(name = "uk_users_username", columnNames = "username"))
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String username;
    @Column(nullable = false, length = 255)
    private String password;
    @Column(nullable = false)
    private boolean enabled = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    protected User() {}
    public User(String username, String password, boolean enabled) {
        this.username = username; this.password = password; this.enabled = enabled;
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isEnabled() { return enabled; }
    public Set<UserRole> getUserRoles() { return userRoles; }
    public void addRole(Role role) { userRoles.add(new UserRole(this, role)); }
}
