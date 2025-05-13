package ru.example.account.util.mapper;

import ru.example.account.web.model.account.request.CreateUserRequest;

import java.time.LocalDateTime;

public class UserMapper {

    private UserMapper() {
    }

//    public static User toUser(CreateUserRequest userRequest) {
//        new User();
//        return User.builder()
//                .username(userRequest.username())
//
//                .password(userRequest.password())
//
//                .roles(userRequest.roles())
//                .build();
//    }

//    public static UserCreateResponse toUserCreateResponse(User newUser) {
//
//        return new UserCreateResponse(newUser.getId(),
//                                      newUser.getEmail(),
//                                      newUser.getRoles());
//    }
//    public static UpdateUserAccountDetailResponseShortDto toUpdateUserAccountDetailResponseShortDto(User user) {
//
//        return new UpdateUserAccountDetailResponseShortDto(
//                user.getId(),
//                user.getEmail(),
//                user.getFirstName(),
//                user.getLastName());
//    }
}
