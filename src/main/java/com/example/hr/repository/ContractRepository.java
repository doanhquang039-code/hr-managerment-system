package com.example.hr.repository;
import org.springframework.data.domain.Sort;
import com.example.hr.models.Contract;
import com.example.hr.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer> {

    @Query("SELECT c FROM Contract c LEFT JOIN FETCH c.user WHERE (:keyword IS NULL OR c.contractType LIKE %:keyword%)")
    List<Contract> findAllWithUser(@Param("keyword") String keyword);

    List<Contract> findByUser(User user);
    @Query("SELECT c FROM Contract c WHERE " +
           "(:employeeName IS NULL OR c.user.fullName LIKE %:employeeName%) AND " +
           "(:type IS NULL OR c.contractType = :type)")
    List<Contract> filterContracts(String employeeName, String type, String status, Sort sort);
}