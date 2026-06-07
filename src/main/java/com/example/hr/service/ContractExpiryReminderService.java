package com.example.hr.service;

import com.example.hr.enums.NotificationType;
import com.example.hr.enums.Role;
import com.example.hr.enums.UserStatus;
import com.example.hr.models.Contract;
import com.example.hr.models.ContractExpiryReminder;
import com.example.hr.models.User;
import com.example.hr.repository.ContractExpiryReminderRepository;
import com.example.hr.repository.ContractRepository;
import com.example.hr.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ContractExpiryReminderService {

    private static final int[] REMINDER_DAYS_BEFORE = {30, 14, 7};

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ContractExpiryReminderRepository reminderRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Gá»­i nháº¯c háº¿t háº¡n há»£p Ä‘á»“ng cho nhÃ¢n viÃªn vÃ  Admin/Manager (má»—i má»‘c 30/14/7 ngÃ y chá»‰ má»™t láº§n / há»£p Ä‘á»“ng).
     */
    @Transactional
    public void sendDueReminders(LocalDate today) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<User> hrRecipients = userRepository.findByRoleInAndStatus(
                List.of(Role.ADMIN, Role.MANAGER),
                UserStatus.ACTIVE);

        for (int days : REMINDER_DAYS_BEFORE) {
            LocalDate expiryOnThatDay = today.plusDays(days);
            List<Contract> contracts = contractRepository.findAllByExpiryDateWithActiveUser(expiryOnThatDay, UserStatus.ACTIVE);

            for (Contract contract : contracts) {
                if (reminderRepository.existsByContractAndReminderDays(contract, days)) {
                    continue;
                }

                User employee = contract.getUser();
                String employeeName = employee != null ? employee.getFullName() : "N/A";
                String expiryStr = contract.getExpiryDate() != null ? contract.getExpiryDate().format(fmt) : "";
                String baseMsg = String.format(
                        "Há»£p Ä‘á»“ng #%d (%s) cá»§a %s háº¿t háº¡n sau %d ngÃ y â€” ngÃ y %s.",
                        contract.getId(),
                        contract.getContractType() != null ? contract.getContractType() : "",
                        employeeName,
                        days,
                        expiryStr);

                if (employee != null) {
                    notificationService.createNotification(
                            employee,
                            "ðŸ“„ " + baseMsg,
                            NotificationType.WARNING,
                            "/user1/profile");
                }

                for (User hr : hrRecipients) {
                    notificationService.createNotification(
                            hr,
                            "ðŸ“„ [HR] " + baseMsg,
                            NotificationType.WARNING,
                            "/admin/contracts");
                }

                ContractExpiryReminder sent = new ContractExpiryReminder();
                sent.setContract(contract);
                sent.setReminderDays(days);
                sent.setSentAt(LocalDateTime.now());
                reminderRepository.save(sent);
            }
        }
    }
}


