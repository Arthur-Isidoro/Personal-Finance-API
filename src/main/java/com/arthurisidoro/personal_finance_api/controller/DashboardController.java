package com.arthurisidoro.personal_finance_api.controller;

import com.arthurisidoro.personal_finance_api.dto.response.DashboardResponse;
import com.arthurisidoro.personal_finance_api.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class DashboardController {

    private final TransactionService transactionService;

    public DashboardController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/api/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getDashboard(startDate, endDate));
    }
}