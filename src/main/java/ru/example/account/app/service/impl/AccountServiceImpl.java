package ru.example.account.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.app.entity.User;
import ru.example.account.app.repository.UserRepository;
import ru.example.account.app.security.jwt.JwtUtils;
import ru.example.account.app.security.service.impl.AppUserDetails;
import ru.example.account.app.service.AccountService;
import ru.example.account.util.exception.exceptions.BadRequestException;
import ru.example.account.util.exception.exceptions.UserNotFoundException;
import ru.example.account.web.model.account.request.CreateMoneyTransferRequest;
import ru.example.account.web.model.account.response.CreateMoneyTransferResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;

    private final JwtUtils jwtUtils;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CreateMoneyTransferResponse transferFromOneAccountToAnother(AppUserDetails currentUser,
                                                                        CreateMoneyTransferRequest request,
                                                                        String token) {


            if (request.sum() == null) {
                log.error("sum and balance must not be a null");
                throw new IllegalArgumentException("sum and balance must not be a null");
            }

            if (request.sum().compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Sum to transfer must be greater than zero!");
                throw new IllegalArgumentException("Sum to transfer must be greater than zero!");
            }

            Long userIdFromToken = this.getUserIdAndValidateToken(token);

            if (userIdFromToken == null) {
                log.error("Invalid user ID in token");
                throw new BadRequestException("Invalid user ID in token");
            }

            Long userIdFromDetails = currentUser.getId();

            if (Objects.equals(userIdFromToken, request.to())) {
                log.error("User tries to move money to them self");
                throw new IllegalStateException("User tries to move money to them self");
            }

            if (!Objects.equals(userIdFromToken, userIdFromDetails)) {
                log.error("User with id: {} and other id {} tries steel money!", userIdFromDetails, userIdFromToken);
                throw new BadRequestException("User with id: %d and other id %d tries steel money!".formatted(userIdFromDetails, userIdFromToken));
            }

        List<Long> usersIdsList = new ArrayList<>(List.of(userIdFromToken, request.to())).stream()
                .sorted(Long::compareTo)
                .toList();

            User firstUser = this.getUserFromDb(usersIdsList.get(0));
            User secondUser = this.getUserFromDb(usersIdsList.get(1));


            return this.validateAndPrecededTransfer(firstUser, secondUser, request.sum());
    }

    private Long getUserIdAndValidateToken(String token) {

        if (token == null) {
            log.info("%Presented token are empty");
            throw new BadRequestException("Presented token are empty");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return jwtUtils.getUserIdFromClaimJwt(token);
    }

    private CreateMoneyTransferResponse validateAndPrecededTransfer(User hostUserAccount,
                                                                    User receiverUserAccount,
                                                                    BigDecimal moneyToTransfer) {
        if (hostUserAccount.getId().equals(receiverUserAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }

        if (hostUserAccount.getUserAccount().getBalance().compareTo(moneyToTransfer) >= 0) {
            BigDecimal amount = moneyToTransfer.setScale(2, RoundingMode.HALF_UP);

            BigDecimal senderBalance = hostUserAccount.getUserAccount().getBalance()
                    .subtract(amount)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal receiverBalance = receiverUserAccount.getUserAccount().getBalance()
                    .add(amount)
                    .setScale(2, RoundingMode.HALF_UP);

            hostUserAccount.getUserAccount().setBalance(senderBalance);
            receiverUserAccount.getUserAccount().setBalance(receiverBalance);

            log.info("Operation was succeed");
            return new CreateMoneyTransferResponse(true, "Operation was succeed");
        } else {
            log.error("Operation was declined. User tries move more than possessed!");
            return new CreateMoneyTransferResponse(false, "Operation was declined. User tries move more than possessed!");
        }

    }

    private User getUserFromDb(Long userId) {

        return this.userRepository.findWithLockingById(userId).orElseThrow(() -> {
           log.error("No user in db with such id: {}", userId);
           return new UserNotFoundException("No user in db with such id: %d".formatted(userId));
        });
    }
}
