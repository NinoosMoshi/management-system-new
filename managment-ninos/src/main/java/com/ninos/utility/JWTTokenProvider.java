package com.ninos.utility;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;  // static for .sign(HMAC512(secret.getBytes())); instead of .sign(Algorithm.HMAC512(secret.getBytes()));// we create static because we don't want to use class name in each claims (SecurityConstant.COMPANY_NAME) and (SecurityConstant.for all the constants)
import static com.ninos.constant.SecurityConstant.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.ninos.domain.UserPrincipal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.*;

@Component
public class JWTTokenProvider {
	
	 
	@Value("${jwt.secret}")   // we use @Value to access application.yml and use out custom name 
	 private String secret;

	    public String generateJwtToken(UserPrincipal userPrincipal) {
	        String[] claims = getClaimsFromUser(userPrincipal);
	        return JWT.create().withIssuer(COMPANY_NAME).withAudience(ANKEDO_ADMINISTRATION)
	                .withIssuedAt(new Date()).withSubject(userPrincipal.getUsername())
	                .withArrayClaim(AUTHORITIES, claims).withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
	                .sign(HMAC512(secret.getBytes()));
	    }
	    
	    
	    
	    public List<GrantedAuthority> getAuthorities(String token) {
	        String[] claims = getClaimsFromToken(token);
	        return stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	    }
	    
	    
	    public Authentication getAuthentication(String username, List<GrantedAuthority> authorities, HttpServletRequest request) {
	        UsernamePasswordAuthenticationToken userPasswordAuthToken = new
	                UsernamePasswordAuthenticationToken(username, null, authorities);
	        userPasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	        return userPasswordAuthToken;
	    }
	    
	    
	    public boolean isTokenValid(String username, String token) {
	        JWTVerifier verifier = getJWTVerifier();
	        return StringUtils.isNotEmpty(username) && !isTokenExpired(verifier, token);
	    }
	    
	    
	    private boolean isTokenExpired(JWTVerifier verifier, String token) {
	        Date expiration = verifier.verify(token).getExpiresAt();
	        return expiration.before(new Date());
	    }
	    
	    
	    public String getSubject(String token) {
	        JWTVerifier verifier = getJWTVerifier();
	        return verifier.verify(token).getSubject();
	    }
	    
	    
	    private String[] getClaimsFromToken(String token) {
	        JWTVerifier verifier = getJWTVerifier();
	        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
	    }
	    
	    
	    private JWTVerifier getJWTVerifier() {
	        JWTVerifier verifier;
	        try {
	            Algorithm algorithm = HMAC512(secret);
	            verifier = JWT.require(algorithm).withIssuer(COMPANY_NAME).build();
	        }catch (JWTVerificationException exception) {
	            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
	        }
	        return verifier;
	    }
	    
	    
	    
	    

		private String[] getClaimsFromUser(UserPrincipal user) {
			List<String> authorities = new ArrayList<>();
			for (GrantedAuthority grantedAuthority : user.getAuthorities()) {
				authorities.add(grantedAuthority.getAuthority());
			}
			
			return authorities.toArray(new String[0]);
		}

}
