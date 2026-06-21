package vn.hcmute.edu.dp.nhom10.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.edu.dp.nhom10.backend.dto.payment.MomoIpnRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.MomoReturnResponseDTO;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.MomoIpnService;
import vn.hcmute.edu.dp.nhom10.backend.service.impl.MomoReturnService;

@RestController
@RequestMapping("/api/payments/momo")
@RequiredArgsConstructor
public class MomoCallbackController {

    private final MomoReturnService returnService;
    private final MomoIpnService ipnService;

    @GetMapping("/return")
    public ResponseEntity<MomoReturnResponseDTO> handleReturn(
            @RequestParam MultiValueMap<String, String> parameters
    ) {
        return ResponseEntity.ok(returnService.handleReturn(parameters));
    }

    @PostMapping("/ipn")
    public ResponseEntity<Void> handleIpn(@RequestBody MomoIpnRequest request) {
        if (!ipnService.handleIpn(request)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.noContent().build();
    }
}
