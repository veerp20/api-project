package com.cloud.aws.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cloud.aws.model.Invoice;

@Repository
public class InvoiceRepository  {
	

	//@Query("SELECT new com.cloud.aws.model.UserRequest(i.id, i.userid, i.fileName) FROM invoice i WHERE i.userid = :id")
   // List<Invoice> findByUserId(@Param("id") long id);
	//List<Invoice> findByUserid(@Param("userid") long userid);
	//new com.cloud.aws.model.Invoice(i.id,i.userid, i.fileName, i.createdAt, i.invoiceDate, i.invoice_Number, i.totalAmount, i.country)


	//@Query("SELECT i FROM Invoice i WHERE i.userid = :userid")
	//List<Invoice> findByUserid (@Param("userid") long userid);
	//List<Invoice> findByUserid ( long userid);
	@Value("${spring.datasource.url}") 
	private  String URL;
	
	@Value("${spring.datasource.username}")
	private  String USER;
	
	@Value("${spring.datasource.password}")
	public  String Password;
	

    public  List<Invoice> findInvoicesByUserId(Long userId) {
        List<Invoice> invoices = new ArrayList<>();
        
        // SQL query with a placeholder for the user ID
        String sql = "SELECT id, userid,fileName, createdAt, invoiceDate, invoice_Number,  totalAmount,country " +
                     "FROM invoice WHERE userid = ?";

        // Try-with-resources for automatic resource management
        try (Connection connection = DriverManager.getConnection(URL, USER, Password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
             
            // Set the user ID parameter
            preparedStatement.setLong(1, userId);
            
            // Execute the query
            ResultSet resultSet = preparedStatement.executeQuery();

            // Process the results
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long userid = resultSet.getLong("userid");
                String fileName = resultSet.getString("fileName");
                if (fileName.contains(".pdf"))
                	 fileName = fileName.replace(".pdf", "");
                else if (fileName.contains(".png")) 
                	fileName = fileName.replace(".png", "");            
                LocalDate createdAt = resultSet.getDate("createdAt").toLocalDate();
                String invoiceDate = resultSet.getString("invoiceDate");
                String invoice_Number = resultSet.getString("invoice_Number");
                String country = resultSet.getString("country");
                String totalAmount = resultSet.getString("totalAmount");

                // Create an Invoice object and add it to the list
                Invoice invoice = new Invoice(id,userid,fileName,createdAt,invoiceDate,invoice_Number,totalAmount,country,null);
                invoices.add(invoice);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
        
        return invoices;
    }
    
    public  Invoice findInvoicesById(Long Id) {
        List<Invoice> invoices = new ArrayList<>();
        
        // SQL query with a placeholder for the user ID
        String sql = "SELECT id, userid,fileName, createdAt, invoiceDate, invoice_Number,  totalAmount,country,filepath " +
                     "FROM invoice WHERE id = ?";

        // Try-with-resources for automatic resource management
        try (Connection connection = DriverManager.getConnection(URL, USER, Password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
             
            // Set the user ID parameter
            preparedStatement.setLong(1, Id);
            
            // Execute the query
            ResultSet resultSet = preparedStatement.executeQuery();

            // Process the results
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long userid = resultSet.getLong("userid");
                String fileName = resultSet.getString("fileName");           
                LocalDate createdAt = resultSet.getDate("createdAt").toLocalDate();
                String invoiceDate = resultSet.getString("invoiceDate");
                String invoice_Number = resultSet.getString("invoice_Number");
                String country = resultSet.getString("country");
                String totalAmount = resultSet.getString("totalAmount");
                String filePath = resultSet.getString("filepath");

                // Create an Invoice object and add it to the list
                Invoice invoice = new Invoice(id,userid,fileName,createdAt,invoiceDate,invoice_Number,totalAmount,country,filePath);
                invoices.add(invoice);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
        
        return invoices.get(0);
    }
}

