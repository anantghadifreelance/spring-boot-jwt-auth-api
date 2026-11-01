package com.anantghadi.jwtauth.api;

import com.anantghadi.jwtauth.user.User;
import com.anantghadi.jwtauth.user.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record UserView(Long id, String name, String email, String role) {
        static UserView of(User user) {
            return new UserView(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
        }
    }

    /** Any authenticated user. */
    @GetMapping("/api/users/me")
    public UserView me(@AuthenticationPrincipal User user) {
        return UserView.of(user);
    }

    /** ADMIN only (secured in SecurityConfig). */
    @GetMapping("/api/admin/users")
    public List<UserView> allUsers() {
        return userRepository.findAll().stream().map(UserView::of).toList();
    }
}
