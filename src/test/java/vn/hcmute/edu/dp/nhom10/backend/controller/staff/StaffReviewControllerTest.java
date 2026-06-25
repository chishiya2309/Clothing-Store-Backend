package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffDeleteReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.request.StaffReplyReviewRequest;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.PageResponse;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffReviewResponse;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffReviewService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class StaffReviewControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private StaffReviewService staffReviewService;

    @InjectMocks
    private StaffReviewController staffReviewController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(staffReviewController)
                .setControllerAdvice(new GlobalExceptionHandling())
                .build();
    }

    @Test
    public void testGetReviews() throws Exception {
        StaffReviewResponse response = StaffReviewResponse.builder()
                .id(1L)
                .content("Sản phẩm tốt")
                .isApproved(false)
                .build();

        PageResponse<StaffReviewResponse> pageResponse = new PageResponse<>(0, 10, 1, 1L, List.of(response));

        when(staffReviewService.getReviewsByTab(eq("PENDING"), anyInt(), anyInt())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/staff/reviews")
                        .param("tab", "PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách đánh giá thành công"))
                .andExpect(jsonPath("$.data.content[0].content").value("Sản phẩm tốt"));
    }

    @Test
    public void testApproveReview() throws Exception {
        StaffReviewResponse response = StaffReviewResponse.builder()
                .id(1L)
                .isApproved(true)
                .build();

        when(staffReviewService.approveReview(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/staff/reviews/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Duyệt đánh giá thành công"));
    }

    @Test
    public void testReplyToReview() throws Exception {
        StaffReplyReviewRequest request = new StaffReplyReviewRequest("Cảm ơn bạn!");
        StaffReviewResponse response = StaffReviewResponse.builder()
                .id(1L)
                .adminReply("Cảm ơn bạn!")
                .build();

        when(staffReviewService.replyToReview(eq(1L), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/staff/reviews/1/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Phản hồi đánh giá thành công"))
                .andExpect(jsonPath("$.data.adminReply").value("Cảm ơn bạn!"));
    }

    @Test
    public void testDeleteReview() throws Exception {
        StaffDeleteReviewRequest request = new StaffDeleteReviewRequest("Spam");
        StaffReviewResponse response = StaffReviewResponse.builder()
                .id(1L)
                .isActive(false)
                .deleteReason("Spam")
                .build();

        when(staffReviewService.deleteReview(eq(1L), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/staff/reviews/1/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Xóa đánh giá thành công"))
                .andExpect(jsonPath("$.data.deleteReason").value("Spam"));
    }
}
