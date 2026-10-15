package vn.hcmute.edu.dp.nhom10.backend.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void registerRequest_validPassword_passesValidation() {
        RegisterRequest request = new RegisterRequest("user@example.com", "Password123", "Nguyễn Thái Bảo");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void registerRequest_passwordWithoutLowercase_failsValidation() {
        RegisterRequest request = new RegisterRequest("user@example.com", "AB123456", "Nguyễn Thái Bảo");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        boolean hasPasswordError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")
                        && v.getMessage().contains("chữ thường"));
        assertTrue(hasPasswordError);
    }

    @Test
    void registerRequest_passwordWithoutUppercase_failsValidation() {
        RegisterRequest request = new RegisterRequest("user@example.com", "ab123456", "Nguyễn Thái Bảo");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        boolean hasPasswordError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertTrue(hasPasswordError);
    }

    @Test
    void registerRequest_passwordWithoutNumber_failsValidation() {
        RegisterRequest request = new RegisterRequest("user@example.com", "Abcdefgh", "Nguyễn Thái Bảo");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        boolean hasPasswordError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertTrue(hasPasswordError);
    }

    @Test
    void registerRequest_passwordTooShort_failsValidation() {
        RegisterRequest request = new RegisterRequest("user@example.com", "Ab123", "Nguyễn Thái Bảo");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        boolean hasPasswordError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertTrue(hasPasswordError);
    }
}
