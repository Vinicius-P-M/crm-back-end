package com.crmproject.demo.service;

public class LeadNotFoundException extends RuntimeException {

    public LeadNotFoundException(Long id) {
        super("Lead não encontrado com id: " + id);
    }

}
