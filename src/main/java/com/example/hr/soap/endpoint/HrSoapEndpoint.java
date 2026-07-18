package com.example.hr.soap.endpoint;

import com.example.hr.enums.AttendanceStatus;
import com.example.hr.models.User;
import com.example.hr.user.repository.UserRepository;
import com.example.hr.attendance.entity.Attendance;
import com.example.hr.attendance.repository.AttendanceRepository;
import com.example.hr.soap.dto.CheckInRequest;
import com.example.hr.soap.dto.CheckInResponse;
import com.example.hr.soap.dto.EmployeeSoap;
import com.example.hr.soap.dto.GetEmployeeRequest;
import com.example.hr.soap.dto.GetEmployeeResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Endpoint
public class HrSoapEndpoint {

    private static final String NAMESPACE_URI = "http://example.com/hr/soap";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getEmployeeRequest")
    @ResponsePayload
    public GetEmployeeResponse getEmployee(@RequestPayload GetEmployeeRequest request) {
        GetEmployeeResponse response = new GetEmployeeResponse();
        
        Optional<User> userOpt = userRepository.findById(request.getId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            EmployeeSoap employeeSoap = new EmployeeSoap();
            employeeSoap.setId(user.getId());
            employeeSoap.setUsername(user.getUsername());
            employeeSoap.setFullName(user.getFullName());
            employeeSoap.setEmail(user.getEmail());
            employeeSoap.setPhone(user.getPhoneNumber());
            employeeSoap.setPosition(user.getPosition() != null ? user.getPosition().getPositionName() : null);
            employeeSoap.setDepartment(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null);
            employeeSoap.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
            
            response.setEmployee(employeeSoap);
        }
        
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "checkInRequest")
    @ResponsePayload
    public CheckInResponse checkIn(@RequestPayload CheckInRequest request) {
        CheckInResponse response = new CheckInResponse();
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));
        
        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByUserAndAttendanceDate(user, today);
        
        Attendance attendance;
        if (existing.isEmpty()) {
            attendance = new Attendance();
            attendance.setUser(user);
            attendance.setAttendanceDate(today);
            LocalTime now = LocalTime.now();
            attendance.setCheckInTime(now);

            LocalTime lateThreshold = LocalTime.of(8, 30);
            attendance.setStatus(now.isAfter(lateThreshold)
                    ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);

            attendance = attendanceRepository.save(attendance);
        } else {
            attendance = existing.get();
        }
        
        response.setStatus(attendance.getStatus().name());
        response.setCheckInTime(attendance.getCheckInTime() != null ? attendance.getCheckInTime().toString() : "");
        
        return response;
    }
}
