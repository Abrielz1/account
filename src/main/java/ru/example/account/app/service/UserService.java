package ru.example.account.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.example.account.web.model.usr.request.UserSearchResponseDto;
import java.time.LocalDate;

public interface UserService {

    Page<UserSearchResponseDto> searchUsers(LocalDate dateOfBirth, String phone, String name, String email, PageRequest page);
}
