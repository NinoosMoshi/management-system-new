package com.ninos.filter;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import com.ninos.utility.JWTTokenProvider;
import static com.ninos.constant.SecurityConstant.*;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {
	
	
	private JWTTokenProvider jwtTokenProvider;
	
	public JwtAuthorizationFilter(JWTTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}



	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if(request.getMethod().equalsIgnoreCase(OPTIONS_HTTP_METHOD)) {
			response.setStatus(OK.value());
		} else {
			String authorizationHeader = request.getHeader(AUTHORIZATION);
			if(authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
				filterChain.doFilter(request, response);
				return;
			}
			String token = authorizationHeader.substring(TOKEN_PREFIX.length());
            String username = jwtTokenProvider.getSubject(token);
            if (jwtTokenProvider.isTokenValid(username, token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<GrantedAuthority> authorities = jwtTokenProvider.getAuthorities(token);
                Authentication authentication = jwtTokenProvider.getAuthentication(username, authorities, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }
		}
		
		filterChain.doFilter(request, response);	
	}

}



/* this class will happen once for the request  ==>  public class JwtAuthorizationFilter extends OncePerRequestFilter.
* getMethod() => it's meaning (get,post,delete).
* SecurityContext is used to store the details of the currently authenticated user, also known as a principle.
*  So, if you have to get the username or any other user details, you need to get this SecurityContext first.
* SecurityContextHolder is a helper class, which provides access to the security context.
* options => is a request to collect information about a server that accepted(server conditions شروط السيرفر),
* and it's send before any request.
*
* if the request is options then return ok => if(request.getMethod().equalsIgnoreCase(OPTIONS_HTTP_METHOD)) {
			response.setStatus(OK.value());
		}
*
* else get header of the request => String authorizationHeader = request.getHeader(AUTHORIZATION);
*    if header is null and is not starting with word Bearer then stop the execution, note: TOKEN_PREFIX = "Bearer"
*    if(authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
				filterChain.doFilter(request, response);
				return;
			}
*
*    else take a header and remove "Bearer" keyword then put a header in token keyword =>
*    String token = authorizationHeader.substring(TOKEN_PREFIX.length());
*
*    get username from the token =>  String username = jwtTokenProvider.getSubject(token);
*
*    if username and token is valid and SecurityContext is null then
*        put username, authorities and request in SecurityContext by using helper class SecurityContextHolder.
*    if (jwtTokenProvider.isTokenValid(username, token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<GrantedAuthority> authorities = jwtTokenProvider.getAuthorities(token);
                Authentication authentication = jwtTokenProvider.getAuthentication(username, authorities, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
*
*    else clear SecurityContext =>  SecurityContextHolder.clearContext();
*/
























