package com.inventory.inventorymanagementsystem.schedular;



import com.inventory.inventorymanagementsystem.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReturnStatusScheduler {

    private final ToolService toolService;

    // runs daily at midnight
    //@Scheduled(cron = "0 0 0 * * ?")
    @Scheduled(cron = "*/30 * * * * *")
    @Transactional
    public void updateOverdueAndSeized() {
        toolService.processOverdueAndSeize();
    }
}

