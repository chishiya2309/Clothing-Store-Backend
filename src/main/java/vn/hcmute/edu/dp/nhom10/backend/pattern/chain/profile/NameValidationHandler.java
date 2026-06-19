package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile;

import org.springframework.stereotype.Component;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateProfileRequest;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;

@Component
public class NameValidationHandler extends ProfileValidationHandler {

    @Override
    protected void validate(UpdateProfileRequest request) {
        if (request.getFullName() != null) {
            String name = request.getFullName().trim();
            if (name.isEmpty()) {
                throw new InvalidDataException("Họ tên không được để trống");
            }
            if (name.length() > 100) {
                throw new InvalidDataException("Họ tên không được vượt quá 100 ký tự");
            }
        }
    }
}
