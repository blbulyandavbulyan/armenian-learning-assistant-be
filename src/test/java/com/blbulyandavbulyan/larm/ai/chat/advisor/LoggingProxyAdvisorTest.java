package com.blbulyandavbulyan.larm.ai.chat.advisor;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingProxyAdvisorTest {

    @Mock
    private CallAdvisor delegate;

    @Mock
    private CallAdvisorChain originalChain;

    @Mock
    private CallAdvisorChain copiedChain;

    @Test
    void getName_appendsProxyToDelegateName() {
        when(delegate.getName()).thenReturn("TestAdvisor");
        LoggingProxyAdvisor proxy = new LoggingProxyAdvisor(delegate);

        assertThat(proxy.getName()).isEqualTo("LoggingProxy for TestAdvisor");
    }

    @Test
    void getOrder_returnsDelegateOrder() {
        when(delegate.getOrder()).thenReturn(42);
        LoggingProxyAdvisor proxy = new LoggingProxyAdvisor(delegate);

        assertThat(proxy.getOrder()).isEqualTo(42);
    }

    @Test
    void adviseCall_wrapsChainAndReturnsResponse() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        
        AssistantMessage message = new AssistantMessage("test response");
        Generation generation = new Generation(message);
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        ChatClientResponse mockResponse = mock(ChatClientResponse.class);
        when(mockResponse.chatResponse()).thenReturn(chatResponse);

        when(delegate.adviseCall(eq(request), any(CallAdvisorChain.class))).thenAnswer(invocation -> {
            CallAdvisorChain wrappedChain = invocation.getArgument(1);
            
            // simulate delegate doing a nextCall
            when(originalChain.nextCall(request)).thenReturn(mockResponse);
            ChatClientResponse innerResponse = wrappedChain.nextCall(request);
            
            return innerResponse;
        });

        LoggingProxyAdvisor proxy = new LoggingProxyAdvisor(delegate);
        
        ChatClientResponse result = proxy.adviseCall(request, originalChain);

        assertThat(result).isSameAs(mockResponse);
        verify(delegate).adviseCall(eq(request), any(CallAdvisorChain.class));
        verify(originalChain).nextCall(request);
    }

    @Test
    void wrappedChain_copy_whenPassedDelegate_translatesToProxy() {
        when(delegate.adviseCall(any(), any(CallAdvisorChain.class))).thenAnswer(invocation -> {
            CallAdvisorChain wrappedChain = invocation.getArgument(1);
            
            // The core test: The delegate asks to copy the chain starting from ITSELF
            wrappedChain.copy(delegate);
            
            return mock(ChatClientResponse.class); // Return dummy response
        });

        LoggingProxyAdvisor proxy = new LoggingProxyAdvisor(delegate);

        // Setting up the proxy to return the copied chain when requested to copy from ITSELF
        when(originalChain.copy(proxy)).thenReturn(copiedChain);

        ChatClientRequest request = mock(ChatClientRequest.class);
        proxy.adviseCall(request, originalChain);

        // Verification: Even though wrappedChain.copy(delegate) was called, 
        // the originalChain received copy(proxy)
        verify(originalChain).copy(proxy);
        verify(originalChain, never()).copy(delegate);
    }

    @Test
    void wrappedChain_copy_whenPassedOtherAdvisor_passesThrough() {
        CallAdvisor otherAdvisor = mock(CallAdvisor.class);

        when(delegate.adviseCall(any(), any(CallAdvisorChain.class))).thenAnswer(invocation -> {
            CallAdvisorChain wrappedChain = invocation.getArgument(1);
            
            // Delegate asks to copy chain starting from some OTHER advisor
            wrappedChain.copy(otherAdvisor);
            
            return mock(ChatClientResponse.class); // Return dummy response
        });

        LoggingProxyAdvisor proxy = new LoggingProxyAdvisor(delegate);

        when(originalChain.copy(otherAdvisor)).thenReturn(copiedChain);

        ChatClientRequest request = mock(ChatClientRequest.class);
        proxy.adviseCall(request, originalChain);

        verify(originalChain).copy(otherAdvisor);
        verify(originalChain, never()).copy(proxy);
    }
}
