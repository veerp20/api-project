package com.cloud.aws.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloud.aws.model.Invoice;

@Repository
public interface InvoiceRepo extends JpaRepository<Invoice, Long> {

	 Optional findById(Long id) ;
	
}
