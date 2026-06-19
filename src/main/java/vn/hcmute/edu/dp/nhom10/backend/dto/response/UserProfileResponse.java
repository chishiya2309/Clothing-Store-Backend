package vn.hcmute.edu.dp.nhom10.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hcmute.edu.dp.nhom10.backend.enums.GenderType;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse implements Serializable {
    private String fullName;
    private String email;
    private String phone;
    private GenderType gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private Integer loyaltyPoints;
    private String membershipTier;
}
