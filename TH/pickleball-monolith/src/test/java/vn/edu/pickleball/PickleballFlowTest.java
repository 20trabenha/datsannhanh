package vn.edu.pickleball;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.edu.pickleball.booking.BookingExpiryJob;
import java.time.LocalDateTime;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PickleballFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired BookingExpiryJob expiryJob;

    private String body(Object value) throws Exception { return json.writeValueAsString(value); }
    private JsonNode parse(String value) throws Exception { return json.readTree(value); }

    @Test
    void bookingManualPaymentAndAdminDecisionFlow() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "bad!", "password", "abc", "phoneNumber", "123"))))
                .andExpect(status().isBadRequest());
        String registration = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "customer1", "password", "abc123", "phoneNumber", "0123456789"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String token = parse(registration).get("token").asText();
        String admin = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "admin", "password", "admin123"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String adminToken = parse(admin).get("token").asText();
        mvc.perform(put("/api/courts/1").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of(
                        "name", "Sân Pickleball 1", "pricePerHour", 120000, "description", "Sân trong nhà",
                        "categoryId", 1, "address", "Quận 1", "amenities", "Đèn chiếu sáng",
                        "openingTime", "09:00", "closingTime", "21:00"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.address").value("Quận 1"))
                .andExpect(jsonPath("$.openingTime").value("09:00:00"));
        LocalDateTime start = LocalDateTime.now().plusDays(3).toLocalDate().atTime(10, 0);
        mvc.perform(get("/api/bookings/quote").header("Authorization", "Bearer " + token)
                .param("courtId", "1").param("startAt", start.minusHours(2).toString())
                .param("endAt", start.minusHours(1).toString()))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/bookings/quote").header("Authorization", "Bearer " + token)
                .param("courtId", "1").param("startAt", start.toString())
                .param("endAt", start.plusMinutes(90).toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalAmount").value(180000))
                .andExpect(jsonPath("$.depositAmount").value(18000));
        mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of(
                        "courtId", 1, "startAt", start.toString(), "endAt", start.plusHours(1).toString(),
                        "customerName", "", "customerPhone", "123"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of(
                        "courtId", 1, "startAt", start.toString(), "endAt", start.plusHours(1).toString(),
                        "customerName", "Nguyễn Văn A", "customerPhone", "123"))))
                .andExpect(status().isBadRequest());
        String request = body(Map.of("courtId", 1, "startAt", start.toString(), "endAt", start.plusHours(1).toString(),
                "customerName", "Nguyễn Văn A", "customerPhone", "0123456789", "customerNote", "Mang vợt riêng"));
        String booking = mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.depositAmount").value(12000))
                .andExpect(jsonPath("$.customerName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.customerPhone").value("0123456789"))
                .andExpect(jsonPath("$.customerNote").value("Mang vợt riêng"))
                .andReturn().getResponse().getContentAsString();
        long bookingId = parse(booking).get("id").asLong();
        mvc.perform(get("/api/courts/1/availability").param("date", start.toLocalDate().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.hoursConfigured").value(true))
                .andExpect(jsonPath("$.booked.length()").value(1))
                .andExpect(jsonPath("$.booked[0].startAt").value(start + ":00"))
                .andExpect(jsonPath("$.slots[2].status").value("BOOKED"));
        mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isConflict());
        mvc.perform(get("/api/admin/bookings").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/bookings/" + bookingId + "/transfer-notice")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("type", "DEPOSIT"))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/bookings/" + bookingId + "/transfer-notice")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("type", "DEPOSIT"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.transferType").value("DEPOSIT"))
                .andExpect(jsonPath("$.orderCode").value(String.format("PB%06d", bookingId)));
        mvc.perform(post("/api/admin/bookings/" + bookingId + "/payment")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("action", "CONFIRM_DEPOSIT", "transactionReference", "REF-DEP-1"))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/bookings/" + bookingId + "/payment")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("action", "CONFIRM_DEPOSIT", "transactionReference", "REF-DEP-1"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.paymentStatus").value("DEPOSIT_PAID"));
        mvc.perform(post("/api/bookings/" + bookingId + "/transfer-notice")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("type", "FULL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.transferType").value("FULL"));
        mvc.perform(post("/api/admin/bookings/" + bookingId + "/payment")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("action", "CONFIRM_FULL", "transactionReference", "REF-FULL-1"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.paymentStatus").value("FULLY_PAID"));
        mvc.perform(get("/api/admin/bookings").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].customerName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.content[0].paymentStatus").value("FULLY_PAID"));
        mvc.perform(post("/api/admin/bookings/" + bookingId + "/decision")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("action", "CANCEL", "reason", "Mưa lớn, sân tạm đóng"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.refundStatus").value("PENDING"))
                .andExpect(jsonPath("$.refundAmount").value(120000));
        mvc.perform(post("/api/admin/bookings/" + bookingId + "/refund")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("action", "CONFIRM_REFUND", "transactionReference", "REF-REFUND-1"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.refundStatus").value("REFUNDED"));
        mvc.perform(get("/api/bookings/" + bookingId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.decisionReason").value("Mưa lớn, sân tạm đóng"));
        String rejectedBooking = mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long rejectedId = parse(rejectedBooking).get("id").asLong();
        mvc.perform(post("/api/admin/bookings/" + rejectedId + "/decision")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("action", "REJECT", "reason", "  "))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/admin/bookings/" + rejectedId + "/decision")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("action", "REJECT", "reason", "Sân đang bảo trì"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));
        mvc.perform(get("/api/bookings/" + rejectedId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.decisionReason").value("Sân đang bảo trì"));
        String expiringBooking = mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long expiringId = parse(expiringBooking).get("id").asLong();
        jdbc.update("update bookings set payment_deadline = ? where id = ?", LocalDateTime.now().minusMinutes(1), expiringId);
        mvc.perform(get("/api/bookings/" + expiringId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EXPIRED"));
        mvc.perform(get("/api/courts/1/availability").param("date", start.toLocalDate().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.booked.length()").value(0))
                .andExpect(jsonPath("$.slots[2].status").value("AVAILABLE"));
        mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isCreated());
    }

    @Test
    void reviewTimeoutCourtClosureAndAccountRecoveryFlow() throws Exception {
        String registration = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "customer2", "password", "abc12345", "phoneNumber", "0987654321"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String token = parse(registration).get("token").asText();
        long customerId = parse(registration).get("userId").asLong();
        String adminToken = parse(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "admin", "password", "admin123"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("token").asText();
        LocalDateTime start = LocalDateTime.now().plusDays(12).toLocalDate().atTime(10, 0);
        String request = body(Map.of("courtId", 1, "startAt", start.toString(), "endAt", start.plusHours(1).toString(),
                "customerName", "Khách thử", "customerPhone", "0987654321"));
        String booking = mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long bookingId = parse(booking).get("id").asLong();
        mvc.perform(post("/api/bookings/" + bookingId + "/transfer-notice")
                .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("type", "DEPOSIT"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.transferReviewDeadline").exists());
        jdbc.update("update bookings set transfer_review_deadline = ? where id = ?", LocalDateTime.now().minusMinutes(1), bookingId);
        expiryJob.expireOverdueBookings();
        mvc.perform(get("/api/bookings/" + bookingId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EXPIRED"))
                .andExpect(jsonPath("$.refundStatus").value("REVIEW_REQUIRED"));
        mvc.perform(get("/api/admin/bookings").header("Authorization", "Bearer " + adminToken)
                .param("date", start.toLocalDate().toString()).param("courtId", "1").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(bookingId));
        mvc.perform(post("/api/admin/bookings/" + bookingId + "/refund")
                .header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("action", "NO_PAYMENT_FOUND"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.refundStatus").value("NONE"));
        String block = mvc.perform(post("/api/admin/court-blocks").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("courtId", 1,
                        "startAt", start.toString(), "endAt", start.plusHours(1).toString(), "reason", "Bảo trì"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/courts/1/availability").param("date", start.toLocalDate().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.slots[2].status").value("CLOSED"));
        mvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isConflict());
        mvc.perform(delete("/api/admin/court-blocks/" + parse(block).get("id").asLong())
                .header("Authorization", "Bearer " + adminToken)).andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/change-password").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body(Map.of(
                        "currentPassword", "abc12345", "newPassword", "newpass123"))))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/bookings/" + bookingId).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "customer2", "password", "abc12345"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "customer2", "password", "newpass123"))))
                .andExpect(status().isOk());
        String resetCode = parse(mvc.perform(post("/api/admin/customers/" + customerId + "/reset-code")
                .header("Authorization", "Bearer " + adminToken)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("resetCode").asText();
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "customer2", "resetCode", resetCode, "newPassword", "resetpass123"))))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "customer2", "resetCode", resetCode, "newPassword", "resetpass456"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "customer2", "password", "resetpass123"))))
                .andExpect(status().isOk());
    }

    @Test
    void repeatedWrongPasswordsTemporarilyLockAccount() throws Exception {
        String registration = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "customer3", "password", "correct123", "phoneNumber", "0912345678"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = parse(registration).get("userId").asLong();
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(body(Map.of("username", "customer3", "password", "wrong"))))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "customer3", "password", "correct123"))))
                .andExpect(status().isBadRequest());
        String adminToken = parse(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "admin", "password", "admin123"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("token").asText();
        String code = parse(mvc.perform(post("/api/admin/customers/" + id + "/reset-code")
                .header("Authorization", "Bearer " + adminToken)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("resetCode").asText();
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "customer3", "resetCode", code, "newPassword", "changed123"))))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("username", "customer3", "password", "changed123"))))
                .andExpect(status().isOk());
    }
}
