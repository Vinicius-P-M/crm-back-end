package com.crmproject.demo.dto;

import com.crmproject.demo.model.StatusLead;

public record StatusStat(StatusLead status, long quantidade, double valorTotal, double percentualValor) {
}
