package com.example.hr.department.entity;

import com.example.hr.models.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "department")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id")
    private User manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    @Column(name = "employee_count", nullable = false)
    private Integer employeeCount = 0;

    @Column(name = "working_hours", length = 100)
    private String workingHours;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by_encrypted", length = 1000)
    private String createdByEncrypted;

    @Column(name = "updated_by_encrypted", length = 1000)
    private String updatedByEncrypted;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (employeeCount == null) {
            employeeCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (employeeCount == null) {
            employeeCount = 0;
        }
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public User getManager() {
		return manager;
	}

	public void setManager(User manager) {
		this.manager = manager;
	}

	public Department getParentDepartment() {
		return parentDepartment;
	}

	public void setParentDepartment(Department parentDepartment) {
		this.parentDepartment = parentDepartment;
	}

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedByEncrypted() {
        return createdByEncrypted;
    }

    public void setCreatedByEncrypted(String createdByEncrypted) {
        this.createdByEncrypted = createdByEncrypted;
    }

    public String getUpdatedByEncrypted() {
        return updatedByEncrypted;
    }

    public void setUpdatedByEncrypted(String updatedByEncrypted) {
        this.updatedByEncrypted = updatedByEncrypted;
    }

    // Alias method for compatibility with services
    public String getName() {
        return this.departmentName;
    }
    
}


