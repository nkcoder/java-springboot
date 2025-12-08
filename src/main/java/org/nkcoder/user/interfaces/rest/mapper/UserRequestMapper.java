package org.nkcoder.user.interfaces.rest.mapper;

import java.util.UUID;
import org.nkcoder.user.application.dto.command.AdminResetPasswordCommand;
import org.nkcoder.user.application.dto.command.AdminUpdateUserCommand;
import org.nkcoder.user.application.dto.command.ChangePasswordCommand;
import org.nkcoder.user.application.dto.command.UpdateProfileCommand;
import org.nkcoder.user.interfaces.rest.request.AdminResetPasswordRequest;
import org.nkcoder.user.interfaces.rest.request.AdminUpdateUserRequest;
import org.nkcoder.user.interfaces.rest.request.ChangePasswordRequest;
import org.nkcoder.user.interfaces.rest.request.UpdateProfileRequest;
import org.springframework.stereotype.Component;

/** Mapper for converting REST requests to application commands. */
@Component
public class UserRequestMapper {

    public UpdateProfileCommand toCommand(UUID userId, UpdateProfileRequest request) {
        return new UpdateProfileCommand(userId, request.name());
    }

    public ChangePasswordCommand toCommand(UUID userId, ChangePasswordRequest request) {
        return new ChangePasswordCommand(userId, request.currentPassword(), request.newPassword());
    }

    public AdminUpdateUserCommand toCommand(UUID targetUserId, AdminUpdateUserRequest request) {
        return new AdminUpdateUserCommand(targetUserId, request.name(), request.email());
    }

    public AdminResetPasswordCommand toCommand(UUID targetUserId, AdminResetPasswordRequest request) {
        return new AdminResetPasswordCommand(targetUserId, request.newPassword());
    }
}
