package com.mekongmobile.smartcrm.auth;

public record UserIdentity(Long userId, String username, String role, Long centerId) {
}
