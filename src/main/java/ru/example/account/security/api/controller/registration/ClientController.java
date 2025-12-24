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
import ru.example.account.security.dto.in.ClientRegisterRequestDto;
import ru.example.account.security.dto.out.ClientRegistrationRequestedResponceDto;
import ru.example.account.security.service.facade.ClientRegistrationService;

@Slf4j
@Tag(name = "Client oriented controller", description = "Clients management")
@Validated
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class ClientController {

    private final ClientRegistrationService clientRegistrationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public ClientRegistrationRequestedResponceDto registerUser(@Valid @RequestBody ClientRegisterRequestDto request,
                                                               HttpServletRequest httpRequest) {

        log.info("User registration attempt: {}", request.email());
        return this.clientRegistrationService.register(request, httpRequest);
    }
}






