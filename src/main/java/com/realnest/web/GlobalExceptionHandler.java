package com.realnest.web;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ModelAndView handleNotFound(NoSuchElementException ex) {
    ModelAndView mav = new ModelAndView("error/404");
    mav.setStatus(HttpStatus.NOT_FOUND);
    mav.addObject("message", ex.getMessage());
    return mav;
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
  }

  @ExceptionHandler(Exception.class)
  public ModelAndView handleGeneric(Exception ex) {
    ModelAndView mav = new ModelAndView("error/500");
    mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    mav.addObject("message", ex.getMessage());
    return mav;
  }
}
