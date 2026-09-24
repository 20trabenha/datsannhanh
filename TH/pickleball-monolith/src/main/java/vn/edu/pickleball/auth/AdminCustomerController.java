package vn.edu.pickleball.auth;

import org.springframework.web.bind.annotation.*;
import vn.edu.pickleball.auth.AuthDtos.CustomerResponse;
import vn.edu.pickleball.auth.AuthDtos.ResetCodeResponse;
import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {
    private final AuthService service;
    public AdminCustomerController(AuthService service) { this.service = service; }
    @GetMapping public List<CustomerResponse> list() { return service.customers(); }
    @PostMapping("/{id}/reset-code") public ResetCodeResponse issueResetCode(@PathVariable Long id) {
        return service.issueResetCode(id);
    }
}
