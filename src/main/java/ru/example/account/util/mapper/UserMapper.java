package ru.example.account.util.mapper;

import ru.example.account.app.entity.EmailData;
import ru.example.account.app.entity.PhoneData;
import ru.example.account.app.entity.User;
import ru.example.account.web.model.usr.request.UserSearchResponseDto;
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
}
