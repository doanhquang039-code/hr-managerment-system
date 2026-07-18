package com.example.hr.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "employeeSoap", namespace = "http://example.com/hr/soap", propOrder = {
    "id",
    "username",
    "fullName",
    "email",
    "phone",
    "position",
    "department",
    "status"
})
public class EmployeeSoap {

    @XmlElement(namespace = "http://example.com/hr/soap")
    private int id;
    
    @XmlElement(namespace = "http://example.com/hr/soap", required = true)
    private String username;
    
    @XmlElement(namespace = "http://example.com/hr/soap", required = true)
    private String fullName;
    
    @XmlElement(namespace = "http://example.com/hr/soap", required = true)
    private String email;
    
    @XmlElement(namespace = "http://example.com/hr/soap")
    private String phone;
    
    @XmlElement(namespace = "http://example.com/hr/soap")
    private String position;
    
    @XmlElement(namespace = "http://example.com/hr/soap")
    private String department;
    
    @XmlElement(namespace = "http://example.com/hr/soap", required = true)
    private String status;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
