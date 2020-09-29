package com.ninos.resource;



import com.ninos.domain.HttpResponse;
import com.ninos.exception.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import static com.ninos.constant.FileConstant.*;
import static com.ninos.constant.SecurityConstant.*;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

import javax.mail.MessagingException;

import com.ninos.domain.User;
import com.ninos.domain.UserPrincipal;
import com.ninos.service.UserService;
import com.ninos.utility.JWTTokenProvider;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping({"/", "/user"})
public class UserResource extends ExceptionHandling{

	public static final String EMAIL_SENT = "An email with a new password was sent to: ";
	public static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";
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
		return new ResponseEntity<>(newUser, OK);
	}


	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user) {
		authenticate(user.getUsername(), user.getPassword());
		User loginUser = userService.findUserByUsername(user.getUsername());
		UserPrincipal userPrincipal = new UserPrincipal(loginUser);
		HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
		return new ResponseEntity<>(loginUser, jwtHeader , OK);
	}


	@PostMapping("/add")
	public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName,
										   @RequestParam("lastName") String lastName,
										   @RequestParam("username") String username,
										   @RequestParam("email") String email,
										   @RequestParam("role") String role,
										   @RequestParam("isActive") String isActive,
										   @RequestParam("isNotLocked") String isNotLocked,
										   @RequestParam(value = "profileImage", required = false)MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {


		User newUser = userService.addNewUser(firstName, lastName, username, email,role, Boolean.parseBoolean(isNotLocked), Boolean.parseBoolean(isActive), profileImage);
           return new ResponseEntity<>(newUser, OK);
	}


	@PostMapping("/update")
	public ResponseEntity<User> update(@RequestParam("currentUsername") String currentUsername,
									   @RequestParam("firstName") String firstName,
									   @RequestParam("lastName") String lastName,
									   @RequestParam("username") String username,
									   @RequestParam("email") String email,
									   @RequestParam("role") String role,
									   @RequestParam("isActive") String isActive,
									   @RequestParam("isNotLocked") String isNotLocked,
									   @RequestParam(value = "profileImage", required = false)MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {


		User updatedUser = userService.updateUser(currentUsername, firstName, lastName, username, email,role, Boolean.parseBoolean(isNotLocked), Boolean.parseBoolean(isActive), profileImage);
		return new ResponseEntity<>(updatedUser, OK);
	}

    @GetMapping("/find/{username}")
	public ResponseEntity<User> getUser(@PathVariable("username") String username){
		 User user = userService.findUserByUsername(username);
		 return new ResponseEntity<>(user,OK);
	}

	@GetMapping("/list")
	public ResponseEntity<List<User>> getAllUsers(){
		List users = userService.getUsers();
		return new ResponseEntity<>(users,OK);
	}

	@GetMapping("/resetPassword/{email}")
	public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws MessagingException, EmailNotFoundException {
		userService.resetPassword(email);
		return response(OK, EMAIL_SENT+ email);
	}

	@DeleteMapping("/delete/{id}")
	@PreAuthorize("hasAnyAuthority('user:delete')")
	public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") long id){
         userService.deleteUser(id);
         return response( NO_CONTENT, USER_DELETED_SUCCESSFULLY);  // NO_CONTENT = 204
	}

	@PostMapping("/updateProfileImage")
	public ResponseEntity<User> updateProfileImage(
									   @RequestParam("username") String username,
									   @RequestParam(value = "profileImage")MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {


		User user = userService.updateProfileImage(username, profileImage);
		return new ResponseEntity<>(user, OK);
	}

	@GetMapping(path = "/image/{username}/{fileName}", produces = IMAGE_JPEG_VALUE)
	public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("fileName") String fileName) throws IOException {
		return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH + fileName)); // "user.home" + "/supportportal/user/ninos/ninos.jpg"
	}


	@GetMapping(path = "/image/profile/{username}", produces = IMAGE_JPEG_VALUE)
	public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException {
		URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + username);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try(InputStream inputStream = url.openStream()) {
			 int bytesRead;
			 byte[] chunk = new byte[1024];
			 while ((bytesRead = inputStream.read(chunk)) > 0){
			 	byteArrayOutputStream.write(chunk, 0, bytesRead);
			 }
		}
		return byteArrayOutputStream.toByteArray();
	}





	private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
		return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(),message.toUpperCase()),httpStatus);
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
	
