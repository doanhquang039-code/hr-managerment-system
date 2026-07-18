package com.example.hr.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "id"
})
@XmlRootElement(name = "getEmployeeRequest", namespace = "http://example.com/hr/soap")
public class GetEmployeeRequest {

    @XmlElement(namespace = "http://example.com/hr/soap")
    private int id;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}
