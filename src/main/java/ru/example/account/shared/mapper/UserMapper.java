package ru.example.account.shared.mapper;

import ru.example.account.business.entity.EmailData;
import ru.example.account.business.entity.PhoneData;
import ru.example.account.user.entity.User;
import ru.example.account.security.model.request.UserSearchResponseDto;
import ru.example.account.security.model.response.CreateUserAccountDetailResponseDto;
import ru.example.account.security.model.response.UserEmailResponseDto;
import ru.example.account.security.model.response.UserPhoneResponseDto;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    private UserMapper() {
    }

    public static UserSearchResponseDto toUserSearchResponseDto(User user) {

        return new UserSearchResponseDto(user.getUsername(), user.getDateOfBirth(),
                                         toPhones(user.getUserPhones()),
                                         toEmails(user.getUserEmails()));
    }

    private static Set<String> toPhones(Set<PhoneData> phoneData) {
        if (phoneData.isEmpty()) {
            return Set.of();
        }

            return phoneData.stream()
                    .map(PhoneData::getPhone)
                    .collect(Collectors.toSet());
    }

    private static Set<String> toEmails(Set<EmailData> emailData) {
        if (emailData.isEmpty()) {
            return Set.of();
        }

        return emailData.stream()
                .map(EmailData::getEmail)
                .collect(Collectors.toSet());
    }

    public static UserEmailResponseDto toUserEmailResponseDto(User user) {

        return new UserEmailResponseDto(toEmails(user.getUserEmails()));

    }

    public static UserPhoneResponseDto toUserPhoneResponseDto(User user) {

        return new UserPhoneResponseDto(toPhones(user.getUserPhones()));
    }

    public static CreateUserAccountDetailResponseDto toCreateUserAccountDetailResponseDto(User user) {

        return new CreateUserAccountDetailResponseDto(user.getId(),
                                                      toEmails(user.getUserEmails()),
                                                      toPhones(user.getUserPhones()));

    }
}
