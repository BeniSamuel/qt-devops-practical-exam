package rw.qtglobal.diary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import rw.qtglobal.diary.entity.User;
import rw.qtglobal.diary.service.AdminService;
import rw.qtglobal.diary.security.CurrentUser;
import rw.qtglobal.diary.payload.AdminUserDto;
import rw.qtglobal.diary.payload.RoleRequest;
import rw.qtglobal.diary.payload.UserDto;

/** Admin only: enforced by the URL rule in SecurityConfig and by method security below. */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService admin;

    public AdminController(AdminService admin) {
        this.admin = admin;
    }

    @GetMapping("/users")
    public List<AdminUserDto> users() {
        return admin.listUsers();
    }

    @PatchMapping("/users/{id}/role")
    public UserDto changeRole(@CurrentUser User me, @PathVariable Long id,
                              @Valid @RequestBody RoleRequest req) {
        return admin.changeRole(me, id, req.role());
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser User me, @PathVariable Long id) {
        admin.deleteUser(me, id);
    }
}
