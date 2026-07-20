package com.crmproject.demo.dto;

import com.crmproject.demo.model.StatusLead;

// Projeção "crua" da agregação no banco (usada só pela query JPQL).
public record StatusCount(StatusLead status, Long quantidade, Double valorTotal) {
}
