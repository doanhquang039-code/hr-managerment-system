package com.example.hr.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "userId",
    "latitude",
    "longitude"
})
@XmlRootElement(name = "checkInRequest", namespace = "http://example.com/hr/soap")
public class CheckInRequest {

    @XmlElement(namespace = "http://example.com/hr/soap")
    private int userId;
    
    @XmlElement(namespace = "http://example.com/hr/soap")
    private Double latitude;
    
    @XmlElement(namespace = "http://example.com/hr/soap")
    private Double longitude;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
