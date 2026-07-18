package com.example.hr.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "employee"
})
@XmlRootElement(name = "getEmployeeResponse", namespace = "http://example.com/hr/soap")
public class GetEmployeeResponse {

    @XmlElement(namespace = "http://example.com/hr/soap", required = true)
    private EmployeeSoap employee;

    public EmployeeSoap getEmployee() { return employee; }
    public void setEmployee(EmployeeSoap employee) { this.employee = employee; }
}
