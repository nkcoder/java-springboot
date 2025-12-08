package org.nkcoder.auth.interfaces.grpc;

import org.nkcoder.auth.application.dto.response.AuthResult;
import org.nkcoder.generated.grpc.AuthProto;
import org.springframework.stereotype.Component;

/** Mapper between gRPC proto messages and application DTOs. */
@Component
public class GrpcAuthMapper {

  public AuthProto.AuthResponse toAuthResponse(AuthResult result) {
    AuthProto.User grpcUser =
        AuthProto.User.newBuilder()
            .setId(result.userId().toString())
            .setEmail(result.email())
            .setName("") // Name not available in AuthResult
            .build();

    AuthProto.AuthToken grpcTokens =
        AuthProto.AuthToken.newBuilder()
            .setAccessToken(result.accessToken())
            .setRefreshToken(result.refreshToken())
            .build();

    return AuthProto.AuthResponse.newBuilder().setUser(grpcUser).setAuthToken(grpcTokens).build();
  }
}
