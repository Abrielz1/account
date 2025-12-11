package ru.example.account.security.api.controller.registration;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.example.account.security.model.request.UserRegisterRequestDto;
import ru.example.account.user.model.response.CreateUserAccountDetailResponseDto;

@Slf4j
@Tag(name = "Client oriented controller", description = "Clients management")
@Validated
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class ClientController {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public CreateUserAccountDetailResponseDto registerUser(@Valid @RequestBody UserRegisterRequestDto request,
                                                           HttpServletRequest httpRequest ) {
        log.info("User registration attempt: {}", request.email());
       // return clientService.registerNewUser(request, httpRequest);
        return null;
    }
}






