package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.example.account.security.dto.in.ClientRegisterRequestDto;
import ru.example.account.security.service.worker.TokenCreationWorker;
import ru.example.account.shared.util.String2CRConverter;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCreationWorkerImpl implements TokenCreationWorker {

    private final String SALT = "^&GOUYGOU%R&(*^FUTUY6ythrV&TDF08i9h8070bRUCVOTIY%DTFCVDcb2zOb";

    private static final String HEADER = "HVTd6J*y%_";

    private static final String TAIL = "_NdhfKid*";


    private final String2CRConverter string2CRConverter;

    @Override
    public String createToken(ClientRegisterRequestDto request) {

        StringBuilder result = new StringBuilder();
        result.append(request.userName())
              .append("_")
              .append(request.phone())
              .append("_")
              .append(request.email())
              .append("_")
              .append(SALT);

        return HEADER + this.string2CRConverter.convertIntoCRC(HEADER + result + TAIL) + TAIL;
    }
}
