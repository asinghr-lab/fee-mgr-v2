package com.discover.app.identity.dto;

import java.util.Set;

public final class IdentityDtos {
    private IdentityDtos() {}
    public record CurrentUserResponse(String username, String displayName, Set<String> roles) {}
}
