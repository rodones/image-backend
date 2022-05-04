package com.image.backend.controller;

import com.image.backend.dto.auth.LoginRequest;
import com.image.backend.dto.auth.LoginResponse;
import com.image.backend.service.AuthService;
import com.image.backend.util.annotations.ApiInformation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Api(tags = AuthController.TAG)
@RequestMapping(AuthController.TAG)
@ApiInformation(tag = AuthController.TAG, description = "Authentication related endpoints.")
@AllArgsConstructor
public class AuthController {

    protected static final String TAG = "auth";

    private final AuthService authService;

    @PreAuthorize("isAnonymous()")
    @PostMapping(
            path = "login",
            consumes = {
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    MediaType.MULTIPART_FORM_DATA_VALUE
            },
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(
            value = "Login a user using password or refresh token.",
            notes = "If grant_type is password, username, password, and client_secret fields are required. "
    )
    public ResponseEntity<LoginResponse> login(
            @Valid @ModelAttribute LoginRequest body,
            @NotNull BindingResult bindingResult
    ) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        var result = authService.login(body);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
}
