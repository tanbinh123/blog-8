package com.github.nkonev.controllers;

import com.github.nkonev.ApiConstants;
import com.github.nkonev.Constants;
import com.github.nkonev.entity.jpa.PasswordResetToken;
import com.github.nkonev.entity.jpa.UserAccount;
import com.github.nkonev.exception.PasswordResetTokenNotFoundException;
import com.github.nkonev.repo.jpa.PasswordResetTokenRepository;
import com.github.nkonev.repo.jpa.UserAccountRepository;
import com.github.nkonev.services.EmailService;
import com.github.nkonev.utils.TimeUtil;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@Transactional
public class PasswordResetController {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${custom.password-reset.token.ttl-minutes}")
    private long passwordResetTokenTtlMinutes;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetController.class);

    /**
     * https://www.owasp.org/index.php/Forgot_Password_Cheat_Sheet
     * https://stackoverflow.com/questions/1102781/best-way-for-a-forgot-password-implementation/1102817#1102817
     * Yes, if your email is stolen you can lost your account
     * @param email
     */
    @PostMapping(value = Constants.Uls.API+Constants.Uls.REQUEST_PASSWORD_RESET)
    public void requestPasswordReset(String email) {
        UUID uuid = UUID.randomUUID();

        Optional<UserAccount> userAccountOptional = userAccountRepository.findByEmail(email);
        if (!userAccountOptional.isPresent()) {
            LOGGER.warn("Skipping sent request password reset email {} because this email is not found", email);
            return; // we care for for email leak
        }
        UserAccount userAccount = userAccountOptional.get();

        Duration ttl = Duration.ofMinutes(passwordResetTokenTtlMinutes);
        LocalDateTime expireTime = TimeUtil.getNowUTC().plus(ttl);

        PasswordResetToken passwordResetToken = new PasswordResetToken(uuid, userAccount.getId(), expireTime);

        passwordResetToken = passwordResetTokenRepository.save(passwordResetToken);

        emailService.sendPasswordResetToken(userAccount.getEmail(), passwordResetToken, userAccount.getUsername());
    }

    public static class PasswordResetDto {
        @NotNull
        private UUID passwordResetToken;

        @Size(min = ApiConstants.MIN_PASSWORD_LENGTH, max = ApiConstants.MAX_PASSWORD_LENGTH)
        @NotEmpty
        private String newPassword;

        public PasswordResetDto() { }

        public PasswordResetDto(UUID passwordResetToken, String newPassword) {
            this.passwordResetToken = passwordResetToken;
            this.newPassword = newPassword;
        }

        public UUID getPasswordResetToken() {
            return passwordResetToken;
        }

        public void setPasswordResetToken(UUID passwordResetToken) {
            this.passwordResetToken = passwordResetToken;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    @PostMapping(value = Constants.Uls.API + Constants.Uls.PASSWORD_RESET_SET_NEW)
    public void resetPassword(@RequestBody @Valid PasswordResetDto passwordResetDto) {

        // webpage parses token uuid from URL
        // .. and js sends this request

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findOne(passwordResetDto.getPasswordResetToken());
        if (passwordResetToken == null) {
            throw new PasswordResetTokenNotFoundException("password reset token not found");
        }
        if (TimeUtil.getNowUTC().isAfter(passwordResetToken.getExpiredAt()) ) {
            throw new PasswordResetTokenNotFoundException("password reset token is expired");
        }
        Optional<UserAccount> userAccountOptional = userAccountRepository.findById(passwordResetToken.getUserId());
        if(!userAccountOptional.isPresent()) {
            return;
        }

        UserAccount userAccount = userAccountOptional.get();

        userAccount.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));

        return;
    }
}