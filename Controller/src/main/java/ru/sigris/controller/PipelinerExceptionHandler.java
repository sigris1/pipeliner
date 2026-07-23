package ru.sigris.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.sigris.core.exception.Exceptions.CycleDetectedException;
import ru.sigris.core.exception.Exceptions.SelfCyclingException;
import ru.sigris.service.dto.ErrorResponse;
import ru.sigris.service.exception.Exceptions.*;

@RestControllerAdvice
public class PipelinerExceptionHandler {

    @ExceptionHandler(PipelineNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePipelineNotFound(
            PipelineNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NodeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNodeNotFound(
            NodeNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PipelineExist.class)
    public ResponseEntity<ErrorResponse> handlePipelineExist(
            PipelineExist ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(NodeExistInPipeline.class)
    public ResponseEntity<ErrorResponse> handleNodeExistInPipeline(
            NodeExistInPipeline ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(EdgeExist.class)
    public ResponseEntity<ErrorResponse> handleEdgeExist(
            EdgeExist ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(PipelineHasntNode.class)
    public ResponseEntity<ErrorResponse> handlePipelineHasntNode(
            PipelineHasntNode ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(CycleDetectedException.class)
    public ResponseEntity<ErrorResponse> handleCycleDetected(
            CycleDetectedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(SelfCyclingException.class)
    public ResponseEntity<ErrorResponse> handleSelfCycling(
            SelfCyclingException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT,
                "Data conflict: unique constraint violation", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        ex.printStackTrace();

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }
}