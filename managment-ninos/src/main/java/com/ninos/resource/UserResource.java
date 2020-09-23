package com.ninos.resource;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import static com.ninos.constant.SecurityConstant.*;

import javax.mail.MessagingException;

import com.ninos.domain.User;
import com.ninos.domain.UserPrincipal;
import com.ninos.exception.domain.EmailExistException;
import com.ninos.exception.domain.ExceptionHandling;
import com.ninos.exception.domain.UserNotFoundException;
import com.ninos.exception.domain.UsernameExistException;
import com.ninos.service.UserService;
import com.ninos.utility.JWTTokenProvider;

@RestController
@RequestMapping({"/", "/user"})
public class UserResource extends ExceptionHandling{
	
	private UserService userService;
	private AuthenticationManager authenticationManager;
	private JWTTokenProvider JWTTokenProvider;
	
	
	@Autowired
	public UserResource(UserService userService, AuthenticationManager authenticationManager, JWTTokenProvider jWTTokenProvider) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.JWTTokenProvider = jWTTokenProvider;
	}


	@GetMapping("home")
	public String showHome(){
		return "application works";
	}



	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException  {
		User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
		return new ResponseEntity<>(newUser,HttpStatus.OK);
	}





	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user) {
		authenticate(user.getUsername(), user.getPassword());
		User loginUser = userService.findUserByUsername(user.getUsername());
		UserPrincipal userPrincipal = new UserPrincipal(loginUser);
		HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
		return new ResponseEntity<>(loginUser, jwtHeader , HttpStatus.OK);
	}

	private void authenticate(String username, String password) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	}


	private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(JWT_TOKEN_HEADER, JWTTokenProvider.generateJwtToken(userPrincipal));
		return headers;
	}







	
	





}
	
