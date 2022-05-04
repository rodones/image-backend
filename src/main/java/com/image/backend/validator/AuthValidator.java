package com.image.backend.validator;

import com.image.backend.dto.auth.LoginRequest;
import com.image.backend.enums.EClientId;
import com.image.backend.enums.EGrantType;
import com.image.backend.enums.ERole;
import com.image.backend.repository.UserRepository;
import com.image.backend.util.SoilBackendRegex;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final EClientId eClientId;
    private final UserRepository userRepository;
    private final MessageSourceAccessor messageSource;

    private Map<String, String> roleAndClientIdTable = null;

    @PostConstruct
    protected void init() {
        roleAndClientIdTable = Map.of(
                ERole.USER, eClientId.getMobileClientId(),
                ERole.SYSADMIN, eClientId.getWebClientId()
        );
    }

    public ValidationResult validate(@NotNull LoginRequest dto) {
        if (dto.getGrant_type() == EGrantType.password) {
            if (dto.getUsername() == null || dto.getUsername().isBlank()) {
                return ValidationResult.failed(
                        messageSource.getMessage("login_request.username.empty")
                );
            }

            if (!dto.getUsername().matches(SoilBackendRegex.EMAIL)) {
                return ValidationResult.failed(
                        messageSource.getMessage("validation.generic.email.unfit_regex")
                );
            }

            if (dto.getPassword() == null || dto.getPassword().isBlank()) {
                return ValidationResult.failed(
                        messageSource.getMessage("login_request.password.empty")
                );
            }
            if (eClientId.getMobileClientId().equals(dto.getClient_id()) &&
                    (dto.getClient_secret() == null || dto.getClient_secret().isBlank())) {
                return ValidationResult.failed(
                        messageSource.getMessage("login_request.client_secret.empty")
                );

            }
        }

        return ValidationResult.success();
    }

    public boolean isMobileUser(String client_id) {
        return client_id.equals(eClientId.getMobileClientId());
    }
}
