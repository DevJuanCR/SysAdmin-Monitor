package com.sysadmin.monitor.repository;

import com.sysadmin.monitor.dto.MetricSummaryDTO;
import com.sysadmin.monitor.entity.SystemMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemMetricRepository extends JpaRepository<SystemMetric, Long> {

    List<SystemMetric> findTop20ByOrderByTimestampDesc();

    List<SystemMetric> findTop20ByHostnameOrderByTimestampDesc(String hostname);

    @Query("SELECT DISTINCT m.hostname FROM SystemMetric m ORDER BY m.hostname")
    List<String> findDistinctHostnames();

    @Query("SELECT new com.sysadmin.monitor.dto.MetricSummaryDTO(m.hostname, AVG(m.cpuUsage), MAX(m.cpuUsage), AVG(m.ramUsage), MAX(m.ramUsage), AVG(m.diskUsage), MAX(m.diskUsage)) FROM SystemMetric m GROUP BY m.hostname ORDER BY m.hostname")
    List<MetricSummaryDTO> findSummaryByHost();
}