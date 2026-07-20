package com.crmproject.demo.controller;

import com.crmproject.demo.dto.LeadStatsResponse;
import com.crmproject.demo.model.Lead;
import com.crmproject.demo.model.StatusLead;
import com.crmproject.demo.service.LeadNotFoundException;
import com.crmproject.demo.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    // GET /api/leads/recentes - Últimos 10 leads criados/atualizados (tela Home)
    @GetMapping("/recentes")
    public List<Lead> listarRecentes() {
        return service.listarRecentes();
    }

    // GET /api/leads/stats - Agregação por status (tela Análise de Dados)
    @GetMapping("/stats")
    public LeadStatsResponse stats() {
        return service.calcularStats();
    }

    // GET /api/leads/{id} - Busca um lead por ID
    @GetMapping("/{id}")
    public Lead buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
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

    @ExceptionHandler(LeadNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleLeadNotFound(LeadNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @PutMapping("/{id}/status")
    public Lead atualizarStatus(@PathVariable Long id, @RequestBody StatusLead status) {
        return service.atualizarStatus(id, status);
    }
}
