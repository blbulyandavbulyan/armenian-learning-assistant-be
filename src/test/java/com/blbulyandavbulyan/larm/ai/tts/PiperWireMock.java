package com.blbulyandavbulyan.larm.ai.tts;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PiperWireMock {
    private final WireMockServer wireMockServer;

    public void stubTtsWithAudio(String text, byte[] audioData) {
        wireMockServer.stubFor(WireMock.post("/")
                .withRequestBody(WireMock.matchingJsonPath("$.text", WireMock.equalTo(text)))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", "audio/wav")
                        .withBody(audioData)));
    }

    public void verifyTtsCalledOnceWith(String text) {
        wireMockServer.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/"))
                .withRequestBody(WireMock.matchingJsonPath("$.text", WireMock.equalTo(text))));
    }
}
