package io.kettil.faas.authz.server.mock;

import com.google.rpc.Status;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class TestEnvoyExtAuthzService implements Closeable {
    private static final Integer GRANTED = 0;
    private static final Integer DENIED = 7;

    private final int gRpcPort;

    private Server server;

    public TestEnvoyExtAuthzService(@Value("${grpc.port}") int gRpcPort) {
        this.gRpcPort = gRpcPort;
    }

    @PostConstruct
    public void start() throws IOException {
        server = ServerBuilder.forPort(gRpcPort)
            .addService(new io.envoyproxy.envoy.service.auth.v2.AuthorizationGrpc.AuthorizationImplBase() {
                @SneakyThrows
                @Override
                public void check(io.envoyproxy.envoy.service.auth.v2.CheckRequest request, StreamObserver<io.envoyproxy.envoy.service.auth.v2.CheckResponse> responseObserver) {
                    log.info("v2.CheckRequest: {}", request);

                    int code;

                    String objectNamespace = request.getAttributes().getContextExtensionsOrThrow("namespace_object");
                    String serviceNamespace = request.getAttributes().getContextExtensionsOrThrow("namespace_service");
                    String servicePath = request.getAttributes().getContextExtensionsOrThrow("service_path");
                    String relation = request.getAttributes().getContextExtensionsOrThrow("relation");
                    String objectIdPtr = request.getAttributes().getContextExtensionsMap().get("objectid_ptr");

                    Map<String, String> headers = request.getAttributes().getRequest().getHttp().getHeadersMap();
                    String token = headers.get("authorization");

                    String method = request.getAttributes().getRequest().getHttp().getMethod();
                    String path = request.getAttributes().getRequest().getHttp().getPath();
                    switch (path) {
                        case "/allow":
                            code = GRANTED;
                            break;
                        case "/deny":
                            code = DENIED;
                            break;

                        default:
                            code = DENIED;
                            break;
                    }

                    responseObserver.onNext(io.envoyproxy.envoy.service.auth.v2.CheckResponse.newBuilder()
                        .setStatus(Status.newBuilder().setCode(code).build())
                        .setOkResponse(io.envoyproxy.envoy.service.auth.v2.OkHttpResponse.newBuilder().build())
                        .build());

                    responseObserver.onCompleted();
                }
            })
            .addService(new io.envoyproxy.envoy.service.auth.v3.AuthorizationGrpc.AuthorizationImplBase() {
                @SneakyThrows
                @Override
                public void check(io.envoyproxy.envoy.service.auth.v3.CheckRequest request, StreamObserver<io.envoyproxy.envoy.service.auth.v3.CheckResponse> responseObserver) {
                    log.info("v3.CheckRequest: {}", request);

                    int code;

                    String path = request.getAttributes().getRequest().getHttp().getPath();
                    switch (path) {
                        case "/allow":
                            code = GRANTED;
                            break;
                        case "/deny":
                            code = DENIED;
                            break;

                        default:
                            code = GRANTED;
                            break;
                    }

                    responseObserver.onNext(io.envoyproxy.envoy.service.auth.v3.CheckResponse.newBuilder()
                        .setStatus(Status.newBuilder().setCode(code).build())
                        .setOkResponse(io.envoyproxy.envoy.service.auth.v3.OkHttpResponse.newBuilder().build())
                        .build());

                    responseObserver.onCompleted();
                }
            })
            .build();

        server.start();

        log.info("gRPC listen port: {}", server.getPort());
    }

    @Override
    public void close() {
        server.shutdown();
    }
}
