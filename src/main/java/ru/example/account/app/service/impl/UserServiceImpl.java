package ru.example.account.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.app.repository.UserRepository;
import ru.example.account.app.service.UserService;
import ru.example.account.app.service.UserSpecification;
import ru.example.account.util.mapper.UserMapper;
import ru.example.account.web.model.usr.request.UserSearchResponseDto;
import java.time.LocalDate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Page<UserSearchResponseDto> searchUsers(LocalDate dateOfBirth,
                                                   String phone,
                                                   String name,
                                                   String email,
                                                   PageRequest page) {

        return userRepository.findAll(new UserSpecification(dateOfBirth, phone, name, email), page)
                .map(UserMapper::toUserSearchResponseDto);

    }
}