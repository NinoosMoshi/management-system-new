package com.ninos.service;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import com.ninos.domain.User;
import com.ninos.exception.domain.EmailExistException;
import com.ninos.exception.domain.EmailNotFoundException;
import com.ninos.exception.domain.UserNotFoundException;
import com.ninos.exception.domain.UsernameExistException;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
	
	User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException;
	List<User> getUsers();
	User findUserByUsername(String username);
	User findUserByEmail(String email);
	User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException;
	User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException;
    void deleteUser(long id);
    void resetPassword(String email) throws MessagingException, EmailNotFoundException;
    User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException;


}
