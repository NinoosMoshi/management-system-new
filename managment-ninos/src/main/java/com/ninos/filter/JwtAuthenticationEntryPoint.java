package com.ninos.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninos.domain.HttpResponse;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpStatus.FORBIDDEN;     // from spring 
import static com.ninos.constant.SecurityConstant.FORBIDDEN_MESSAGE;   // from constant
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
			throws IOException {
		
		 HttpResponse httpResponse = new HttpResponse(FORBIDDEN.value(), FORBIDDEN, FORBIDDEN.getReasonPhrase().toUpperCase(), FORBIDDEN_MESSAGE);  // we make import static org.springframework.http.HttpStatus.FORBIDDEN; to ignore write HttpStatus.FORBIDDEN.value() , instead that we can write just FORBIDDEN.value()                                                   
	        response.setContentType(APPLICATION_JSON_VALUE);
	        response.setStatus(FORBIDDEN.value());
	        OutputStream outputStream = response.getOutputStream();
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.writeValue(outputStream, httpResponse);
	        outputStream.flush();
		
		
	}
	
	
	
	

}
