package com.example.fitplanner.service;

import com.example.fitplanner.entity.model.Program;
import com.example.fitplanner.repository.ProgramRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@Service
public class ProgramService {

    private final ProgramRepository programRepository;

    @Autowired
    public ProgramService(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    public List<Program> getProgramsByUserId(Long userId) {
        return programRepository.getByUserId(userId);
    }

    public Program getCurrentProgram() {
        return programRepository.findFirstByOrderByCreatedAtDesc().orElse(null);
    }

    public Program getProgramById(Long id) {
        return programRepository.findById(id).orElse(null);
    }

    public Program saveProgram(Program program) {
        return programRepository.save(program);
    }

    public void deleteProgram(Long id) {
        programRepository.deleteById(id);
    }
}
