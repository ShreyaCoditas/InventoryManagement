package com.inventory.inventorymanagementsystem.constants;

public enum ToolIssuanceStatus {
    ALLOCATED,          // tool issued to worker
    REQUESTED_RETURN,   // worker requested return (submitted quantity)
    //RETURNED,           // supervisor verified returned (fit)
    //DAMAGED,            // supervisor marked as damaged/unfit
    OVERDUE,            // 30 days passed after issuance (no or incomplete return)
    //SEIZED              // 30 + 7 days passed -> seized
    COMPLETED
}

