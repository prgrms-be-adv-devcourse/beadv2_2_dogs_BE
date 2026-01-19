package com.barofarm.config;

import feign.RequestInterceptor;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@RequiredArgsConstructor
public class FeignTracePropagationConfig {

    private final CurrentTraceContext currentTraceContext;
    private final Propagator propagator;

    @Bean
    public RequestInterceptor tracingFeignInterceptor() {
        return template -> {
            TraceContext context = currentTraceContext.context();
            if (context == null) {
                return;
            }
            propagator.inject(context, template,
                (carrier, key, value) -> carrier.header(key, value));
        };
    }
}
