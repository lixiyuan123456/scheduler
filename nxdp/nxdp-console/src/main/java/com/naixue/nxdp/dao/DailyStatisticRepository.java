package com.naixue.nxdp.dao;

import java.util.List;

import com.naixue.nxdp.model.DailyStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyStatisticRepository extends JpaRepository<DailyStatistic, Integer> {

    void deleteByDate(String date);

    List<DailyStatistic> findByDateBetweenOrderByDateAsc(String start, String end);
}
