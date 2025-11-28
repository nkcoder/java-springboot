package org.nkcoder.grpc;

import com.google.protobuf.Timestamp;
import com.timor.user.grpc.AuthProto;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.nkcoder.dto.auth.AuthResponse;
import org.nkcoder.dto.auth.AuthTokens;
import org.nkcoder.dto.user.UserResponse;

public class GrpcMapper {
    public static AuthProto.AuthResponse toAuthResponse(AuthResponse authResponse) {
        UserResponse user = authResponse.user();
        AuthTokens tokens = authResponse.tokens();

        AuthProto.User grpcUser = AuthProto.User.newBuilder()
                .setId(user.id().toString())
                .setEmail(user.email())
                .setName(user.name())
                .setLastLoginAt(toTimestamp(user.lastLoginAt()))
                .build();

        AuthProto.AuthToken grpcTokens = AuthProto.AuthToken.newBuilder()
                .setAccessToken(tokens.accessToken())
                .setRefreshToken(tokens.refreshToken())
                .build();

        return AuthProto.AuthResponse.newBuilder()
                .setUser(grpcUser)
                .setAuthToken(grpcTokens)
                .build();
    }

    public static Timestamp toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return Timestamp.getDefaultInstance();
        }
        Instant instant = dateTime.toInstant(ZoneOffset.UTC);

        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
