package org.nkcoder.auth.interfaces.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.nkcoder.auth.application.dto.command.LoginCommand;
import org.nkcoder.auth.application.dto.command.RegisterCommand;
import org.nkcoder.auth.application.dto.response.AuthResult;
import org.nkcoder.auth.application.service.AuthApplicationService;
import org.nkcoder.auth.domain.model.AuthRole;
import org.nkcoder.generated.grpc.AuthProto;
import org.nkcoder.generated.grpc.AuthServiceGrpc;
import org.nkcoder.shared.kernel.exception.AuthenticationException;
import org.nkcoder.shared.kernel.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** gRPC service for authentication operations. */
@GrpcService
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(AuthGrpcService.class);

    private final AuthApplicationService authService;
    private final GrpcAuthMapper mapper;

    public AuthGrpcService(AuthApplicationService authService, GrpcAuthMapper mapper) {
        this.authService = authService;
        this.mapper = mapper;
    }

    @Override
    public void register(AuthProto.RegisterRequest request, StreamObserver<AuthProto.ApiResponse> responseObserver) {
        logger.info("Received gRPC registration request for email: {}", request.getEmail());

        if (request.getEmail().isEmpty()
                || request.getPassword().isEmpty()
                || request.getName().isEmpty()) {
            logger.error("Invalid registration request: email, password, and name must not be empty");
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Email, password, and name must not be empty")
                    .asRuntimeException());
            return;
        }

        try {
            RegisterCommand command =
                    new RegisterCommand(request.getEmail(), request.getPassword(), request.getName(), AuthRole.MEMBER);

            AuthResult result = authService.register(command);

            AuthProto.ApiResponse apiResponse = AuthProto.ApiResponse.newBuilder()
                    .setMessage("User registered successfully")
                    .setData(mapper.toAuthResponse(result))
                    .build();

            responseObserver.onNext(apiResponse);
            responseObserver.onCompleted();

        } catch (ValidationException e) {
            logger.error("Registration validation error: {}", e.getMessage());
            responseObserver.onError(
                    Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    @Override
    public void login(AuthProto.LoginRequest request, StreamObserver<AuthProto.ApiResponse> responseObserver) {
        logger.info("Received gRPC login request for email: {}", request.getEmail());

        if (request.getEmail().isEmpty() || request.getPassword().isEmpty()) {
            logger.error("Invalid login request: email and password must not be empty");
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Email and password must not be empty")
                    .asRuntimeException());
            return;
        }

        try {
            LoginCommand command = new LoginCommand(request.getEmail(), request.getPassword());

            AuthResult result = authService.login(command);

            AuthProto.ApiResponse apiResponse = AuthProto.ApiResponse.newBuilder()
                    .setMessage("User logged in successfully")
                    .setData(mapper.toAuthResponse(result))
                    .build();

            responseObserver.onNext(apiResponse);
            responseObserver.onCompleted();

        } catch (AuthenticationException e) {
            logger.error("Login authentication error: {}", e.getMessage());
            responseObserver.onError(
                    Status.UNAUTHENTICATED.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }
}
