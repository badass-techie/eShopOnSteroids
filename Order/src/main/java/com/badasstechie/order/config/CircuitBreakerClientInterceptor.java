package com.badasstechie.order.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.utils.CircuitBreakerUtil;
import io.grpc.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>Courtesy of <a href="https://www.deepnetwork.com/blog/grpc/resilience4j/2020/12/17/Resilience4j-grpc.html">Deep Network GmbH</a></p>
 *
 * <p>The ClientInterceptor will intercept every single call to the client. If the CircuitBreaker
 * is in closed or half-closed state call will be permitted and the grpc request can continue.</p>
 *
 * <p>It will also add a custom listener to every single grpc call to check the status of the call.
 * If it is a server side error it will be judged as circuitBreaker error, otherwise, it will be judged as a success.</p>
 */
public final class CircuitBreakerClientInterceptor implements ClientInterceptor {

    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerClientInterceptor(CircuitBreaker circuitBreaker) {
        super();
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ClientInterceptors.CheckedForwardingClientCall(next.newCall(method, callOptions)) {

            @Override
            protected void checkedStart(ClientCall.Listener responseListener, io.grpc.Metadata headers) {
                if (CircuitBreakerUtil.isCallPermitted(circuitBreaker))
                    this.delegate().start(new CircuitBreakerClientInterceptor.Listener(responseListener, System.nanoTime()), headers);
            }
        };
    }

    private final class Listener extends ForwardingClientCallListener.SimpleForwardingClientCallListener {
        private final long startedAt;
        // Server errors are taken from table in https://cloud.google.com/apis/design/errors
        private final Set<Status.Code> serverErrorStatusSet = Set.of(
                Status.Code.DATA_LOSS,
                Status.Code.UNKNOWN,
                Status.Code.INTERNAL,
                Status.Code.UNIMPLEMENTED,
                Status.Code.UNAVAILABLE,
                Status.Code.DEADLINE_EXCEEDED
        );

        public Listener(io.grpc.ClientCall.Listener delegate, long startedAt) {
            super(delegate);
            this.startedAt = startedAt;
        }

        @Override
        public void onClose(Status status, io.grpc.Metadata trailers) {
            long elapsed = System.nanoTime() - startedAt;
            // If the status code is not a server error status code add a success to circuitBreaker
            if (!serverErrorStatusSet.contains(status.getCode())) {
                CircuitBreakerClientInterceptor.this.circuitBreaker.onSuccess(elapsed, TimeUnit.NANOSECONDS);
            } else {
                CircuitBreakerClientInterceptor.this.circuitBreaker.onError(elapsed, TimeUnit.NANOSECONDS,
                        new StatusRuntimeException(status, trailers));
            }

            super.onClose(status, trailers);
        }
    }
}
