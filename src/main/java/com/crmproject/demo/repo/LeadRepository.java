package com.crmproject.demo.repo;

import com.crmproject.demo.dto.StatusCount;
import com.crmproject.demo.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findTop10ByOrderByDataUltimaAtualizacaoDesc();

    @Query("SELECT new com.crmproject.demo.dto.StatusCount(l.status, COUNT(l), SUM(l.valorContrato)) " +
           "FROM Lead l GROUP BY l.status")
    List<StatusCount> contarPorStatus();

}
