package com.crmproject.demo.service;

import com.crmproject.demo.dto.LeadStatsResponse;
import com.crmproject.demo.dto.StatusCount;
import com.crmproject.demo.dto.StatusStat;
import com.crmproject.demo.model.Lead;
import com.crmproject.demo.model.StatusLead;
import com.crmproject.demo.repo.LeadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadService {

    @Autowired
    private LeadRepository repository;

    // Método para criar (já definido por você)
    public Lead criarLead(Lead lead) {
        lead.setStatus(StatusLead.PRIMEIRO_CONTATO);
        return repository.save(lead);
    }

    // Método para listar (já definido por você)
    public List<Lead> listarTodos() {
        return repository.findAll();
    }

    public List<Lead> listarRecentes() {
        return repository.findTop10ByOrderByDataUltimaAtualizacaoDesc();
    }

    public LeadStatsResponse calcularStats() {
        List<StatusCount> contagens = repository.contarPorStatus();

        double valorTotal = contagens.stream().mapToDouble(StatusCount::valorTotal).sum();
        long quantidadeTotal = contagens.stream().mapToLong(StatusCount::quantidade).sum();

        List<StatusStat> porStatus = contagens.stream()
            .map(c -> new StatusStat(
                c.status(),
                c.quantidade(),
                c.valorTotal(),
                valorTotal == 0 ? 0 : (c.valorTotal() / valorTotal) * 100
            ))
            .toList();

        return new LeadStatsResponse(porStatus, quantidadeTotal, valorTotal);
    }

    public Lead buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new LeadNotFoundException(id));
    }

    // NOVO: Método para atualizar o status de um lead existente
    public Lead atualizarStatus(Long id, StatusLead novoStatus) {
        Lead lead = repository.findById(id)
            .orElseThrow(() -> new LeadNotFoundException(id));
        
        lead.setStatus(novoStatus); // Isso aciona o setStatus e a data automática
        return repository.save(lead);
    }
}