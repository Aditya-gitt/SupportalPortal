package com.example.demo.jwt;

import com.auth0.jwt.JWT;
import static com.example.demo.constant.SecurityConstant.*;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.demo.domain.MyUserDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/***
 * This class is just like other service classes.
 * It provides service for JWT.
 */
@Component
public class JWTTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    MyUserDetails myUserDetails;

 //Generating token at the time of logging in.
    public String generateJwtToken(MyUserDetails myUserDetails) {
        this.myUserDetails = myUserDetails;
        String[] claims = this.getClaimsFromUser();

        return JWT.create().withIssuer(AKI_LLC).withAudience(AKI_ADMINISTRATION)
                .withIssuedAt(new Date()).withSubject(myUserDetails.getUsername())
                .withArrayClaim(AUTHORITIES, claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes()));
    }
    //Getting authorities from user at the time of logging in.
    private String[] getClaimsFromUser() {
        List<String> authorities = new ArrayList<>();

        for(GrantedAuthority grantedAuthority : myUserDetails.getAuthorities()) {
            authorities.add(grantedAuthority.getAuthority());
        }
        return authorities.toArray(new String[0]);
    }

 //Getting the authorities from the token provided by client.
    public List<GrantedAuthority> getAuthorities(String token) {
        String[] claims = this.getClaimsFromToken(token);
        return Arrays.stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = this.getJWTVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

 //Creating JWT verifier
    private JWTVerifier getJWTVerifier() {
        JWTVerifier verifier;
        try {
            Algorithm algorithm = Algorithm.HMAC512(secret);
            verifier = JWT.require(algorithm).withIssuer(AKI_LLC).build();
        }
        catch (JWTVerificationException jwtVerificationException) {
            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }
        return verifier;
    }

 //This method is to just authenticate user.
 //It just sets the security context for the user & tells security that
 //hey security this user is authenticated just process his requests.
    public Authentication getAuthentication(String username,
                                            List<GrantedAuthority> authority,
                                            HttpServletRequest request) {
        UsernamePasswordAuthenticationToken userPassAuthToken =
                new UsernamePasswordAuthenticationToken(username, null, authority);
        userPassAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return userPassAuthToken;
    }

 //These methods are to validate the token, like if it is expired or username is valid.
    public boolean isTokenValid(String username, String token) {
        JWTVerifier verifier = this.getJWTVerifier();
        return StringUtils.isNotEmpty(username) && !this.isTokenExpired(verifier, token);
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

 //Get subject from token
    public String getSubject(String token) {
        JWTVerifier verifier = this.getJWTVerifier();
        return verifier.verify(token).getSubject();
    }
}
