package com.example.hr.training.api;

import com.example.hr.training.dto.TrainingEnrollmentDTO;
import com.example.hr.training.dto.TrainingProgramDTO;
import com.example.hr.training.entity.TrainingEnrollment;
import com.example.hr.training.entity.TrainingProgram;
import com.example.hr.training.service.TrainingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/training")
public class TrainingApiController {

    private final TrainingService trainingService;

    public TrainingApiController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping
    public ResponseEntity<List<TrainingProgram>> getAll() {
        return ResponseEntity.ok(trainingService.getAllPrograms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingProgram> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(trainingService.getProgramById(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<TrainingProgram>> getActive() {
        return ResponseEntity.ok(trainingService.getActivePrograms());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<TrainingProgram>> getUpcoming() {
        return ResponseEntity.ok(trainingService.getUpcomingPrograms());
    }

    @PostMapping
    public ResponseEntity<TrainingProgram> create(@Valid @RequestBody TrainingProgramDTO dto) {
        return ResponseEntity.ok(trainingService.createProgram(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingProgram> update(@PathVariable Integer id,
                                                    @Valid @RequestBody TrainingProgramDTO dto) {
        return ResponseEntity.ok(trainingService.updateProgram(id, dto));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<TrainingProgram> start(@PathVariable Integer id) {
        return ResponseEntity.ok(trainingService.startProgram(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TrainingProgram> complete(@PathVariable Integer id) {
        return ResponseEntity.ok(trainingService.completeProgram(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TrainingProgram> cancel(@PathVariable Integer id) {
        return ResponseEntity.ok(trainingService.cancelProgram(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        trainingService.deleteProgram(id);
        return ResponseEntity.noContent().build();
    }

    // --- Enrollments ---

    @PostMapping("/{programId}/enroll/{userId}")
    public ResponseEntity<TrainingEnrollment> enroll(@PathVariable Integer programId,
                                                       @PathVariable Integer userId) {
        return ResponseEntity.ok(trainingService.enrollUser(programId, userId));
    }

    @GetMapping("/{programId}/enrollments")
    public ResponseEntity<List<TrainingEnrollment>> getEnrollments(@PathVariable Integer programId) {
        return ResponseEntity.ok(trainingService.getProgramEnrollments(programId));
    }

    @PatchMapping("/enrollment/{id}/grade")
    public ResponseEntity<TrainingEnrollment> grade(@PathVariable Integer id,
                                                      @RequestBody TrainingEnrollmentDTO dto) {
        return ResponseEntity.ok(trainingService.gradeEnrollment(id, dto.getScore(), dto.getFeedback()));
    }

    @PatchMapping("/enrollment/{id}/drop")
    public ResponseEntity<TrainingEnrollment> drop(@PathVariable Integer id) {
        return ResponseEntity.ok(trainingService.dropEnrollment(id));
    }

    @GetMapping("/user/{userId}/enrollments")
    public ResponseEntity<List<TrainingEnrollment>> getUserEnrollments(@PathVariable Integer userId) {
        return ResponseEntity.ok(trainingService.getUserEnrollments(userId));
    }

    @GetMapping("/budget/{year}")
    public ResponseEntity<BigDecimal> getBudget(@PathVariable int year) {
        return ResponseEntity.ok(trainingService.getTotalBudgetByYear(year));
    }
}

