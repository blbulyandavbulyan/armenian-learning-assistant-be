package com.blbulyandavbulyan.larm.ai.chat.advisor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

@Slf4j
@RequiredArgsConstructor
public class LoggingProxyAdvisor implements CallAdvisor {

    private final CallAdvisor delegate;

    @Override
    public String getName() {
        return "LoggingProxy for " + delegate.getName();
    }

    @Override
    public int getOrder() {
        return delegate.getOrder();
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain originalChain) {
        // 1. INBOUND REQUEST: Before the advisor touches it
        log.debug("➡️ [{}] (1) Request entering advisor: {}", delegate.getName(), request);

        // We wrap the chain to intercept what happens in the middle!
        CallAdvisorChain wrappedChain = new CallAdvisorChain() {
            @Override
            public ChatClientResponse nextCall(ChatClientRequest modifiedRequest) {
                // 2. OUTBOUND REQUEST: After the advisor modified it, right before going to the next layer
                log.debug("➡️ [{}] (2) Request leaving advisor (going to next): {}", delegate.getName(), modifiedRequest);

                ChatClientResponse innerResponse = originalChain.nextCall(modifiedRequest);

                // 3. INBOUND RESPONSE: The raw response coming back from the LLM/next layer, BEFORE the advisor fixes it
                if (innerResponse != null && innerResponse.chatResponse() != null && innerResponse.chatResponse().getResult() != null) {
                    log.debug("⬅️ [{}] (3) Raw response received (before advisor processes it):\n{}",
                            delegate.getName(),
                            innerResponse.chatResponse().getResult().getOutput().getText());
                }
                return innerResponse;
            }

            @Override
            public java.util.List<CallAdvisor> getCallAdvisors() {
                return originalChain.getCallAdvisors();
            }

            @Override
            public CallAdvisorChain copy(CallAdvisor after) {
                return originalChain.copy(after);
            }
        };

        ChatClientResponse finalResponse = delegate.adviseCall(request, wrappedChain);

        // 4. OUTBOUND RESPONSE: The final response AFTER the advisor processed/fixed it
        if (finalResponse != null && finalResponse.chatResponse() != null && finalResponse.chatResponse().getResult() != null) {
            log.debug("⬅️ [{}] (4) Final response returning (after advisor processed it):\n{}",
                    delegate.getName(),
                    finalResponse.chatResponse().getResult().getOutput().getText());
        }

        return finalResponse;
    }
}
