package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.Bay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BayRepository extends JpaRepository<Bay, Long> {

    @Query("SELECT b FROM Bay b WHERE b.factory.id = :factoryId")
    List<Bay> findAllByFactoryId(@Param("factoryId") Long factoryId);

    List<Bay> findByFactoryId(Long factoryId);
}
