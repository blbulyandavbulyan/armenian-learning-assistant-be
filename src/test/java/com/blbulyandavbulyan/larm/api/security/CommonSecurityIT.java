package com.blbulyandavbulyan.larm.api.security;

import com.blbulyandavbulyan.larm.api.BaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class CommonSecurityIT extends BaseIT {

    interface RequestMapping {
        String CHAT_DIALOGUES = "/chat/dialogues";
    }

    @Test
    void testCorsConfiguration_whenOriginIsRight() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.options(RequestMapping.CHAT_DIALOGUES)
                        .header("Origin", "http://localhost:8081")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Origin", "http://localhost:8081"))
                .andExpect(MockMvcResultMatchers.header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH"));
    }

    @Test
    void testCorsConfiguration_whenOriginIsWrong() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.options(RequestMapping.CHAT_DIALOGUES)
                        .header("Origin", "http://unknown-origin.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
