package vn.hcmute.edu.dp.nhom10.backend.dto.checkout;

public record AddressSnapshot(
        String recipientName,
        String phone,
        String province,
        String district,
        String ward,
        String streetAddress
) {
}
