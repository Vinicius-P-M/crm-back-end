package com.crmproject.demo.controller;

import com.crmproject.demo.model.Lead;
import com.crmproject.demo.model.StatusLead;
import com.crmproject.demo.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    @Autowired
    private LeadService service;

    // GET /api/leads - Lista todos os leads
    @GetMapping
    public List<Lead> listarTudo() {
        return service.listarTodos();
    }

    // POST /api/leads - Cria um novo lead
    @PostMapping
    public ResponseEntity<Lead> criar(@Valid @RequestBody Lead lead) {
        return ResponseEntity.ok(service.criarLead(lead));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @PutMapping("/{id}/status")
    public Lead atualizarStatus(@PathVariable Long id, @RequestBody StatusLead status) {
        return service.atualizarStatus(id, status);
    }
}
