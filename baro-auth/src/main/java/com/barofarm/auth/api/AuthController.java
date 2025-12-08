package com.barofarm.auth.api;

import com.barofarm.auth.api.dto.LoginRequest;
import com.barofarm.auth.api.dto.MeResponse;
import com.barofarm.auth.api.dto.SignupRequest;
import com.barofarm.auth.application.AuthService;
import com.barofarm.auth.application.usecase.LoginResult;
import com.barofarm.auth.application.usecase.SignUpResult;
import com.barofarm.auth.infrastructure.security.AuthUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Sign up / login / profile")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @Operation(summary = "Sign up", description = "Create user with email/password/name/phone/marketing")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Email already exists")})
    public ResponseEntity<SignUpResult> signup(@RequestBody SignupRequest request) {
        var response = authService.signUp(request.toServiceRequest());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login with email/password and issue tokens")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Login success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")})
    public ResponseEntity<LoginResult> login(@RequestBody LoginRequest request) {
        var response = authService.login(request.toServiceRequest());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "My profile", description = "Returns current authenticated user info")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")})
    public ResponseEntity<MeResponse> getCurrentUser(@AuthenticationPrincipal AuthUserPrincipal principal) {

        MeResponse response = new MeResponse(principal.getUserId(), principal.getUsername(), principal.getRole());

        return ResponseEntity.ok(response);
    }
}
