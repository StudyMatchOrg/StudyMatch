package pt.upt.studymatch.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.dto.AccountResponse;
import pt.upt.studymatch.dto.LoginRequest;
import pt.upt.studymatch.dto.LoginResponse;
import pt.upt.studymatch.service.AccountService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return accountService.login(request);
    }

    @GetMapping("/me")
    public AccountResponse me() {
        return accountService.me();
    }
}
