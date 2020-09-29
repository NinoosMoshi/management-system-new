package com.ninos.service.Impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;


import javax.mail.MessagingException;
import javax.transaction.Transactional;

import com.ninos.exception.domain.EmailNotFoundException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ninos.domain.User;
import com.ninos.domain.UserPrincipal;
import com.ninos.enumeration.Role;
import com.ninos.exception.domain.EmailExistException;
import com.ninos.exception.domain.UserNotFoundException;
import com.ninos.exception.domain.UsernameExistException;
import com.ninos.repository.UserRepository;
import com.ninos.service.EmailService;
import com.ninos.service.LoginAttemptService;
import com.ninos.service.UserService;

import static com.ninos.constant.FileConstant.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


@Qualifier("UserDetailsService")
@Transactional
@Service
public class UserServiceImple implements UserService, UserDetailsService {
	
	private Logger LOGGER = LoggerFactory.getLogger(getClass());
	private UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;
	private LoginAttemptService loginAttemptService;
	private EmailService emailService;
	
	
    @Autowired
	public UserServiceImple(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService,EmailService emailService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.loginAttemptService = loginAttemptService;
		this.emailService = emailService;
	}



	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
	     User user = userRepository.findUserByUsername(username);
	     if(user == null) {
	    	 LOGGER.error("User not found by username: "+username);
	    	 throw new UsernameNotFoundException("User not found by username: "+username);
	     }else {
	    	 validateLoginAttempt(user);
	    	 user.setLastLoginDateDisplay(user.getLastLoginDate());
	    	 user.setLastLoginDate(new Date());
	    	 userRepository.save(user);
	    	 UserPrincipal userPrincipal = new UserPrincipal(user);
	    	 LOGGER.info("Returning found user by username: "+ username);
	    	 return userPrincipal;
	     }
		
		
	}



	private void validateLoginAttempt(User user) {
		if(user.isNotLocked()) {
			if(loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
				   user.setNotLocked(false);
			}else {
				   user.setNotLocked(true);
			}
		}else {
			loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
		}
	}



	@Override
	public User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {
		validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
		User user = new User();
		user.setUserId(generateUserId());
		String password = generatePassword();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUsername(username);
		user.setEmail(email);
		user.setJoinDate(new Date());
		user.setPassword(encodePassword(password));
		user.setActive(true);
		user.setNotLocked(true);
		user.setRole(Role.ROLE_USER.name());
		user.setAuthorities(Role.ROLE_USER.getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
		userRepository.save(user);
		//LOGGER.info("New user password: " + password);
		emailService.sendNewPasswordEmail(firstName, password, email);
		return user;
	}



	private String getTemporaryProfileImageUrl(String username) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
	}





	private String encodePassword(String password) {
		return passwordEncoder.encode(password);
	}



	private String generatePassword() {
		return RandomStringUtils.randomAlphanumeric(10); // return 10 random letters
	}



	private String generateUserId() {
		return RandomStringUtils.randomNumeric(10);   // return a random String and it has a length = 10 numbers
	}


	
	
	
	

	private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistException, EmailExistException {
        User userByNewUsername = findUserByUsername(newUsername);
        User userByNewEmail = findUserByEmail(newEmail);
        
        if(StringUtils.isNotBlank(currentUsername)) {
        	
            User currentUser = findUserByUsername(currentUsername);
            if(currentUser == null) {
                throw new UserNotFoundException("No user found by username" + currentUsername);
            }
            if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UsernameExistException("Username already exists");
            }
            if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException("Email already Exists");
            }
            return currentUser;
        } else {
            if(userByNewUsername != null) {
                throw new UsernameExistException("Username already exists");
            }
            if(userByNewEmail != null) {
                throw new EmailExistException("Email already exists");
            }
            return null;
        }
    }



	@Override
	public List<User> getUsers() {
		return userRepository.findAll();
	}



	@Override
	public User findUserByUsername(String username) {
		return userRepository.findUserByUsername(username);
	}



	@Override
	public User findUserByEmail(String email) {
		return userRepository.findUserByEmail(email);
	}


	@Override
	public User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {
		validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
		User user = new User();
		String password = generatePassword();
		user.setUserId(generateUserId());
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setJoinDate(new Date());
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(encodePassword(password));
		user.setActive(isActive);
		user.setNotLocked(isNonLocked);
		user.setRole(getRoleEnumName(role).name());
		user.setAuthorities(getRoleEnumName(role).getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
		userRepository.save(user);
		saveProfileImage(user, profileImage);
		return user;
	}

	private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
    	if (user != null){
			Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();  // it will like user/home/supportportal/user/ninos
		    if(!Files.exists(userFolder)){
		    	Files.createDirectories(userFolder);
		    	LOGGER.info(DIRECTORY_CREATED + userFolder);
			}
		    Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + DOT + JPG_EXTENSION));
		    Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
		    user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
		    userRepository.save(user);
		    LOGGER.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
    	}
	}

	private String setProfileImageUrl(String username) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username + FORWARD_SLASH
		 + username + DOT + JPG_EXTENSION).toUriString();
	}

	private Role getRoleEnumName(String role) {
    	return Role.valueOf(role.toUpperCase());
	}

	@Override
	public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {
		User currentUser = validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
		currentUser.setFirstName(newFirstName);
		currentUser.setLastName(newLastName);
		currentUser.setUsername(newUsername);
		currentUser.setEmail(newEmail);
		currentUser.setActive(isActive);
		currentUser.setNotLocked(isNonLocked);
		currentUser.setRole(getRoleEnumName(role).name());
		currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
		userRepository.save(currentUser);
		saveProfileImage(currentUser, profileImage);
		return currentUser; }

	@Override
	public void deleteUser(long id) { userRepository.deleteById(id); }

	@Override
	public void resetPassword(String email) throws MessagingException, EmailNotFoundException {
      User user = userRepository.findUserByEmail(email);
      if(user == null){
      	 throw new EmailNotFoundException("user not found bt Email : "+ email);
	  }
      String password = generatePassword();
      user.setPassword(encodePassword(password));
      userRepository.save(user);
      emailService.sendNewPasswordEmail(user.getFirstName(), password, user.getEmail()); }

	@Override
	public User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {
		User user = validateNewUsernameAndEmail(username, null, null);
		saveProfileImage(user, profileImage);
    	return user; }

}
