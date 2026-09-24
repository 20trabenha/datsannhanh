package vn.edu.pickleball.common;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Configuration
public class BookingStatusMigration {
    @Bean
    CommandLineRunner widenExistingBookingStatusConstraint(JdbcTemplate jdbc) {
        return args -> {
            String database = jdbc.execute((ConnectionCallback<String>) connection ->
                    connection.getMetaData().getDatabaseProductName());
            if ("H2".equals(database)) {
            List<Map<String, Object>> constraints = jdbc.queryForList("""
                    SELECT tc.CONSTRAINT_NAME, cc.CHECK_CLAUSE
                    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                    JOIN INFORMATION_SCHEMA.CHECK_CONSTRAINTS cc
                      ON tc.CONSTRAINT_CATALOG = cc.CONSTRAINT_CATALOG
                     AND tc.CONSTRAINT_SCHEMA = cc.CONSTRAINT_SCHEMA
                     AND tc.CONSTRAINT_NAME = cc.CONSTRAINT_NAME
                    WHERE tc.TABLE_NAME = 'BOOKINGS' AND tc.CONSTRAINT_TYPE = 'CHECK'
                    """);
            for (Map<String, Object> constraint : constraints) {
                String clause = String.valueOf(constraint.get("CHECK_CLAUSE")).toUpperCase();
                if (clause.contains("STATUS") && !clause.contains("REJECTED")) {
                    String name = String.valueOf(constraint.get("CONSTRAINT_NAME")).replace("\"", "\"\"");
                    jdbc.execute("ALTER TABLE BOOKINGS DROP CONSTRAINT \"" + name + "\"");
                    jdbc.execute("ALTER TABLE BOOKINGS ADD CONSTRAINT \"" + name + "\" " +
                            "CHECK (STATUS IN ('PENDING_PAYMENT','CONFIRMED','CANCELLED','EXPIRED','REJECTED'))");
                }
            }
            }
            // Preserve older transfer notices, then give each a bounded review window.
            List<Map<String, Object>> oldNotices = jdbc.queryForList("SELECT id, start_at, transfer_submitted_at " +
                    "FROM bookings WHERE transfer_submitted_at IS NOT NULL AND transfer_review_deadline IS NULL " +
                    "AND status IN ('PENDING_PAYMENT','CONFIRMED')");
            for (Map<String, Object> notice : oldNotices) {
                LocalDateTime submitted = ((Timestamp) notice.get("TRANSFER_SUBMITTED_AT")).toLocalDateTime();
                LocalDateTime start = ((Timestamp) notice.get("START_AT")).toLocalDateTime();
                LocalDateTime deadline = submitted.plusHours(2);
                if (start.isBefore(deadline)) deadline = start;
                jdbc.update("UPDATE bookings SET transfer_review_deadline = ? WHERE id = ?", deadline, notice.get("ID"));
            }
            // A reported transfer awaits manual review, even after the initial payment deadline.
            jdbc.update("UPDATE bookings SET status = 'EXPIRED' WHERE status = 'PENDING_PAYMENT' " +
                    "AND transfer_submitted_at IS NULL AND payment_deadline IS NOT NULL AND payment_deadline <= CURRENT_TIMESTAMP");
        };
    }
}
