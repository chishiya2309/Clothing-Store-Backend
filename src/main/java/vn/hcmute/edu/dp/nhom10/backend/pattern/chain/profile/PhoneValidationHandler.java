package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateProfileRequest;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

import java.util.regex.Pattern;

@Component
public class PhoneValidationHandler extends ProfileValidationHandler {

    // Validates a phone number, assuming 10 to 15 digits possibly starting with +
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    @Override
    protected void validate(UpdateProfileRequest request) {
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(request.getPhone().trim()).matches()) {
                throw new InvalidDataException("Số điện thoại không hợp lệ");
            }
        }
    }
}
