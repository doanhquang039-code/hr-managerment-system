package com.example.hr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {

    private static final Map<String, EmailTemplate> TEMPLATES = new HashMap<>();

    static {
        // Welcome email template
        TEMPLATES.put("WELCOME", new EmailTemplate(
                "WELCOME",
                "Welcome to HR Management System",
                """
                <html>
                <body>
                    <h2>Welcome {{fullName}}!</h2>
                    <p>Your account has been created successfully.</p>
                    <p><strong>Username:</strong> {{username}}</p>
                    <p><strong>Temporary Password:</strong> {{password}}</p>
                    <p>Please change your password after first login.</p>
                    <p>Best regards,<br>HR Team</p>
                </body>
                </html>
                """,
                Arrays.asList("fullName", "username", "password")
        ));

        // Password reset template
        TEMPLATES.put("PASSWORD_RESET", new EmailTemplate(
                "PASSWORD_RESET",
                "Password Reset Request",
                """
                <html>
                <body>
                    <h2>Password Reset Request</h2>
                    <p>Hi {{fullName}},</p>
                    <p>We received a request to reset your password.</p>
                    <p>Your new temporary password is: <strong>{{password}}</strong></p>
                    <p>Please change it after login.</p>
                    <p>If you didn't request this, please contact HR immediately.</p>
                    <p>Best regards,<br>HR Team</p>
                </body>
                </html>
                """,
                Arrays.asList("fullName", "password")
        ));

        // Leave approval template
        TEMPLATES.put("LEAVE_APPROVED", new EmailTemplate(
                "LEAVE_APPROVED",
                "Leave Request Approved",
                """
                <html>
                <body>
                    <h2>Leave Request Approved</h2>
                    <p>Hi {{fullName}},</p>
                    <p>Your leave request has been approved.</p>
                    <p><strong>Leave Type:</strong> {{leaveType}}</p>
                    <p><strong>From:</strong> {{startDate}}</p>
                    <p><strong>To:</strong> {{endDate}}</p>
                    <p><strong>Days:</strong> {{days}}</p>
                    <p>Best regards,<br>HR Team</p>
                </body>
                </html>
                """,
                Arrays.asList("fullName", "leaveType", "startDate", "endDate", "days")
        ));

        // Leave rejection template
        TEMPLATES.put("LEAVE_REJECTED", new EmailTemplate(
                "LEAVE_REJECTED",
                "Leave Request Rejected",
                """
                <html>
                <body>
                    <h2>Leave Request Rejected</h2>
                    <p>Hi {{fullName}},</p>
                    <p>Unfortunately, your leave request has been rejected.</p>
                    <p><strong>Leave Type:</strong> {{leaveType}}</p>
                    <p><strong>From:</strong> {{startDate}}</p>
                    <p><strong>To:</strong> {{endDate}}</p>
                    <p><strong>Reason:</strong> {{reason}}</p>
                    <p>Please contact your manager for more details.</p>
                    <p>Best regards,<br>HR Team</p>
                </body>
                </html>
                """,
                Arrays.asList("fullName", "leaveType", "startDate", "endDate", "reason")
        ));

        // Payslip template
        TEMPLATES.put("PAYSLIP", new EmailTemplate(
                "PAYSLIP",
                "Your Payslip for {{month}}",
                """
                <html>
                <body>
                    <h2>Payslip for {{month}}</h2>
                    <p>Hi {{fullName}},</p>
                    <p>Your payslip for {{month}} is now available.</p>
                    <p><strong>Basic Salary:</strong> {{basicSalary}}</p>
                    <p><strong>Allowances:</strong> {{allowances}}</p>
                    <p><strong>Deductions:</strong> {{deductions}}</p>
                    <p><strong>Net Salary:</strong> {{netSalary}}</p>
                    <p>Please login to the system to view and download your detailed payslip.</p>
                    <p>Best regards,<br>HR Team</p>
                </body>
                </html>
                """,
                Arrays.asList("fullName", "month", "basicSalary", "allowances", "deductions", "netSalary")
        ));

        // Interview invitation template
        TEMPLATES.put("INTERVIEW_INVITATION", new EmailTemplate(
                "INTERVIEW_INVITATION",
                "Interview Invitation",
                """
                <html>
                <body>
                    <h2>Interview Invitation</h2>
                    <p>Dear {{candidateName}},</p>
                    <p>We are pleased to invite you for an interview for the position of <strong>{{position}}</strong>.</p>
                    <p><strong>Date:</strong> {{interviewDate}}</p>
                    <p><strong>Time:</strong> {{interviewTime}}</p>
                    <p><strong>Location:</strong> {{location}}</p>
                    <p><strong>Interviewer:</strong> {{interviewer}}</p>
                    <p>Please confirm your attendance by replying to this email.</p>
                    <p>Best regards,<br>HR Team</p>
                </body>
                </html>
                """,
                Arrays.asList("candidateName", "position", "interviewDate", "interviewTime", "location", "interviewer")
        ));

        // Performance review template
        TEMPLATES.put("PERFORMANCE_REVIEW", new EmailTemplate(
                "PERFORMANCE_REVIEW",
                "Performance Review Scheduled",
                """
                <html>
                <body>
                    <h2>Performance Review Scheduled</h2>
                    <p>Hi {{fullName}},</p>
                    <p>Your performance review has been scheduled.</p>
                    <p><strong>Review Period:</strong> {{reviewPeriod}}</p>
                    <p><strong>Review Date:</strong> {{reviewDate}}</p>
                    <p><strong>Reviewer:</strong> {{reviewer}}</p>
                    <p>Please prepare your self-assessment before the review.</p>
                    <p>Best regards,<br>HR Team</p>
                </body>
                </html>
                """,
                Arrays.asList("fullName", "reviewPeriod", "reviewDate", "reviewer")
        ));
    }

    /**
     * Get all email templates
     */
    public List<EmailTemplate> getAllTemplates() {
        return new ArrayList<>(TEMPLATES.values());
    }

    /**
     * Get template by code
     */
    public EmailTemplate getTemplate(String code) {
        EmailTemplate template = TEMPLATES.get(code);
        if (template == null) {
            throw new RuntimeException("Email template not found: " + code);
        }
        return template;
    }

    /**
     * Render template with variables
     */
    public String renderTemplate(String code, Map<String, String> variables) {
        EmailTemplate template = getTemplate(code);
        String content = template.content();

        // Replace variables
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            content = content.replace(placeholder, entry.getValue());
        }

        return content;
    }

    /**
     * Render subject with variables
     */
    public String renderSubject(String code, Map<String, String> variables) {
        EmailTemplate template = getTemplate(code);
        String subject = template.subject();

        // Replace variables
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            subject = subject.replace(placeholder, entry.getValue());
        }

        return subject;
    }

    /**
     * Validate template variables
     */
    public boolean validateVariables(String code, Map<String, String> variables) {
        EmailTemplate template = getTemplate(code);
        
        for (String requiredVar : template.requiredVariables()) {
            if (!variables.containsKey(requiredVar) || variables.get(requiredVar) == null) {
                log.error("Missing required variable: {}", requiredVar);
                return false;
            }
        }
        
        return true;
    }

    /**
     * Email template record
     */
    public record EmailTemplate(
            String code,
            String subject,
            String content,
            List<String> requiredVariables
    ) {}
}


