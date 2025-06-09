package ru.example.account.app.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.example.account.app.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

  //  private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.info("Loading user by email: {}", email);

        var user = userRepository.getFullUserData(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found");
                });

//        boolean emailExists = user.getUserEmails().stream()
//                .anyMatch(e -> e.getEmail().equals(email));

        log.info("User found: {}", user.getUsername());
        log.info("Password: {}", user.getPassword());
        log.info("Roles: {}", user.getRoles());


//        boolean passwordMatches = passwordEncoder.matches("password", user.getPassword());
//        log.info("Password matches: {}", passwordMatches);

//        if (!emailExists) {
//            throw new BadCredentialsException("Email not registered for user");
//        }

        return new AppUserDetails(user, email);
    }
}
