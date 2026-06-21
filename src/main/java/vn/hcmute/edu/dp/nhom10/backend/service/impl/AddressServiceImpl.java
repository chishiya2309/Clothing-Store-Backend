package vn.hcmute.edu.dp.nhom10.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.AddressRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AddressResponse;
import vn.hcmute.edu.dp.nhom10.backend.entity.Address;
import vn.hcmute.edu.dp.nhom10.backend.entity.User;
import vn.hcmute.edu.dp.nhom10.backend.exception.InvalidDataException;
import vn.hcmute.edu.dp.nhom10.backend.exception.ResourceNotFoundException;
import vn.hcmute.edu.dp.nhom10.backend.repository.AddressRepository;
import vn.hcmute.edu.dp.nhom10.backend.repository.UserRepository;
import vn.hcmute.edu.dp.nhom10.backend.service.AddressService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "ADDRESS-SERVICE")
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressResponse> getAddresses(String email) {
        User user = findUserByEmail(email);
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId())
                .stream()
                .map(AddressResponse::fromEntity)
                .toList();
    }

    @Override
    public AddressResponse getAddress(String email, Long addressId) {
        User user = findUserByEmail(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));
        return AddressResponse.fromEntity(address);
    }

    @Override
    @Transactional
    public AddressResponse createAddress(String email, AddressRequest request) {
        User user = findUserByEmail(email);

        boolean isFirstAddress = addressRepository.countByUserId(user.getId()) == 0;
        boolean shouldBeDefault = Boolean.TRUE.equals(request.isDefault()) || isFirstAddress;

        if (shouldBeDefault) {
            addressRepository.clearDefaultByUserId(user.getId());
        }

        Address address = Address.builder()
                .user(user)
                .recipientName(request.recipientName())
                .phone(request.phone())
                .province(request.province())
                .district(request.district())
                .ward(request.ward())
                .streetAddress(request.streetAddress())
                .isDefault(shouldBeDefault)
                .build();

        Address saved = addressRepository.save(address);
        log.info("Created address id={} for user={}", saved.getId(), email);
        return AddressResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(String email, Long addressId, AddressRequest request) {
        User user = findUserByEmail(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        boolean shouldBeDefault = Boolean.TRUE.equals(request.isDefault());
        if (shouldBeDefault && !address.getIsDefault()) {
            addressRepository.clearDefaultByUserId(user.getId());
        }

        address.setRecipientName(request.recipientName());
        address.setPhone(request.phone());
        address.setProvince(request.province());
        address.setDistrict(request.district());
        address.setWard(request.ward());
        address.setStreetAddress(request.streetAddress());
        address.setIsDefault(shouldBeDefault || address.getIsDefault());

        Address saved = addressRepository.save(address);
        log.info("Updated address id={} for user={}", saved.getId(), email);
        return AddressResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = findUserByEmail(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        if (address.getIsDefault()) {
            long count = addressRepository.countByUserId(user.getId());
            if (count <= 1) {
                throw new InvalidDataException("Không thể xóa địa chỉ mặc định duy nhất");
            }
        }

        addressRepository.delete(address);
        log.info("Deleted address id={} for user={}", addressId, email);

        // If we deleted the default, promote the next one
        if (address.getIsDefault()) {
            List<Address> remaining = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
                log.info("Promoted address id={} to default for user={}", newDefault.getId(), email);
            }
        }
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(String email, Long addressId) {
        User user = findUserByEmail(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        addressRepository.clearDefaultByUserId(user.getId());
        address.setIsDefault(true);
        Address saved = addressRepository.save(address);
        log.info("Set default address id={} for user={}", saved.getId(), email);
        return AddressResponse.fromEntity(saved);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }
}
