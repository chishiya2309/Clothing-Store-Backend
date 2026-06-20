package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateProfileRequest;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProfileValidationChainTest {

    private ProfileValidationHandler validationChain;

    @BeforeEach
    void setUp() {
        NameValidationHandler nameHandler = new NameValidationHandler();
        PhoneValidationHandler phoneHandler = new PhoneValidationHandler();
        DobValidationHandler dobHandler = new DobValidationHandler();

        nameHandler.setNext(phoneHandler).setNext(dobHandler);
        validationChain = nameHandler;
    }

    @Test
    void testValidProfile() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("Nguyen Van A")
                .phone("0987654321")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .build();

        assertDoesNotThrow(() -> validationChain.handle(request));
    }

    @Test
    void testInvalidNameBlank() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("   ")
                .build();

        assertThrows(InvalidDataException.class, () -> validationChain.handle(request));
    }

    @Test
    void testInvalidPhoneFormat() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .phone("abc1234567")
                .build();

        assertThrows(InvalidDataException.class, () -> validationChain.handle(request));
    }

    @Test
    void testInvalidDobFuture() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .dateOfBirth(LocalDate.now().plusDays(1))
                .build();

        assertThrows(InvalidDataException.class, () -> validationChain.handle(request));
    }
}
