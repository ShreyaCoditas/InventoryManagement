//package com.inventory.inventorymanagementsystem.controller;
//
//import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
//import com.inventory.inventorymanagementsystem.dto.ToolDto;
//import com.inventory.inventorymanagementsystem.dto.ToolRequestDto;
//import com.inventory.inventorymanagementsystem.service.WorkerService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/worker")
//public class WorkerController {
//
//    @Autowired
////    private WorkerService workerService;
//
//    @PostMapping("/request")
//    public ResponseEntity<ApiResponseDto<String>> requestTool(
//            @RequestBody ToolRequestDto request,
//            @RequestParam Long workerId) {
//        return ResponseEntity.ok(workerService.requestTool(workerId, request));
//    }
//
////    @PutMapping("/return/{issuanceId}")
////    public ResponseEntity<ApiResponseDto<String>> returnTool(
////            @PathVariable Long issuanceId,
////            @RequestParam int quantity) {
////        return ResponseEntity.ok(workerService.returnTool(issuanceId, quantity));
////    }
//}
