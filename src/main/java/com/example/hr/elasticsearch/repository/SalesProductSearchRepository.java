package com.example.hr.elasticsearch.repository;

import com.example.hr.elasticsearch.document.SalesProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesProductSearchRepository extends ElasticsearchRepository<SalesProductDocument, Integer> {
    
    List<SalesProductDocument> findByNameContainingOrDescriptionContaining(String name, String description);
}
