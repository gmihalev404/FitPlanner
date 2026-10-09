package com.example.fitplanner.service;

import com.example.fitplanner.entity.model.Program;
import com.example.fitplanner.entity.model.ProgramRating;
import com.example.fitplanner.entity.model.User;
import com.example.fitplanner.repository.ExerciseProgressRepository;
import com.example.fitplanner.repository.ExerciseRepository;
import com.example.fitplanner.repository.ProgramRatingRepository;
import com.example.fitplanner.repository.ProgramRepository;
import com.example.fitplanner.repository.UserRepository;
import com.example.fitplanner.repository.WorkoutSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgramServiceRatingTest {

    @Mock
    private ProgramRepository programRepository;
    @Mock
    private WorkoutSessionRepository workoutSessionRepository;
    @Mock
    private ExerciseProgressRepository exerciseProgressRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProgramRatingRepository programRatingRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProgramService programService;

    private Program program;
    private User user;

    @BeforeEach
    void setUp() {
        program = new Program();
        program.setId(10L);
        user = new User();
        user.setId(20L);
    }

    @Test
    void rateProgramSuccessUpdatesAverageAndCount() {
        when(programRepository.findById(10L)).thenReturn(Optional.of(program));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(programRatingRepository.existsByUserIdAndProgramId(20L, 10L)).thenReturn(false);
        when(programRatingRepository.findAverageRating(10L)).thenReturn(4.5);
        when(programRatingRepository.countByProgramId(10L)).thenReturn(2L);
        programService.rateProgram(10L, 20L, 5);

        verify(programRatingRepository).saveAndFlush(any(ProgramRating.class));
        ArgumentCaptor<Program> captor = ArgumentCaptor.forClass(Program.class);
        verify(programRepository).save(captor.capture());

        Program saved = captor.getValue();
        assertEquals(4.5, saved.getRating());
        assertEquals(2L, saved.getRatingCount());
    }

    @Test
    void duplicateRatingIsRejected() {
        when(programRepository.findById(10L)).thenReturn(Optional.of(program));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(programRatingRepository.existsByUserIdAndProgramId(20L, 10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> programService.rateProgram(10L, 20L, 4));
        verify(programRatingRepository, never()).saveAndFlush(any(ProgramRating.class));
    }

    @Test
    void invalidRatingValueIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> programService.rateProgram(10L, 20L, 0));
        assertThrows(IllegalArgumentException.class, () -> programService.rateProgram(10L, 20L, 6));
    }

    @Test
    void raceConditionDuplicateConstraintIsHandled() {
        when(programRepository.findById(10L)).thenReturn(Optional.of(program));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(programRatingRepository.existsByUserIdAndProgramId(20L, 10L)).thenReturn(false);
        when(programRatingRepository.saveAndFlush(any(ProgramRating.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(IllegalStateException.class, () -> programService.rateProgram(10L, 20L, 5));
    }
}
