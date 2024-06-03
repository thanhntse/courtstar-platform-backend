package com.example.courtstar.controller;

import com.example.courtstar.dto.request.*;
import com.example.courtstar.dto.response.AuthenticationResponse;
import com.example.courtstar.dto.response.IntrospectResponse;
import com.example.courtstar.services.AccountAuthentication;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    private AccountAuthentication authentication;

    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) throws JOSEException {
        AuthenticationResponse authenticationResponse =authentication.Authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .data(authenticationResponse)
                .code(1000)
                .message("Login Success")
                .build();
    }
    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws JOSEException, ParseException {
        var authenticated = authentication.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .data(authenticated)
                .code(1000)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws JOSEException, ParseException {
        authentication.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request) throws JOSEException, ParseException {
        AuthenticationResponse authenticationResponse =authentication.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .data(authenticationResponse)
                .code(1000)
                .build();
    }

}