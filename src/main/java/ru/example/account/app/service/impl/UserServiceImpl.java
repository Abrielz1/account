package ru.example.account.app.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.app.entity.User;
import ru.example.account.app.repository.UserRepository;
import ru.example.account.app.security.service.impl.AppUserDetails;
import ru.example.account.app.service.UserService;
import ru.example.account.web.model.usr.request.UpdateUserAccountDetailRequestDto;
import ru.example.account.web.model.usr.response.UpdateUserAccountDetailResponseShortDto;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UpdateUserAccountDetailResponseShortDto> getUserAccounts(Long cursor, Integer size, String sortOrder) {

        Long effectiveCursor = Optional.ofNullable(cursor).orElseGet(() -> 0L);
        Integer effectiveSize = Optional.ofNullable(size).orElseGet(() -> 10);

        Sort.Direction sort = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        log.info("users list were sent via userController");
//        return userRepository.findByIdGreaterThan(effectiveCursor, PageRequest.of(0, effectiveSize, Sort.by(sort, "id")))
//                .map(UserMapper::toUpdateUserAccountDetailResponseShortDto);
        return null;
    }

    @Override
    public UpdateUserAccountDetailResponseShortDto getUserAccountByUserId(Long userId) {
        log.info("user account with userId: {} was sent via user service", userId);
       // return UserMapper.toUpdateUserAccountDetailResponseShortDto(this.getUserFromDbByUserId(userId));
        return null;
    }

    @Override
    public UpdateUserAccountDetailResponseShortDto editUserAccountDetail(Long userId,
                                                                         UpdateUserAccountDetailRequestDto updateUser) {
        User userFromDb = this.getUserFromDbByUserId(userId);

        log.info("user account was updated via user service with userId: {}", userId);
//        return UserMapper.toUpdateUserAccountDetailResponseShortDto(
//                this.setupEntity(userFromDb, updateUser, passwordEncoder));
        return null;
    }

    @Override
    public void userDelete(@NotNull AppUserDetails currentUser, String emailForPruning) {

        User userToSelfPrune = this.getUserFromDbByEmail(emailForPruning);

//       this.checkUserAccountForRightToSelfPruning(userToSelfPrune, emailForPruning);
//        userRepository.
//        log.info("Deletion requested for user: {}", emailForPruning);
    }

    private void checkUserAccountForRightToSelfPruning(@NotNull User currentUser, String emailForPruning) {

//        if (!currentUser.getEmail().equalsIgnoreCase(emailForPruning)) {
//
//            log.error("User account has no right to prune this username: {} account! Or account with email: {} not present!!", currentUser.getUsername(), emailForPruning);
//            throw new AccessDeniedException("User tries prune other account! Or account with email: " + emailForPruning + " not present!");
//        }

        log.info("User account has acquired right to be self pruned with email: {}", emailForPruning);
    }

    private User getUserFromDbByUserId(Long userId) {
        log.info("user with userId: {} was sent from db via UserService", userId);
//        return userRepository.findUserById(userId).orElseThrow(() -> {
//            log.error("user with userId: {} was not fond in db via UserService", userId);
//            return new UserNotFoundException("user with userId:" + userId + " was not fond in db");
//        });
        return null;
    }

    private User getUserFromDbByEmail(String userEmail) {

//        return userRepository.findUserByEmail(userEmail).orElseThrow(() -> {
//            log.error("No user in Db with given email: {}", userEmail);
//            return new UserNotFoundException("No user in Db with given email " + userEmail);
//        });
        return null;
    }

    private User setupEntity(User userFromDb, UpdateUserAccountDetailRequestDto updateUser, PasswordEncoder passwordEncoder) {

//        Optional.ofNullable(updateUser.firstName()).ifPresent(userFromDb::setFirstName);
//        Optional.ofNullable(updateUser.lastName()).ifPresent(userFromDb::setLastName);
//        Optional.ofNullable(updateUser.password()).ifPresent(pws -> userFromDb.setPassword(passwordEncoder.encode(pws)));
      //  log.info("user was updated! With email: {}", userFromDb.);
        return userFromDb;
    }
}
