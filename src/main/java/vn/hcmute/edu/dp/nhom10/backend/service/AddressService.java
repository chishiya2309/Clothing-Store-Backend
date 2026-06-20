package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.AddressRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getAddresses(String email);
    AddressResponse getAddress(String email, Long addressId);
    AddressResponse createAddress(String email, AddressRequest request);
    AddressResponse updateAddress(String email, Long addressId, AddressRequest request);
    void deleteAddress(String email, Long addressId);
    AddressResponse setDefaultAddress(String email, Long addressId);
}
