package com.example.hr.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "status",
    "checkInTime"
})
@XmlRootElement(name = "checkInResponse", namespace = "http://example.com/hr/soap")
public class CheckInResponse {

    @XmlElement(namespace = "http://example.com/hr/soap", required = true)
    private String status;
    
    @XmlElement(namespace = "http://example.com/hr/soap", required = true)
    private String checkInTime;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
}
