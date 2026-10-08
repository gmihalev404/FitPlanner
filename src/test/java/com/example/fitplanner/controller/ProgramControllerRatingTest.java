package com.example.fitplanner.controller;

import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.ProgramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProgramControllerRatingTest {

    private ProgramService programService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        programService = mock(ProgramService.class);
        ProgramController controller = new ProgramController(programService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void unauthenticatedUsersCannotRate() throws Exception {
        mockMvc.perform(post("/programs/rate/1").param("rating", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void authenticatedRatingCallsService() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(15L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", userDto);

        mockMvc.perform(post("/programs/rate/7").param("rating", "4").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/details/7"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(programService).rateProgram(7L, 15L, 4);
    }

    @Test
    void duplicateRatingShowsErrorMessage() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(33L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", userDto);
        doThrow(new IllegalStateException("You already rated this program"))
                .when(programService).rateProgram(2L, 33L, 5);

        mockMvc.perform(post("/programs/rate/2").param("rating", "5").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/programs/details/2"))
                .andExpect(flash().attribute("errorMessage", "You already rated this program"));
    }
}
