package com.example.hr.repository;

import com.example.hr.models.Contract;
import com.example.hr.models.ContractExpiryReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractExpiryReminderRepository extends JpaRepository<ContractExpiryReminder, Long> {

    boolean existsByContractAndReminderDays(Contract contract, Integer reminderDays);
}
