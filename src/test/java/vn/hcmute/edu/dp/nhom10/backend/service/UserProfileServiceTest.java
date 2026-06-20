package vn.hcmute.edu.dp.nhom10.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateProfileRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.UserProfileResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.enums.GenderType;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile.DobValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile.NameValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile.PhoneValidationHandler;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.UserProfileServiceImpl;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NameValidationHandler nameValidationHandler;
    
    @Mock
    private PhoneValidationHandler phoneValidationHandler;
    
    @Mock
    private DobValidationHandler dobValidationHandler;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Old Name")
                .phone("0123456789")
                .gender(GenderType.male)
                .loyaltyPoints(100)
                .build();
    }

    @Test
    void testGetUserProfile_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        UserProfileResponse response = userProfileService.getUserProfile("test@example.com");

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Old Name", response.getFullName());
        assertEquals(100, response.getLoyaltyPoints());
    }

    @Test
    void testGetUserProfile_NotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userProfileService.getUserProfile("notfound@example.com"));
    }

    @Test
    void testUpdateUserProfile_Success() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("New Name")
                .phone("0987654321")
                .gender(GenderType.female)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .build();

        // Mock chain setup
        when(nameValidationHandler.setNext(phoneValidationHandler)).thenReturn(phoneValidationHandler);
        when(phoneValidationHandler.setNext(dobValidationHandler)).thenReturn(dobValidationHandler);
        doNothing().when(nameValidationHandler).handle(request);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserProfileResponse response = userProfileService.updateUserProfile("test@example.com", request);

        assertNotNull(response);
        assertEquals("New Name", response.getFullName());
        assertEquals("0987654321", response.getPhone());
        assertEquals(GenderType.female, response.getGender());
        assertEquals(LocalDate.of(2000, 1, 1), response.getDateOfBirth());
        verify(userRepository, times(1)).save(mockUser);
    }
}
