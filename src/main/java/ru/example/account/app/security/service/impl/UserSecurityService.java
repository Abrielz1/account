package ru.example.account.app.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.example.account.app.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByUsername(username)

                .map(AppUserDetails::new)
                .orElseThrow(() -> {
                    log.error("No user with this username: %s".formatted(username));
                    return new UsernameNotFoundException("No user with this username: " + username);
                });
    }
}
