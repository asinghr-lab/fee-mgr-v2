package com.discover.app.identity.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId @JoinColumn(name = "id")
    private User user;
    @Column(length = 150)
    private String displayName;

    protected UserProfile() {}
    public UserProfile(User user, String displayName) { this.user = user; this.displayName = displayName; }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
