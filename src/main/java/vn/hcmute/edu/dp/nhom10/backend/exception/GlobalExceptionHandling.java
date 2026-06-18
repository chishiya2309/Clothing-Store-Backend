package vn.hcmute.edu.dp.nhom10.backend.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestControllerAdvice
public class GlobalExceptionHandling {

    @ExceptionHandler(InsufficientStockException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Insufficient Stock Response", summary = "Handle exception when product has insufficient stock", value = """
                            {
                                "timestamp": "2023-10-19T06:07:35.321+00:00",
                                "status": 400,
                                "path": "/api/customer/cart/items",
                                "error": "Bad Request",
                                "message": "Product has insufficient stock"
                            }
                            """)) })
    })
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(
            InsufficientStockException e,
            WebRequest request
    ) {
        HttpStatus status = BAD_REQUEST;
        ErrorResponse errorResponse = baseErrorResponse(status, request);
        errorResponse.setMessage(e.getMessage());

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler({ ConstraintViolationException.class, MissingServletRequestParameterException.class,
            MethodArgumentNotValidException.class, IllegalArgumentException.class })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "Handle exception when the data invalid. (@RequestBody, @RequestParam)", summary = "Handle Bad Request", value = """
                            {
                                "timestamp": "2023-10-19T06:07:35.321+00:00",
                                "status": 400,
                                "path": "/api/v1/...",
                                "error": "Invalid payload",
                                "message": "{data} must not be blank"
                            }
                            """)) })
    })
    public ResponseEntity<ErrorResponse> handleValidationException(Exception e, WebRequest request) {
        HttpStatus status = BAD_REQUEST;
        ErrorResponse errorResponse = baseErrorResponse(status, request);

        String message = e.getMessage();
        if (e instanceof MethodArgumentNotValidException) {
            int start = message.lastIndexOf("[") + 1;
            int end = message.lastIndexOf("]") - 1;
            message = message.substring(start, end);
            errorResponse.setError("Invalid Payload");
            errorResponse.setMessage(message);
        } else if (e instanceof MissingServletRequestParameterException) {
            errorResponse.setError("Invalid Parameter");
            errorResponse.setMessage(message);
        } else if (e instanceof ConstraintViolationException) {
            errorResponse.setError("Invalid Parameter");
            errorResponse.setMessage(message.substring(message.indexOf(" ") + 1));
        } else {
            errorResponse.setError("Invalid Data");
            errorResponse.setMessage(message);
        }

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Not Found", content = {
                    @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "404 Response", summary = "Handle exception when resource not found", value = """
                            {
                                "timestamp": "2023-10-19T06:07:35.321+00:00",
                                "status": 404,
                                "path": "/api/v1/...",
                                "error": "Not Found",
                                "message": "{data} not found"
                            }
                            """)) })
    })
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException e,
            WebRequest request
    ) {
        HttpStatus status = NOT_FOUND;
        ErrorResponse errorResponse = baseErrorResponse(status, request);
        errorResponse.setMessage(e.getMessage());

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(HttpServerErrorException.InternalServerError.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                    @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "500 Response", summary = "Handle exception when server error", value = """
                            {
                                "timestamp": "2023-10-19T06:07:35.321+00:00",
                                "status": 500,
                                "path": "/api/v1/...",
                                "error": "Internal Server Error",
                                "message": "Connection timeout, please try again"
                            }
                            """)) })
    })
    public ResponseEntity<ErrorResponse> handleInternalServerErrorException(
            HttpServerErrorException.InternalServerError e,
            WebRequest request
    ) {
        HttpStatus status = INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = baseErrorResponse(status, request);
        errorResponse.setMessage(e.getMessage());

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(InvalidDataException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "409", description = "Conflict", content = {
                    @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "409 Response", summary = "Handle exception when input data is conflicted", value = """
                            {
                                "timestamp": "2023-10-19T06:07:35.321+00:00",
                                "status": 409,
                                "path": "/api/v1/...",
                                "error": "Conflict",
                                "message": "{data} exists. Please try again"
                            }
                            """)) })
    })
    public ResponseEntity<ErrorResponse> handleDuplicateKeyException(
            InvalidDataException e,
            WebRequest request
    ) {
        HttpStatus status = CONFLICT;
        ErrorResponse errorResponse = baseErrorResponse(status, request);
        errorResponse.setMessage(e.getMessage());

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(PaymentGatewayUnavailableException.class)
    public ResponseEntity<ErrorResponse> handlePaymentGatewayUnavailableException(
            PaymentGatewayUnavailableException e,
            WebRequest request
    ) {
        HttpStatus status = SERVICE_UNAVAILABLE;
        ErrorResponse errorResponse = baseErrorResponse(status, request);
        errorResponse.setMessage(e.getMessage());

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler({ AccessDeniedException.class })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Forbidden", content = {
                    @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "403 Response", summary = "Handle exception when access forbidden", value = """
                            {
                                "timestamp": "2023-10-19T06:07:35.321+00:00",
                                "status": 403,
                                "path": "/api/v1/...",
                                "error": "Forbidden",
                                "message": "Access denied! {reason}"
                            }
                            """)) })
    })
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e,
            WebRequest req
    ) {
        HttpStatus status = FORBIDDEN;
        ErrorResponse errorResponse = baseErrorResponse(status, req);
        errorResponse.setMessage(e.getMessage());

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler({ InternalAuthenticationServiceException.class,
            org.springframework.security.authentication.BadCredentialsException.class })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {
                    @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(name = "401 Response", summary = "Handle exception when user not authenticated", value = """
                            {
                                "timestamp": "2023-10-19T06:07:35.321+00:00",
                                "status": 401,
                                "path": "/api/v1/...",
                                "error": "Unauthorized",
                                "message": "Username or password is incorrect"
                            }
                            """)) })
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception e, WebRequest req) {
        HttpStatus status = UNAUTHORIZED;
        ErrorResponse errorResponse = baseErrorResponse(status, req);
        errorResponse.setMessage("Username or password is incorrect");

        return ResponseEntity.status(status).body(errorResponse);
    }

    private ErrorResponse baseErrorResponse(HttpStatus status, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(status.value());
        errorResponse.setError(status.getReasonPhrase());
        return errorResponse;
    }
}
