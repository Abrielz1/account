package ru.example.account.web.model.account.response;


import ru.example.account.app.entity.RoleType;
import java.util.Set;

public record UserCreateResponse(Long id,

                                 String email,

                                 Set<RoleType> roles) {
}
