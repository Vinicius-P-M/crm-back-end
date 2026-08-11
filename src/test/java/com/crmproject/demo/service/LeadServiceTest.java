package com.crmproject.demo.service;

import com.crmproject.demo.dto.LeadStatsResponse;
import com.crmproject.demo.dto.StatusCount;
import com.crmproject.demo.model.Lead;
import com.crmproject.demo.model.StatusLead;
import com.crmproject.demo.repo.LeadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// @ExtendWith liga o Mockito nesse teste — sem isso, @Mock e @InjectMocks são só anotações "mortas".
@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    // @Mock cria um LeadRepository falso: existe, tem os mesmos métodos,
    // mas não faz nada de verdade a menos que a gente diga o que ele deve devolver (with "when...thenReturn").
    @Mock
    private LeadRepository repository;

    // @InjectMocks cria a instância REAL de LeadService, mas injeta o @Mock acima
    // no lugar do @Autowired LeadRepository que ela normalmente teria.
    @InjectMocks
    private LeadService service;

    @Test
    void criarLead_devePersistirComStatusPrimeiroContatoEDataPreenchida() {
        // Arrange: monta o cenário.
        // O lead que "chega" pro service, ainda sem status nem data definidos.
        Lead leadNovo = new Lead();
        leadNovo.setEmpresa("Empresa Teste");

        // Ensinamos o mock: "quando repository.save for chamado com qualquer Lead,
        // devolve o mesmo objeto recebido" — simula o banco devolvendo a entidade salva.
        when(repository.save(any(Lead.class))).thenAnswer(chamada -> chamada.getArgument(0));

        // Act: executa o método que estamos testando de verdade.
        Lead resultado = service.criarLead(leadNovo);

        // Assert: confere o resultado.
        assertThat(resultado.getStatus()).isEqualTo(StatusLead.PRIMEIRO_CONTATO);
        assertThat(resultado.getDataUltimaAtualizacao()).isNotNull();

        // Confirma que o service realmente chamou save() — não só que o objeto ficou certo,
        // mas que o "efeito colateral" esperado (persistir) de fato aconteceu.
        verify(repository).save(leadNovo);
    }

    @Test
    void buscarPorId_devolveOLeadQuandoEleExiste() {
        // Arrange
        Lead leadExistente = new Lead();
        leadExistente.setEmpresa("Empresa Existente");
        when(repository.findById(1L)).thenReturn(Optional.of(leadExistente));

        // Act
        Lead resultado = service.buscarPorId(1L);

        // Assert
        assertThat(resultado).isEqualTo(leadExistente);
    }

    @Test
    void buscarPorId_lancaLeadNotFoundExceptionQuandoNaoExiste() {
        // Arrange: o mock devolve um Optional vazio, simulando "não achou no banco".
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert, juntos: assertThatThrownBy executa o código e confere a exceção lançada.
        assertThatThrownBy(() -> service.buscarPorId(999L))
            .isInstanceOf(LeadNotFoundException.class)
            .hasMessageContaining("999");
    }

    @Test
    void atualizarStatus_alteraStatusEAtualizaData() {
        // Arrange
        Lead lead = new Lead();
        lead.setEmpresa("Empresa X");
        lead.setStatus(StatusLead.PRIMEIRO_CONTATO);
        LocalDateTime dataAntiga = lead.getDataUltimaAtualizacao();

        when(repository.findById(1L)).thenReturn(Optional.of(lead));
        when(repository.save(any(Lead.class))).thenAnswer(chamada -> chamada.getArgument(0));

        // Act
        Lead resultado = service.atualizarStatus(1L, StatusLead.ACEITA);

        // Assert
        assertThat(resultado.getStatus()).isEqualTo(StatusLead.ACEITA);
        assertThat(resultado.getDataUltimaAtualizacao()).isAfterOrEqualTo(dataAntiga);
    }

    @Test
    void listarRecentes_delegaParaQueryDeTop10() {
        // Arrange
        List<Lead> leadsRecentes = List.of(new Lead(), new Lead());
        when(repository.findTop10ByOrderByDataUltimaAtualizacaoDesc()).thenReturn(leadsRecentes);

        // Act
        List<Lead> resultado = service.listarRecentes();

        // Assert
        assertThat(resultado).isEqualTo(leadsRecentes);
    }

    @Test
    void calcularStats_calculaPercentualCorretamenteEntreDoisStatus() {
        // Arrange: dois status, um com 7500 (75%) e outro com 2500 (25%) de um total de 10000.
        List<StatusCount> contagens = List.of(
            new StatusCount(StatusLead.PRIMEIRO_CONTATO, 3L, 7500.0),
            new StatusCount(StatusLead.ACEITA, 1L, 2500.0)
        );
        when(repository.contarPorStatus()).thenReturn(contagens);

        // Act
        LeadStatsResponse stats = service.calcularStats();

        // Assert
        assertThat(stats.totalQuantidade()).isEqualTo(4L);
        assertThat(stats.totalValor()).isEqualTo(10000.0);
        assertThat(stats.porStatus()).hasSize(2);
        assertThat(stats.porStatus().get(0).percentualValor()).isEqualTo(75.0);
        assertThat(stats.porStatus().get(1).percentualValor()).isEqualTo(25.0);
    }

    @Test
    void calcularStats_naoQuebraQuandoNaoHaLeads() {
        // Arrange: nenhum lead no banco — a query de agregação devolve lista vazia.
        when(repository.contarPorStatus()).thenReturn(List.of());

        // Act
        LeadStatsResponse stats = service.calcularStats();

        // Assert: sem isso, valorTotal == 0 causaria divisão por zero no cálculo do percentual.
        assertThat(stats.totalQuantidade()).isZero();
        assertThat(stats.totalValor()).isZero();
        assertThat(stats.porStatus()).isEmpty();
    }
}
