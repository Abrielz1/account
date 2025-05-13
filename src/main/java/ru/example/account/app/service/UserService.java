package ru.example.account.app.service;

import jakarta.validation.constraints.NotNull;
import ru.example.account.app.security.service.impl.AppUserDetails;
import ru.example.account.web.model.usr.request.UpdateUserAccountDetailRequestShortDto;
import ru.example.account.web.model.usr.response.UpdateUserAccountDetailResponseShortDto;
import java.util.List;

public interface UserService {

    List<UpdateUserAccountDetailResponseShortDto> getUserAccounts(Long cursor, Integer size, String sortOrder);

    UpdateUserAccountDetailResponseShortDto getUserAccountByUserId(Long userId);

    UpdateUserAccountDetailResponseShortDto editUserAccountDetail(Long userId,
                                                                  UpdateUserAccountDetailRequestShortDto updateUser);

    void userDelete(@NotNull AppUserDetails currentUser, String emailForPruning);
}
