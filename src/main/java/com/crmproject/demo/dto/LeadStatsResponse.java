package com.crmproject.demo.dto;

import java.util.List;

public record LeadStatsResponse(List<StatusStat> porStatus, long totalQuantidade, double totalValor) {
}
