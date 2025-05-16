package ru.example.account.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.example.account.app.entity.Account;
import ru.example.account.app.entity.User;
import ru.example.account.app.repository.AccountRepository;
import ru.example.account.app.repository.UserRepository;
import ru.example.account.app.security.jwt.JwtUtils;
import ru.example.account.app.security.service.impl.AppUserDetails;
import ru.example.account.web.model.account.request.CreateMoneyTransferRequest;
import ru.example.account.web.model.account.response.CreateMoneyTransferResponse;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private JwtUtils jwtUtils;

    private User user1;
    private User user2;
    private Account account1;
    private Account account2;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        account1 = new Account(1L, new BigDecimal("100"), new BigDecimal("100"), null, 0L);
        account2 = new Account(2L, new BigDecimal("50"), new BigDecimal("50"), null, 0L);

        user1 = User.builder()
                .id(1L)
                .userAccount(account1)
                .build();

        user2 = User.builder()
                .id(2L)
                .userAccount(account2)
                .build();
    }

    @Test
    void transferMoney_SuccessfulTransfer() {
        // Arrange
        AppUserDetails currentUser = new AppUserDetails(user1, "test@mail.com");
        CreateMoneyTransferRequest request = new CreateMoneyTransferRequest(2L, new BigDecimal("50"));

        when(userRepository.findWithLockingById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findWithLockingById(2L)).thenReturn(Optional.of(user2));
        when(jwtUtils.getUserIdFromClaimJwt(any())).thenReturn(1L);

        // Act
        CreateMoneyTransferResponse response = accountService
                .transferFromOneAccountToAnother(currentUser, request, "token");

        // Assert
        assertTrue(response.result());
        assertEquals(new BigDecimal("50"), user1.getUserAccount().getBalance());
        assertEquals(new BigDecimal("100"), user2.getUserAccount().getBalance());
    }

    @Test
    void transferMoney_InsufficientBalance() {
        AppUserDetails currentUser = new AppUserDetails(user1, "test@mail.com");
        CreateMoneyTransferRequest request = new CreateMoneyTransferRequest(2L, new BigDecimal("150"));

        when(userRepository.findWithLockingById(any())).thenReturn(Optional.of(user1));

        assertThrows(IllegalStateException.class, () ->
                accountService.transferFromOneAccountToAnother(currentUser, request, "token"));
    }

    @Test
    void transferMoney_SelfTransfer() {
        AppUserDetails currentUser = new AppUserDetails(user1, "test@mail.com");
        CreateMoneyTransferRequest request = new CreateMoneyTransferRequest(1L, new BigDecimal("10"));

        assertThrows(IllegalStateException.class, () ->
                accountService.transferFromOneAccountToAnother(currentUser, request, "token"));
    }
}
