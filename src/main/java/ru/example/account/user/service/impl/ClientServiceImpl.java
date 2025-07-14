package ru.example.account.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.example.account.security.model.request.UserRegisterRequestDto;
import ru.example.account.user.entity.Client;
import ru.example.account.user.entity.EmailData;
import ru.example.account.user.entity.PhoneData;
import ru.example.account.user.entity.RoleType;
import ru.example.account.user.entity.User;
import ru.example.account.user.model.request.ManageUserEmailRequestDto;
import ru.example.account.user.model.request.ManageUserPhoneRequestDto;
import ru.example.account.user.model.request.UserSearchResponseDto;
import ru.example.account.user.model.response.CreateUserAccountDetailResponseDto;
import ru.example.account.user.model.response.UserEmailResponseDto;
import ru.example.account.user.model.response.UserPhoneResponseDto;
import ru.example.account.user.repository.ClientRepository;
import ru.example.account.security.service.impl.AppUserDetails;
import ru.example.account.user.service.UserProcessor;
import ru.example.account.user.service.ClientService;
import ru.example.account.shared.exception.exceptions.AccessDeniedException;
import ru.example.account.shared.exception.exceptions.AlreadyExistsException;
import java.time.LocalDate;
import java.util.Objects;
import static ru.example.account.shared.mapper.ClientMapper.toAuthResponse;

@Slf4j
@Service
@CacheConfig(cacheNames = "clients")
@Transactional
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final UserProcessor userProcessor;

    private final ClientRepository castomerRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional("businessTransactionManager")
    public CreateUserAccountDetailResponseDto registerNewUser(UserRegisterRequestDto request) {

        if (castomerRepository.checkUserByUsername(request.username())) {
            throw new AlreadyExistsException("Username " + request.username() + " is already taken.");
        }

        if (castomerRepository.checkUserByEmail(request.email())) {
            throw new AlreadyExistsException("Email " + request.email() + " is already taken.");
        }

        if (castomerRepository.checkUserByPhone(request.phoneNumber())) {
            throw new AlreadyExistsException("Phone " + request.phoneNumber() + " is already taken.");
        }

        if (!StringUtils.hasText(request.accountName()) || !StringUtils.hasText(request.phoneNumber()) || !StringUtils.hasText(request.email())) {
            throw new  IllegalArgumentException("Client must send valid data!");
        }

        Client newClient = new Client();

        newClient.setFieldsClient(request);

        newClient.setPassword(passwordEncoder.encode(request.password()));

        newClient.addRole(RoleType.ROLE_CLIENT);
        newClient.addEmail(request.email());
        newClient.addPhone(request.phoneNumber());

        castomerRepository.save(newClient);

        return toAuthResponse(newClient);
    }

   // @Override
    @Cacheable(key = "{#dateOfBirth, #phone, #name, #email, #page.pageNumber, #page.pageSize}")
    public Page<UserSearchResponseDto> searchUsers(LocalDate dateOfBirth,
                                                   String phone,
                                                   String name,
                                                   String email,
                                                   PageRequest page) {

//        return userRepository.findAll(new UserSpecification(dateOfBirth, phone, name, email), page)
//                .map(UserMapper::toUserSearchResponseDto);
        return null;
    }

  //  @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#currentUser.id"),
            @CacheEvict(value = "userSearch", allEntries = true)
    })
    public CreateUserAccountDetailResponseDto createUserEmailData(AppUserDetails currentUser,
                                                                  ManageUserEmailRequestDto createPhone) {

        User userFromDb = userProcessor.getUserByUserId(currentUser.getId());

        if (!this.userProcessor.isFreeEmail(createPhone.email())) {

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

      //  return UserMapper.toCreateUserAccountDetailResponseDto(this.userRepository.save(userFromDb));
        return null;
    }

 //   @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#currentUser.id"),
            @CacheEvict(value = "userSearch", allEntries = true)
    })
    public CreateUserAccountDetailResponseDto createUserPhoneData(AppUserDetails currentUser,
                                                                  ManageUserPhoneRequestDto createPhone) {

        User userFromDb = userProcessor.getUserByUserId(currentUser.getId());

        if (!this.userProcessor.isFreePhone(createPhone.phone())) {

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

      //  return UserMapper.toCreateUserAccountDetailResponseDto(this.userRepository.save(userFromDb));
        return null;
    }

  //  @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#currentUser.id"),
            @CacheEvict(value = "userSearch", allEntries = true)
    })
    public UserEmailResponseDto editUserEmailData(AppUserDetails currentUser, ManageUserEmailRequestDto updateUser) {

        User userFromDb = userProcessor.getUserByUserId(currentUser.getId());
        this.userRightsValidator(currentUser.getId(), userFromDb.getId());

        EmailData emailData = EmailData
                .builder()
                .user(userFromDb)
                .email(updateUser.email())
                .build();

        userFromDb.getUserEmails().add(emailData);


       // return UserMapper.toUserEmailResponseDto(this.updateEmailData(userFromDb, updateUser));
        return null;
    }

 //   @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#currentUser.id"),
            @CacheEvict(value = "userSearch", allEntries = true)
    })
    public UserPhoneResponseDto editUserPhoneData(AppUserDetails currentUser, ManageUserPhoneRequestDto updateUser) {

        User userFromDb = userProcessor.getUserByUserId(currentUser.getId());
        this.userRightsValidator(currentUser.getId(), userFromDb.getId());

       // return UserMapper.toUserPhoneResponseDto(this.updatePhoneData(userFromDb, updateUser));
        return null;
    }

    private User updateEmailData(User userFromDb, ManageUserEmailRequestDto updateUser) {

        if (StringUtils.hasText(updateUser.email()) && this.userProcessor.isFreeEmail(updateUser.email())) {

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

        if (StringUtils.hasText(updateUser.phone()) && this.userProcessor.isFreePhone(updateUser.phone())) {

            PhoneData phoneData = PhoneData
                    .builder()
                    .user(userFromDb)
                    .phone(updateUser.phone())
                    .build();

            userFromDb.getUserPhones().add(phoneData);
        }

        return userFromDb;
    }

    private void userRightsValidator(Long userIdFromToken, Long userIdFromDb) {

        if (!Objects.equals(userIdFromToken, userIdFromDb)) {

            throw new AccessDeniedException("You can only edit your own data");
        }
    }
}