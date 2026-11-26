package com.gooci.console.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleShellControllerTest {

    @Test
    void deliveryRejectsLegacyHttpAndWebSocketBeforeResolvingTarget() throws Exception {
        ModuleShellController controller = new ModuleShellController(null, "", true, false, "", true);
        for (boolean websocket : new boolean[] {false, true}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ext/AI_HUB/");
            if (websocket) {
                request.addHeader("Upgrade", "websocket");
            }
            MockHttpServletResponse response = new MockHttpServletResponse();
            controller.open("AI_HUB", request, response);
            assertEquals(404, response.getStatus());
        }
    }
}
