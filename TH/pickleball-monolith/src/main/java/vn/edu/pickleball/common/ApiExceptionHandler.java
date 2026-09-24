package vn.edu.pickleball.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return response(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, String>> notFound(NoSuchElementException e) {
        return response(HttpStatus.NOT_FOUND, e.getMessage());
    }
    @ExceptionHandler({IllegalStateException.class, DataIntegrityViolationException.class})
    ResponseEntity<Map<String, String>> conflict(Exception e) {
        return response(HttpStatus.CONFLICT, e instanceof IllegalStateException ? e.getMessage() : "Dữ liệu đang được sử dụng.");
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, String>> forbidden(AccessDeniedException e) {
        return response(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, String>> tooLarge(MaxUploadSizeExceededException e) {
        return response(HttpStatus.BAD_REQUEST, "Ảnh không được lớn hơn 5 MB.");
    }
    private ResponseEntity<Map<String, String>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
