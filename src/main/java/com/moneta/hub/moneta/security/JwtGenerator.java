package com.moneta.hub.moneta.security;

import com.moneta.hub.moneta.model.entity.MonetaUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtGenerator {

    /**
     * Expiration time in milliseconds (5 minutes)
     */
    private static final long JWT_EXPIRATION = 300000L;

    /**
     * Key used for signing JWT token
     */
    private static final String JWT_KEY =
            "MSXUB340VHSM8W0B7VS2ELZPWL3842N7EC2GDZB0EP0UHD0949Q0Q" +
            "YLP3CGJ6T6DUJ7XP3ZFS7NFSXAPKU4JDXZA3QR2NHURGR243SC25NE" +
            "BY7X41S33PHPV7FL728W1ZBPZE14E82K35CKDYX5CFRS9QBMS0V8WCH" +
            "1HMM18PSYFFQPX3LLWZGC8XC7V3D7GFQ6X5KPFU7HQAG67MPR1K20NSZ" +
            "80ZTB9T1U580KN4WMHQXEGKTY79S54URWXS7PKJJ3F4K0XYV1D2HDKYZD" +
            "RN1KZL4EEDJP9CLHVJNBNM8HQ0RTPUQ0FPLGJP2S0DXVK8UCFYK92DJLUJ" +
            "Z1DQSBZNDT7AED9Z0LJLNAVW1T7S69KPRPWVJF3BBFLTAJ7XQ5E9J7GMS7E" +
            "6B29G2CRB2PVVTLDVQG26UFCBZG3ZL069PZJW04CFQS59SD47WRBKRZVR76F" +
            "9PHBPHGVSVPKPEG8F2Y8XGT75A2ARCCTNUQ4KWYDEYAFUSGMFTK8QX1KH1UY";

    @Value("${spring.application.name}")
    private String applicationName;

    public String generateToken(Authentication authentication, MonetaUser user) {
        String username = authentication.getName();
        Date issueDate = new Date();
        Date expireDate = new Date(issueDate.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                   .issuer(applicationName)
                   .subject(username)
                   .claim("roles", user.getRoles().stream().map(roles -> roles.getName().name()).toList())
                   .issuedAt(issueDate)
                   .expiration(expireDate)
                   .signWith(getSigningKey())
                   .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                            .verifyWith(getSigningKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        Claims claims = Jwts.parser()
                            .verifyWith(getSigningKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
        try {
            return notExpired(claims.getIssuedAt(), claims.getExpiration());
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT is expired or invalid.");
        }
    }

    private boolean notExpired(Date issuedAt, Date expireAt) {
        return issuedAt.before(expireAt);
    }
}
