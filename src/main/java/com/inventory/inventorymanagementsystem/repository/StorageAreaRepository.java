package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.StorageArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface StorageAreaRepository extends JpaRepository<StorageArea, Long> {

    @Query("SELECT COALESCE(SUM(s.currentQuantity), 0) FROM StorageArea s WHERE s.tool.id = :toolId")
    int findTotalAvailableQuantityByToolId(Long toolId);

    // Returns only the tool IDs that exist in the given factory
    @Query("SELECT DISTINCT sa.tool.id FROM StorageArea sa WHERE sa.factory.id = :factoryId")
    Set<Long> findToolIdsByFactoryId(@Param("factoryId") Long factoryId);

    List<StorageArea> findByFactoryId(Long factoryId);
}
