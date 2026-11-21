package com.blbulyandavbulyan.larm.api.advice;

import com.blbulyandavbulyan.larm.core.exception.ConflictException;
import com.blbulyandavbulyan.larm.core.exception.EntityNotFoundException;
import com.blbulyandavbulyan.larm.exercise.evaluation.UnsupportedAnswerTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerAdvice {
    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage())).build();
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleConflictException(ConflictException e) {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage())).build();
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleUnsupportedAnswerType(UnsupportedAnswerTypeException e) {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage())).build();
    }
}
