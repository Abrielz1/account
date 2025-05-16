package ru.example.account.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.example.account.app.entity.EmailData;
import ru.example.account.app.entity.PhoneData;
import ru.example.account.app.entity.User;
import ru.example.account.app.repository.UserRepository;
import ru.example.account.app.security.service.impl.AppUserDetails;
import ru.example.account.app.service.UserService;
import ru.example.account.app.service.UserSpecification;
import ru.example.account.util.exception.exceptions.AccessDeniedException;
import ru.example.account.util.exception.exceptions.AlreadyExistsException;
import ru.example.account.util.exception.exceptions.UserNotFoundException;
import ru.example.account.util.mapper.UserMapper;
import ru.example.account.web.model.usr.request.ManageUserEmailRequestDto;
import ru.example.account.web.model.usr.request.ManageUserPhoneRequestDto;
import ru.example.account.web.model.usr.request.UserSearchResponseDto;
import ru.example.account.web.model.usr.response.CreateUserAccountDetailResponseDto;
import ru.example.account.web.model.usr.response.UserEmailResponseDto;
import ru.example.account.web.model.usr.response.UserPhoneResponseDto;
import java.time.LocalDate;
import java.util.Objects;

@Slf4j
@Service
@CacheConfig(cacheNames = "users")
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Cacheable(key = "{#dateOfBirth, #phone, #name, #email, #page.pageNumber, #page.pageSize}")
    public Page<UserSearchResponseDto> searchUsers(LocalDate dateOfBirth,
                                                   String phone,
                                                   String name,
                                                   String email,
                                                   PageRequest page) {

        return userRepository.findAll(new UserSpecification(dateOfBirth, phone, name, email), page)
                .map(UserMapper::toUserSearchResponseDto);

    }

    @Override
    public CreateUserAccountDetailResponseDto createUserEmailData(AppUserDetails currentUser,
                                                                  ManageUserEmailRequestDto createPhone) {

        User userFromDb = this.getUserByUserId(currentUser.getId());

        if (!this.isFreeEmail(createPhone.email())) {

            log.error("User with id: {} tries to create already taken email: {}", currentUser.getId(), createPhone.email());
            throw new AlreadyExistsException("User with id: %d tries to create already taken email: %s".formatted(currentUser.getId(), createPhone.email()));
        }

        this.userRightsValidator(currentUser.getId(), userFromDb.getId());

        EmailData emailData = EmailData
                .builder()
                .user(userFromDb)
                .email(createPhone.email())
                .build();

        userFromDb.getUserEmails().add(emailData);

        return UserMapper.toCreateUserAccountDetailResponseDto(this.userRepository.saveAndFlush(userFromDb));
    }

    @Override
    public CreateUserAccountDetailResponseDto createUserPhoneData(AppUserDetails currentUser,
                                                                  ManageUserPhoneRequestDto createPhone) {

        User userFromDb = this.getUserByUserId(currentUser.getId());

        if (!this.isFreePhone(createPhone.phone())) {

            log.error("User with id: {} tries to create already taken phone: {}", currentUser.getId(), createPhone.phone());
            throw new AlreadyExistsException("User with id: %d tries to create already taken phone: %s".formatted(currentUser.getId(), createPhone.phone()));
        }
        this.userRightsValidator(currentUser.getId(), userFromDb.getId());

        PhoneData phoneData = PhoneData
                .builder()
                .user(userFromDb)
                .phone(createPhone.phone())
                .build();

        userFromDb.getUserPhones().add(phoneData);

        return UserMapper.toCreateUserAccountDetailResponseDto(this.userRepository.saveAndFlush(userFromDb));
    }

    @Override
    @CacheEvict(key = "#currentUser.id")
    @Transactional
    public UserEmailResponseDto editUserEmailData(AppUserDetails currentUser, ManageUserEmailRequestDto updateUser) {

        User userFromDb = this.getUserByUserId(currentUser.getId());
        this.userRightsValidator(currentUser.getId(), userFromDb.getId());

        EmailData emailData = EmailData
                .builder()
                .user(userFromDb)
                .email(updateUser.email())
                .build();

        userFromDb.getUserEmails().add(emailData);


        return UserMapper.toUserEmailResponseDto(this.updateEmailData(userFromDb, updateUser));
    }

    @Override
    @CacheEvict(key = "#currentUser.id")
    @Transactional
    public UserPhoneResponseDto editUserPhoneData(AppUserDetails currentUser, ManageUserPhoneRequestDto updateUser) {

        User userFromDb = this.getUserByUserId(currentUser.getId());
        this.userRightsValidator(currentUser.getId(), userFromDb.getId());

        return UserMapper.toUserPhoneResponseDto(this.updatePhoneData(userFromDb, updateUser));
    }


    @Cacheable(key = "#userId", unless = "#result == null")
    public User getUserByUserId(Long userId) {

        return userRepository.findById(userId).orElseThrow(() -> {
            log.error("No user with such id: {}", userId);
            return new UserNotFoundException("No user with such id: %d".formatted(userId));
        });
    }

    private boolean isFreeEmail(String newEmail) {

        return !userRepository.existsEmails(newEmail);
    }

    private boolean isFreePhone(String newPhone) {

        return !userRepository.existsByPhones(newPhone);
    }

    private User updateEmailData(User userFromDb, ManageUserEmailRequestDto updateUser) {

        if (StringUtils.hasText(updateUser.email()) && this.isFreeEmail(updateUser.email())) {

            EmailData emailData = EmailData
                    .builder()
                    .user(userFromDb)
                    .email(updateUser.email())
                    .build();

            userFromDb.getUserEmails().add(emailData);
        }

        return userFromDb;
    }

    private User updatePhoneData(User userFromDb, ManageUserPhoneRequestDto updateUser) {

        if (StringUtils.hasText(updateUser.phone()) && this.isFreePhone(updateUser.phone())) {

            PhoneData phoneData = PhoneData
                    .builder()
                    .user(userFromDb)
                    .phone(updateUser.phone())
                    .build();

            userFromDb.getUserPhones().add(phoneData);
        }

        return userFromDb;
    }

    @CacheEvict(allEntries = true)
    public void evictAllUsersCache() {
        log.info("Evicting all users cache");
    }

    private void userRightsValidator(Long userIdFromToken, Long userIdFromDb) {

        if (!Objects.equals(userIdFromToken, userIdFromDb)) {

            throw new AccessDeniedException("You can only edit your own data");
        }
    }
}