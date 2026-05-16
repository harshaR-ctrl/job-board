package com.example.jobboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a candidate attempts to apply to a job they have already applied to.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(String message) {
        super(message);
    }

    public DuplicateApplicationException(Long candidateId, Long jobId) {
        super(String.format("Candidate %d has already applied to job %d", candidateId, jobId));
    }
}
