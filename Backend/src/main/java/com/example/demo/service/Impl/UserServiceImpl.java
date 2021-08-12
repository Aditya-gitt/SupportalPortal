package com.example.demo.service.Impl;

import com.example.demo.domain.MyUserDetails;
import com.example.demo.domain.User;
import com.example.demo.enumeration.Role;
import com.example.demo.exception.domain.EmailExistsException;
import com.example.demo.exception.domain.EmailNotFoundException;
import com.example.demo.exception.domain.UserNotFoundException;
import com.example.demo.exception.domain.UsernameExistsException;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.EmailService;
import com.example.demo.service.LoginAttemptService;
import com.example.demo.service.UserService;
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

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static com.example.demo.constant.FileConstant.*;
import static com.example.demo.constant.UserImplConstant.NO_USER_FOUND_BY_EMAIL;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private UserRepo userRepo;
    private BCryptPasswordEncoder passwordEncoder;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private LoginAttemptService loginAttemptService;
    private EmailService emailService;

    @Autowired
    public UserServiceImpl(UserRepo userRepo, BCryptPasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService, EmailService emailService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepo.findUserByUsername(username);
        if(user == null) {
            this.logger.error("User with provided username(" + username + ") does not exist!!");
            throw new UsernameNotFoundException("User with provided username(" + username + ") does not exist!!");
        }
        else {
            this.validateLoginAttempts(user);
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepo.save(user);
            logger.info("Found user with username: " + username);
            return new MyUserDetails(user);
        }
    }

    @Override
    public User register(String firstname, String lastname, String username, String email) throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException {
        this.validateUsernameAndEmail(StringUtils.EMPTY, username, email);
        User user = new User();
        user.setUserId(this.generateUserId());
        String password = this.generatePassword();
        String encodedPassword = this.encodePassword(password);
        String message;

        user.setFirstName(firstname);
        user.setLastName(lastname);
        user.setEmail(email);
        user.setUsername(username);
        user.setJoinDate(new Date());
        user.setPassword(encodedPassword);
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(Role.ROLE_USER.name());
        user.setAuthorities(Role.ROLE_USER.getAuthorities());
        user.setProfileImageUrl(this.getTempImgUrl(username));

        userRepo.save(user);
        logger.info("New user password " + password);
        message = "Hello" + firstname + ", \n \n Your new account password is : " + password + "\n \n The Support Team";
        //this.emailService.sendEmail(email, message, "AKI, LLC - New Password");
        return user;
    }

    @Override
    public User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException {
        this.validateUsernameAndEmail(StringUtils.EMPTY, username, email);
        User user = new User();
        String password = this.generatePassword();
        user.setUserId(this.generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setUsername(username);
        user.setJoinDate(new Date());
        user.setPassword(this.encodePassword(password));
        user.setActive(isActive);
        user.setNotLocked(isNonLocked);
        user.setRole(getRoleEnumName(role).name());
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        user.setProfileImageUrl(this.getTempImgUrl(username));

        userRepo.save(user);
        this.saveProfileImage(user, profileImage);
        logger.info("New user password " + password);
        return user;
    }

    @Override
    public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException {
        User currentUser = this.validateUsernameAndEmail(currentUsername, newUsername, newEmail);
        currentUser.setFirstName(newFirstName);
        currentUser.setLastName(newLastName);
        currentUser.setEmail(newEmail);
        currentUser.setUsername(newUsername);
        currentUser.setActive(isActive);
        currentUser.setNotLocked(isNonLocked);
        currentUser.setRole(getRoleEnumName(role).name());
        currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());

        userRepo.save(currentUser);
        this.saveProfileImage(currentUser, profileImage);
        return currentUser;
    }

    @Override
    public List<User> getUsers() {
        List<User> usersList = userRepo.findAll();
        return usersList;
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepo.findUserByUsername(username);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepo.findUserByEmail(email);
    }

    @Override
    public void deleteUserByUsername(String username) {
        this.userRepo.deleteUserByUsername(username);
    }

    @Override
    public void resetPassword(String email) throws EmailNotFoundException, MessagingException {
        User user = this.userRepo.findUserByEmail(email);
        if(user == null) {
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
        }
        String password = this.generatePassword();
        user.setPassword(this.encodePassword(password));
        this.userRepo.save(user);
        this.logger.info("New user password " + password);
        //this.emailService.sendEmail(email, "New Password : " + password, "New Password");
    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException {
        User user = this.validateUsernameAndEmail(username, null, null);
        this.saveProfileImage(user, profileImage);
        return user;
    }

    private String getTempImgUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
    }

    private String encodePassword(String password) {
        return this.passwordEncoder.encode(password);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private User validateUsernameAndEmail(String currentUsername, String newUsername, String email) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        if(StringUtils.isNotBlank(currentUsername)) {
            User currentUser = findUserByUsername(currentUsername);
            if (currentUser == null) {
                throw new UserNotFoundException("No user found by username " + currentUsername);
            }
            User userByUsername = findUserByUsername(newUsername);
            if(userByUsername != null && currentUser.getId() != userByUsername.getId()) {
                throw new UsernameExistsException("Username already exists!!");
            }
            User userByEmail = findUserByEmail(email);
            if(userByEmail != null && currentUser.getId() != userByEmail.getId()){
                throw new EmailExistsException("Email already exists!!");
            }
            return currentUser;
        }
        else {
            User userByUsername = findUserByUsername(newUsername);
            if(userByUsername != null) {
                throw new UsernameExistsException("Username already exists!!");
            }
            User userByEmail = findUserByEmail(email);
            if(userByEmail != null) {
                throw new EmailExistsException("Email already exists!!");
            }
            return null;
        }
    }

    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
        if(profileImage != null) {
            Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
            if(!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                logger.info(DIRECTORY_CREATED);
            }
            Files.deleteIfExists(Paths.get(userFolder + DOT + user.getUsername() + JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() + DOT + JPG_EXTENSION));
            user.setProfileImageUrl(this.getImgUrl(user.getUsername()));
            this.userRepo.save(user);
            logger.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
        }
    }

    private String getImgUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().
                path(USER_IMAGE_PATH + username + FORWARD_SLASH + username + DOT + JPG_EXTENSION).toUriString();
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    private void validateLoginAttempts(User user) {
        if(user.isNotLocked()) {
            if(this.loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
                user.setNotLocked(false);
            }
            else {
                user.setNotLocked(true);
            }
        }
        else {
            this.loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

}
