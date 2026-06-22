package vn.hcmute.edu.dp.nhom10.backend.controller.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import vn.hcmute.edu.dp.nhom10.backend.dto.response.StaffOrderDetailResponse;
import vn.hcmute.edu.dp.nhom10.backend.enums.OrderStatus;
import vn.hcmute.edu.dp.nhom10.backend.exception.GlobalExceptionHandling;
import vn.hcmute.edu.dp.nhom10.backend.security.AuthenticatedUserProvider;
import vn.hcmute.edu.dp.nhom10.backend.service.StaffOrderService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = StaffOrderControllerSecurityTest.SecurityTestConfiguration.class)
class StaffOrderControllerSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private StaffOrderService staffOrderService;

    @Autowired
    private AuthenticatedUserProvider authenticatedUserProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(staffOrderService, authenticatedUserProvider);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "staff@test.com", roles = "STAFF")
    void staffCanConfirmOrder() throws Exception {
        when(authenticatedUserProvider.getCurrentUserId(any(Authentication.class))).thenReturn(5L);
        when(staffOrderService.confirmOrder("ORD-1", 5L)).thenReturn(StaffOrderDetailResponse.builder()
                .orderCode("ORD-1")
                .status(OrderStatus.processing)
                .items(List.of())
                .timeline(List.of())
                .build());

        mockMvc.perform(patch("/api/staff/orders/ORD-1/confirm"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void customerCannotConfirmOrder() throws Exception {
        mockMvc.perform(patch("/api/staff/orders/ORD-1/confirm"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotConfirmOrder() throws Exception {
        mockMvc.perform(patch("/api/staff/orders/ORD-1/confirm"))
                .andExpect(status().isUnauthorized());
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @EnableMethodSecurity
    static class SecurityTestConfiguration {

        @Bean
        StaffOrderController staffOrderController(StaffOrderService staffOrderService,
                                                  AuthenticatedUserProvider authenticatedUserProvider) {
            return new StaffOrderController(staffOrderService, authenticatedUserProvider);
        }

        @Bean
        StaffOrderService staffOrderService() {
            return Mockito.mock(StaffOrderService.class);
        }

        @Bean
        AuthenticatedUserProvider authenticatedUserProvider() {
            return Mockito.mock(AuthenticatedUserProvider.class);
        }

        @Bean
        GlobalExceptionHandling globalExceptionHandling() {
            return new GlobalExceptionHandling();
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .logout(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(exception -> exception.authenticationEntryPoint(
                            (request, response, authException) -> response.sendError(401)));

            return http.build();
        }
    }
}
