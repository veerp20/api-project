package com.cloud.aws.model;

import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Invoice {
  
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
	private Long userid;  // Make sure this matches exactly
    private String fileName;
    private LocalDate createdAt;
    private String invoiceDate;
    private String invoice_Number;
    private String totalAmount;
    private String country;
    private String filePath;
    
    
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getUserid() {
		return userid;
	}
	public void setUserid(Long userid) {
		this.userid = userid;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public LocalDate getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}
	public String getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public String getInvoice_Number() {
		return invoice_Number;
	}
	public void setInvoice_Number(String invoice_Number) {
		this.invoice_Number = invoice_Number;
	}
	public String getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	@Override
	public String toString() {
		return "Invoice [id=" + id + ", userid=" + userid + ", fileName=" + fileName + ", createdAt=" + createdAt
				+ ", invoiceDate=" + invoiceDate + ", invoice_Number=" + invoice_Number + ", totalAmount=" + totalAmount
				+ ", country=" + country + ", filePath=" + filePath + "]";
	}
	public Invoice(Long id, Long userid, String fileName, LocalDate createdAt2, String invoiceDate, String invoice_Number,
			String totalAmount, String country, String filePath) {
		super();
		this.id = id;
		this.userid = userid;
		this.fileName = fileName;
		this.createdAt = createdAt2;
		this.invoiceDate = invoiceDate;
		this.invoice_Number = invoice_Number;
		this.totalAmount = totalAmount;
		this.country = country;
		this.filePath = filePath;
	}
 
    public Invoice() {
		// TODO Auto-generated constructor stub
	}
    
    
}
