package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.profile;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateProfileRequest;

public abstract class ProfileValidationHandler {
    private ProfileValidationHandler next;

    public ProfileValidationHandler setNext(ProfileValidationHandler next) {
        this.next = next;
        return next;
    }

    public void handle(UpdateProfileRequest request) {
        validate(request);
        if (next != null) {
            next.handle(request);
        }
    }

    protected abstract void validate(UpdateProfileRequest request);
}
