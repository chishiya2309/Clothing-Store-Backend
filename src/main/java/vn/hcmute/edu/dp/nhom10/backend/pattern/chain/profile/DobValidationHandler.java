package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateProfileRequest;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.time.LocalDate;

@Component
public class DobValidationHandler extends ProfileValidationHandler {

    @Override
    protected void validate(UpdateProfileRequest request) {
        if (request.getDateOfBirth() != null) {
            if (request.getDateOfBirth().isAfter(LocalDate.now())) {
                throw new InvalidDataException("Ngày sinh không được ở trong tương lai");
            }
        }
    }
}
