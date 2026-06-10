package vn.hcmute.edu.dp.nhom10.backend.service;

import vn.hcmute.edu.dp.nhom10.backend.dto.request.ApplyVoucherRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.CreateVoucherRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.UpdateVoucherRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.AppliedVoucherResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VoucherResponse;

import java.util.List;

public interface VoucherService {
    VoucherResponse create(CreateVoucherRequest request);

    VoucherResponse update(Long id, UpdateVoucherRequest request);

    VoucherResponse getById(Long id);

    List<VoucherResponse> getAll();

    void deleteOrDeactivate(Long id);

    AppliedVoucherResponse apply(ApplyVoucherRequest request, String customerEmail);
}
