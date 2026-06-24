package vn.hcmute.edu.dp.nhom10.backend.service;

public record LoyaltyPointAwardResult(
        int awardedPoints,
        Integer previousPoints,
        Integer resultingPoints,
        String previousMembershipTier,
        String resultingMembershipTier,
        boolean membershipTierChanged
) {
}
