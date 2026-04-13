// package asset.project.exception;

// import asset.project.dto.response.ApiResponse;
// import org.springframework.http.HttpStatus;
// import org.springframework.security.access.AccessDeniedException;
// import org.springframework.security.authentication.BadCredentialsException;
// import org.springframework.web.bind.MethodArgumentNotValidException;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.ResponseStatus;
// import org.springframework.web.bind.annotation.RestControllerAdvice;

// @RestControllerAdvice
// public class GlobalExceptionHandler {

//     @ExceptionHandler(MethodArgumentNotValidException.class)
//     @ResponseStatus(HttpStatus.BAD_REQUEST)
//     public ApiResponse<Object> handleValidation(MethodArgumentNotValidException ex) {
//         String msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
//         return ApiResponse.builder().status(400).message(msg).build();
//     }

//     @ExceptionHandler(ResourceNotFoundException.class)
//     @ResponseStatus(HttpStatus.NOT_FOUND)
//     public ApiResponse<Object> handleNotFound(ResourceNotFoundException ex) {
//         return ApiResponse.builder().status(404).message(ex.getMessage()).build();
//     }

//     @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
//     @ResponseStatus(HttpStatus.FORBIDDEN)
//     public ApiResponse<Object> handleForbidden(RuntimeException ex) {
//         return ApiResponse.builder().status(403).message(ex.getMessage()).build();
//     }

//     @ExceptionHandler(BadCredentialsException.class)
//     @ResponseStatus(HttpStatus.UNAUTHORIZED)
//     public ApiResponse<Object> handleBadCredentials(BadCredentialsException ex) {
//         return ApiResponse.builder().status(401).message("Invalid email or password").build();
//     }

//     @ExceptionHandler(BusinessException.class)
//     @ResponseStatus(HttpStatus.BAD_REQUEST)
//     public ApiResponse<Object> handleBusiness(BusinessException ex) {
//         return ApiResponse.builder().status(400).message(ex.getMessage()).build();
//     }

//     @ExceptionHandler(RuntimeException.class)
//     @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//     public ApiResponse<Object> handleRuntime(RuntimeException ex) {
//         return ApiResponse.builder().status(500).message(ex.getMessage()).build();
//     }
// }

package asset.project.exception;

import asset.project.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.builder().status(400).message(msg).build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Object> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.builder().status(404).message(ex.getMessage()).build();
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Object> handleForbidden(RuntimeException ex) {
        return ApiResponse.builder().status(403).message(ex.getMessage()).build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Object> handleBadCredentials(BadCredentialsException ex) {
        return ApiResponse.builder().status(401).message("Invalid email or password").build();
    }

    @ExceptionHandler(TokenNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Object> handleTokenNotFound(TokenNotFoundException ex) {
        return ApiResponse.builder().status(401).message(ex.getMessage()).build();
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Object> handleInsufficientAuth(InsufficientAuthenticationException ex) {
        return ApiResponse.builder().status(401).message("Token not found").build();
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleBusiness(BusinessException ex) {
        return ApiResponse.builder().status(400).message(ex.getMessage()).build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleRuntime(RuntimeException ex) {
        return ApiResponse.builder().status(500).message(ex.getMessage()).build();
    }
}