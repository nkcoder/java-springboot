package org.nkcoder.grpc;

import com.timor.user.grpc.AuthProto;
import com.timor.user.grpc.AuthServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.nkcoder.dto.auth.AuthResponse;
import org.nkcoder.dto.auth.LoginRequest;
import org.nkcoder.dto.auth.RegisterRequest;
import org.nkcoder.enums.Role;
import org.nkcoder.service.AuthService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AuthGrpcService.class);

  private final AuthService authService;

  @Autowired
  public AuthGrpcService(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public void register(
      AuthProto.RegisterRequest request, StreamObserver<AuthProto.ApiResponse> responseObserver) {
    logger.info("Received registration request for email: {}", request.getEmail());

    if (request.getEmail().isEmpty()
        || request.getPassword().isEmpty()
        || request.getName().isEmpty()) {
      logger.error("Invalid registration request: email, password, and username must not be empty");
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("Email, password, and username must not be empty")
              .asRuntimeException());
      return;
    }

    RegisterRequest registerRequest =
        new RegisterRequest(
            request.getEmail(), request.getPassword(), request.getName(), Role.MEMBER);

    AuthResponse response = authService.register(registerRequest);

    logger.info("auth response: {}", response);

    var grpcResponse = GrpcMapper.toAuthResponse(response);
    var apiResponse =
        AuthProto.ApiResponse.newBuilder()
            .setMessage("User registered successfully")
            .setData(grpcResponse)
            .build();

    responseObserver.onNext(apiResponse);
    responseObserver.onCompleted();
  }

  @Override
  public void login(
      AuthProto.LoginRequest request, StreamObserver<AuthProto.ApiResponse> responseObserver) {
    logger.info("Received login request for email: {}", request.getEmail());

    if (request.getEmail().isEmpty() || request.getPassword().isEmpty()) {
      logger.error("Invalid login request: email and password must not be empty");
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("Email and password must not be empty")
              .asRuntimeException());
      return;
    }

    LoginRequest loginRequest = new LoginRequest(request.getEmail(), request.getPassword());
    AuthResponse response = authService.login(loginRequest);

    var grpcResponse = GrpcMapper.toAuthResponse(response);
    var apiResponse =
        AuthProto.ApiResponse.newBuilder()
            .setMessage("User logged in successfully")
            .setData(grpcResponse)
            .build();

    responseObserver.onNext(apiResponse);
    responseObserver.onCompleted();
  }
}
