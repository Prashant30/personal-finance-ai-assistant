package dev.prashant.finance.assistant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssistantController.class)
class AssistantControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AssistantService assistantService;

    @Test
    void validatesBlankQuestions() throws Exception {
        mockMvc.perform(post("/api/v1/assistant/questions")
                        .contentType("application/json")
                        .content("{\"question\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void returnsStructuredAnswer() throws Exception {
        SpendingIntent intent = new SpendingIntent(
                IntentType.TOTAL_SPENDING, "2026-10-01", "2026-10-29", "", "", "", 5);
        when(assistantService.answer(anyString())).thenReturn(new QuestionResponse(
                "How much?", "The total was $100.00.", intent,
                List.of(new Evidence("total_spending", "Total spending", "100.00", "USD", "October")),
                "Qwen/Qwen3-8B", "request-1"));

        mockMvc.perform(post("/api/v1/assistant/questions")
                        .contentType("application/json")
                        .content("{\"question\":\"How much?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("The total was $100.00."))
                .andExpect(jsonPath("$.evidence[0].value").value("100.00"));
    }
}

