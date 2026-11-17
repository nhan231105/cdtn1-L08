package com.mekongmobile.smartcrm.auth;

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    @Test
    void accessTokenContainsOnlyUserIdRoleAndExpiryClaims() throws Exception {
        UserRepository users = mock(UserRepository.class);
        UserIdentity identity = new UserIdentity(42L, "marketing", "MARKETING", null);
        String hash = new BCryptPasswordEncoder().encode("test-password-123");
        when(users.findEnabled("marketing")).thenReturn(Optional.of(new UserRepository.UserAccount(identity, hash)));
        when(users.findEnabled(42L)).thenReturn(Optional.of(new UserRepository.UserAccount(identity, hash)));
        AuthService auth = new AuthService(users, "test-jwt-secret-0123456789abcdef");

        String token = (String) auth.login("marketing", "test-password-123").get("access_token");
        var jwt = SignedJWT.parse(token);
        var claims = jwt.getJWTClaimsSet();

        assertEquals("42", claims.getSubject());
        assertEquals("MARKETING", claims.getStringClaim("role"));
        assertEquals(Set.of("sub", "exp", "role"), claims.getClaims().keySet());
        long now = Instant.now().getEpochSecond();
        long expires = claims.getExpirationTime().toInstant().getEpochSecond();
        assertTrue(expires > now);
        assertTrue(expires <= now + 3600);
        assertEquals(identity, auth.require("Bearer " + token, "MARKETING"));
    }
}
