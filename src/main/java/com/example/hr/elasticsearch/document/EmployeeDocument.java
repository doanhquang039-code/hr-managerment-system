package com.example.hr.elasticsearch.document;


import com.example.hr.department.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.List;

/**
 * Employee Elasticsearch Document
 * Document cho full-text search employees
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "employees")
public class EmployeeDocument {

    @Id
    private Integer id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String fullName;

    @Field(type = FieldType.Keyword)
    private String username;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Text)
    private String phone;

    @Field(type = FieldType.Keyword)
    private String department;

    @Field(type = FieldType.Keyword)
    private String position;

    @Field(type = FieldType.Text, analyzer = "standard")
    private List<String> skills;

    @Field(type = FieldType.Date)
    private LocalDate hireDate;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String address;

    @Field(type = FieldType.Double)
    private Double salary;
}
