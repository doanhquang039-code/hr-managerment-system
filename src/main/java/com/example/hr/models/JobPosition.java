package com.example.hr.models;

import jakarta.persistence.*; // Sử dụng jakarta cho Spring Boot 3+
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobposition")
@Data
@NoArgsConstructor  // Cần thiết cho JPA
@AllArgsConstructor // Tiện cho việc khởi tạo nhanh
public class JobPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "position_name", nullable = false, length = 100)
    private String positionName;

    @Column(name = "job_level")
    private Integer jobLevel = 1;

    // Sử dụng columnDefinition để khớp chính xác với DECIMAL(5, 2) trong SQL
    @Column(name = "allowance_coeff", precision = 5, scale = 2)
    private BigDecimal allowanceCoeff = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY) // Nên dùng Lazy để tối ưu hiệu năng
    @JoinColumn(name = "next_position_id")
    private JobPosition nextPosition;
    @Column(name = "active")
    private Boolean active = true; // Mặc định là true khi tạo mới

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	public Integer getJobLevel() {
		return jobLevel;
	}

	public void setJobLevel(Integer jobLevel) {
		this.jobLevel = jobLevel;
	}

	public BigDecimal getAllowanceCoeff() {
		return allowanceCoeff;
	}

	public void setAllowanceCoeff(BigDecimal allowanceCoeff) {
		this.allowanceCoeff = allowanceCoeff;
	}

	public JobPosition getNextPosition() {
		return nextPosition;
	}

	public void setNextPosition(JobPosition nextPosition) {
		this.nextPosition = nextPosition;
	}
    
}