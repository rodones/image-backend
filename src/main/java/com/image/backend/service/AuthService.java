package com.image.backend.service;

import com.image.backend.config.security.JwtUtil;
import com.image.backend.dto.auth.LoginRequest;
import com.image.backend.dto.auth.LoginResponse;
import com.image.backend.enums.EErrorCode;
import com.image.backend.model.User;
import com.image.backend.validator.AuthValidator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthValidator authValidator;
    private final MessageSourceAccessor messageSource;

    public LoginResponse login(@NotNull LoginRequest body) {
        var result = authValidator.validate(body);

        if (result.isNotValid()) {
            return LoginResponse.notOk(result.getMessage(), EErrorCode.UNAUTHORIZED);
        }

        switch (body.getGrant_type()) {
            case password:
                return loginWithPassword(body);
            default:
                return LoginResponse.notOk(messageSource
                        .getMessage("login_request.grant_type.unknown"), EErrorCode.BAD_REQUEST);
        }
    }

    @Transactional
    protected LoginResponse loginWithPassword(LoginRequest body) {
        try {
            var authentication = new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword());
            var authenticate = authenticationManager.authenticate(authentication);
            var user = (User) authenticate.getPrincipal();

            if (authValidator.isMobileUser(body.getClient_id())) {
                var new_secret = "";
                if (body.getClient_secret() == null) {
                    new_secret = "invalid-secret";
                } else {
                    new_secret = body.getClient_secret();
                }
            }

            var accessToken = jwtUtil.generateAccessToken(user, body.getClient_id());

            return LoginResponse.ok(accessToken);
        } catch (AuthenticationException e) {
            return LoginResponse.notOk(e.getMessage(), EErrorCode.UNAUTHORIZED);
        }
    }

}
