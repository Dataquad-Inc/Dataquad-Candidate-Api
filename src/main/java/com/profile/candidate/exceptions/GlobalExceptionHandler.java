package com.profile.candidate.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    // Handle CandidateNotFoundException
    @ExceptionHandler(CandidateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCandidateNotFoundException(CandidateNotFoundException ex) {

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(404,ex.getMessage());
        ErrorResponse response=new ErrorResponse(false,"Candidate Not Found ",null,error);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // HTTP 404
    }
    @ExceptionHandler(InterviewAlreadyScheduledException.class)
    public ResponseEntity<ErrorResponse> handleInterviewAlreadyScheduledException(InterviewAlreadyScheduledException ex) {

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(400, ex.getMessage());
        ErrorResponse response=new ErrorResponse(false,"Interview not Scheduled",null,error);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileTypeException(InvalidFileTypeException ex) {
        logger.info("handling InvalidFileTypeException in Global Exception Handler");
        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(400, ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                false,
                "Invalid File Type",
                null,
                error
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    // Handle FileSizeExceededException (added for file size exceeded)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(413,ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                false,
                "File size exceeds the maximum allowed size of 10 MB.",
                null,
                error
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);  // Return 413 Payload Too Large
    }
    // Handle CandidateAlreadyExistsException
    @ExceptionHandler(CandidateAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCandidateAlreadyExistsException(CandidateAlreadyExistsException ex) {

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(409, ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                true,  // Custom exception message
                "Candidate Already Exists",
                null,
                error
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT); // HTTP 409
    }
    @ExceptionHandler(SubmissionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSubmissionNotFoundException(SubmissionNotFoundException ex){

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(404,ex.getMessage());
        ErrorResponse response=new ErrorResponse(
                false,
                "Submission Not Found",
                null,
                error);
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex){

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(500,ex.getMessage());
        ErrorResponse response=new ErrorResponse(false,"Exception",null,error);
        return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // Handle all other unchecked exceptions (generic fallback)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
       ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(500,ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                false,
                "Internal server error occurred",
                null,
                error
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // HTTP 500
    }
    @ExceptionHandler(InterviewNotScheduledException.class)
    public ResponseEntity<ErrorResponse> handleInterviewNotScheduledException(InterviewNotScheduledException ex){
        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(409,ex.getMessage());
        ErrorResponse response=new ErrorResponse(
                false,
                "Interview Not Scheduled ",
                null,
                error);

        return new ResponseEntity<>(response,HttpStatus.CONFLICT);
    }
    @ExceptionHandler(InvalidClientException.class)
    public ResponseEntity<ErrorResponse> handleInvalidClientException(InvalidClientException ex){

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(403, ex.getMessage());
        ErrorResponse response=new ErrorResponse(false,"Interview Not scheduled",null,error);

        return new ResponseEntity<>(response,HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInterviewResponseDto(JobNotFoundException ex){

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(403, ex.getMessage());
        ErrorResponse response=new ErrorResponse(false,"Interview Not Scheduled",null,error);
        return new ResponseEntity<>(response,HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(NoInterviewsFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoInterviewsFoundException(NoInterviewsFoundException ex){

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(404, ex.getMessage());
      ErrorResponse response=new ErrorResponse(false,"No Interviews Found",null,error);
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(DateRangeValidationException.class)
    public ResponseEntity<ErrorResponse> handleDateRangeValidationException(DateRangeValidationException ex){

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(409, ex.getMessage());
        ErrorResponse response=new ErrorResponse(false,"No Interviews Found",null,error);
        return new ResponseEntity<>(response,HttpStatus.CONFLICT);

    }
    @ExceptionHandler(DuplicateInterviewPlacementException.class)
    public ResponseEntity<?> handleDuplicateInterviewPlacementException(DuplicateInterviewPlacementException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(PlacementsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlacementsNotFoundException(PlacementsNotFoundException ex){

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(404, ex.getMessage());
        ErrorResponse response=new ErrorResponse(false,"Placements Not Found",null,error);
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlacementsNotFoundException(UserNotFoundException ex){

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(404, ex.getMessage());
        ErrorResponse response=new ErrorResponse(false,"User Not Found",null,error);
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidOTPException.class)
    public ResponseEntity<ErrorResponse> handlePlacementsNotFoundException(InvalidOTPException ex){

        ErrorResponse.ErrorDto error=new ErrorResponse.ErrorDto(400, ex.getMessage());
        ErrorResponse response=new ErrorResponse(false,"Invalid OTP",null,error);
        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }
}
