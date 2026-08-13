package com.blbulyandavbulyan.larm.api.advice;

import com.blbulyandavbulyan.larm.storage.ObjectNotFoundException;
import com.blbulyandavbulyan.larm.storage.local.PathTraversalDetectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.blbulyandavbulyan.larm.api.assets")
public class AssetControllerAdvice {

    @ExceptionHandler({ObjectNotFoundException.class, PathTraversalDetectedException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleStorageExceptions(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problemDetail.setTitle("Internal Server Error");
        return problemDetail;
    }
}
