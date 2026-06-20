package vn.hcmute.edu.dp.nhom10.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VnPayIpnResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.VnPayReturnResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VnPayIpnService;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.VnPayReturnService;

@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
public class VnPayCallbackController {

    private final VnPayReturnService returnService;
    private final VnPayIpnService ipnService;

    @GetMapping("/return")
    public ResponseEntity<VnPayReturnResponseDTO> handleReturn(
            @RequestParam MultiValueMap<String, String> parameters
    ) {
        return ResponseEntity.ok(returnService.handleReturn(parameters));
    }

    @GetMapping("/ipn")
    public ResponseEntity<VnPayIpnResponse> handleIpn(
            @RequestParam MultiValueMap<String, String> parameters
    ) {
        return ResponseEntity.ok(ipnService.handleIpn(parameters));
    }
}
