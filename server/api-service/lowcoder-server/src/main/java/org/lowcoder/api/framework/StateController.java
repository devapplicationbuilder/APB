package org.quickdev.api.framework;


import static org.quickdev.sdk.exception.BizError.SERVER_NOT_READY;
import static org.quickdev.sdk.util.ExceptionUtils.ofError;

import org.quickdev.api.framework.view.ResponseView;
import org.quickdev.api.framework.warmup.WarmupHelper;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class StateController implements StateEndpoints 
{
    private final WarmupHelper warmupHelper;

    private volatile boolean ready;

    @Override
    public Mono<ResponseView<Boolean>> healthCheck() {
        if (!ready) {
            return warmupHelper.warmup()
                    .doOnSuccess(it -> ready = true)
                    .then(ofError(SERVER_NOT_READY, "SERVER_NOT_READY"));
        }
        return Mono.just(ResponseView.success(true));
    }

}
