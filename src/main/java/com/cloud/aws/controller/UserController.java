package com.cloud.aws.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cloud.aws.model.ApiResponse;
import com.cloud.aws.model.FileData;
import com.cloud.aws.model.Invoice;
import com.cloud.aws.model.InvoiceResponse;
import com.cloud.aws.model.User;
import com.cloud.aws.model.UserRequest;
import com.cloud.aws.service.PDFExtractorService;
import com.cloud.aws.service.TextractService;
import com.cloud.aws.service.UserService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class UserController {
	@Autowired
	private UserService userService;
	
	@Autowired
	private PDFExtractorService pdfExtractorService;
	
	@Autowired
	private  TextractService textractService ;
	
	@Autowired
	 private HttpServletResponse response;
	
	 @Value("${file.upload-dir}")
	    private String uploadDir;
	
	 @PostMapping("/sign-up")
	public ResponseEntity<ApiResponse> createUser(@RequestBody User user) {
		user.setCreateDt(LocalDateTime.now());
		boolean isValidUser = userService.validateUserPwd(user.getEmail());
		if (isValidUser) {
			ApiResponse response = new ApiResponse(false, "User already created ", user);

			return new ResponseEntity<>(response, HttpStatus.FOUND);
		} else {

			User createdUser = userService.createUser(user);
			createdUser.setPassword(null);
			// Assuming createUser returns the created user's ID or some identifier
			ApiResponse response = new ApiResponse(true, "User created successfully", createdUser);

			return new ResponseEntity<>(response, HttpStatus.CREATED);
		}
	}

	@PostMapping("/sign-in")
	public ResponseEntity<ApiResponse> Sign(@RequestBody UserRequest userRequest) {
		boolean isValidUser = userService.validateUser(userRequest.getEmail(),userRequest.getPassword());
		User user = userService.isEmailVerified(userRequest.getEmail());
		user.setPassword(null);
		if (isValidUser) {			
			ApiResponse response = new ApiResponse(isValidUser, "Login successfully", user);

			return new ResponseEntity<>(response, HttpStatus.OK);
		}else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
			        .body(new ApiResponse(isValidUser, "Access denied", user));
	}
	}
	@PutMapping("/reset")
	public ResponseEntity<ApiResponse> loginPwd(@RequestParam String emailid) {
		boolean isValidUserpwd = userService.validateUserPwd(emailid);
		if (isValidUserpwd) {
			ApiResponse response = new ApiResponse(isValidUserpwd, "Reset Pwd", null);

			return new ResponseEntity<>(response, HttpStatus.OK);
		} else{
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
			        .body(new ApiResponse(isValidUserpwd, "Access denied", null));
	}
	}
	
	@PutMapping("/update-user")
	public ResponseEntity<ApiResponse> UpdateUser(@RequestBody User user) {
		boolean isUser =false;
		//user.setCreateDt(LocalDateTime.now());
		User updateUser = userService.updateUser(user);
		if (updateUser !=null) {
			isUser=true;
			ApiResponse response = new ApiResponse(isUser, "Password Reset successfully", updateUser);

			return new ResponseEntity<>(response, HttpStatus.OK);
		} else{
			isUser = false;
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
			        .body(new ApiResponse(isUser, "Access denied", updateUser));
	}
	}

	@PostMapping("/upload-file")
	public ResponseEntity<ApiResponse> uploadPdfData(@RequestBody FileData fileData) {
		File directory =null;
		try {
            byte[] pdfData = Base64.getDecoder().decode(fileData.getText());
			/*
			 * String uploadDir = System.getProperty("user.dir") ; uploadDir +=
			 * "\\src\\main\\resources\\uploads\\";
			 */
            
            String filePath = uploadDir+fileData.getFileName(); // Define your file path here
            directory = new File(uploadDir);
            if (!directory.exists()) {
				directory.mkdirs();
				System.out.println(directory.mkdirs());
			}
		    File  file=  new File(filePath);
		   int pageIndex=0;
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfData);
            }
            try (PDDocument document = PDDocument.load(new File(filePath))) {
                if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) {
                    return ResponseEntity.badRequest().body(null);
                }

                PDDocument singlePageDocument = new PDDocument();
                PDPage page = document.getPage(pageIndex); // Get the specified page
                singlePageDocument.addPage(page); // Add it to a new document
                singlePageDocument.save(filePath); // Save the new document
                singlePageDocument.close();}catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(null);
   }
             pdfExtractorService.uploadInvoicePdf(file,fileData.getUserId(),filePath);
       	 
			return ResponseEntity.status(HttpStatus.OK)
			        .body(new ApiResponse(true, "PDF Uploaded successfully", null));

	} catch (Exception e) {
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		        .body(new ApiResponse(false, "Internal server error", null));
	}

	}
		  
		  
	

	@PostMapping("/upload")
	public ResponseEntity<ApiResponse> uploadInvoice(@RequestParam("file") MultipartFile file,@RequestParam long id) {
		Map<String, Object> extractedData =null;
		// String uploadDir = "D:\\SpringTest\\CloudWebApplication\\src\\main\\resources\\uploads";
		/*
		 * String uploadDir = System.getProperty("user.dir") ; uploadDir +=
		 * "\\src\\main\\resources\\uploads\\";
		 *
		 */
		File directory =null;
		try {
			// Check if file is not empty
			
			if(!file.getOriginalFilename().contains("pdf")) {		
				User user = userService.isFindById(id);
				  directory = new File(uploadDir);

				// Create the directory if it doesn't exist
				  if (!directory.exists()) {
						directory.mkdirs();
						System.out.println(directory.mkdirs());
					}
				
				if(user.getEmail() != null) {
				// Save the file locally
				String filePath = uploadDir + "\\" + file.getOriginalFilename();
				System.out.println( uploadDir  );
				File localFile = new File(filePath);
				 extractedData = new HashMap<>();
				 extractedData.put("userId", user.getId());
				 extractedData.put("filePath", filePath);
				 pdfExtractorService.saveInvoiceData(extractedData, file.getOriginalFilename());
				 file.transferTo(localFile);
					System.out.println("File saved at: " + localFile.getAbsolutePath());
					return ResponseEntity.status(HttpStatus.OK)
					        .body(new ApiResponse(true, "Image Uploaded successfully", null));
				}
			}else {
			// Extract data from PDF
				InputStream inputStream = file.getInputStream();
				//pdfExtractorService.convertPdfToImage(inputStream,uploadDir);
			
			// Save the file to local directory (src/main/resources)
			 directory = new File(uploadDir);
			 System.out.println(directory);

			// Create the directory if it doesn't exist
			if (!directory.exists()) {
				directory.mkdirs();
				System.out.println(directory.mkdirs());
			}

			// Save the file locally
			 String filePath = uploadDir + "\\" + file.getOriginalFilename();
			 System.out.println("*****************+uploadDir+****************");
			 File localFile = new File(filePath);
			 extractedData = pdfExtractorService.extractData(inputStream,
					file.getOriginalFilename(),id,localFile);
							
				file.transferTo(localFile);
				System.out.println("File saved at: " + localFile.getAbsolutePath());
			
				return ResponseEntity.status(HttpStatus.OK)
				        .body(new ApiResponse(true, "PDF Uploaded successfully", null));
			}

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			        .body(new ApiResponse(false, "Internal server error", null));
		}
		return null;
	}

	@GetMapping("/get-pdf-detail")
	public ResponseEntity<InvoiceResponse> getPDFile(@RequestParam long userId, @RequestParam(required = false) long pdfId)  {
		InvoiceResponse response = pdfExtractorService.getPdfDetails(userId,pdfId);
		if(response!=null) {
	        return ResponseEntity.ok(response);
	    } else {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
	@GetMapping("/get-data")
	public ResponseEntity<List<Invoice>> getViewdata(@RequestParam long id) {
		List<Invoice> invoice = pdfExtractorService.getViewData(id);
		if (invoice.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // No invoices found
		}

		return ResponseEntity.ok(invoice); // Return the list of invoices
	}
	
	

    public List<String> analyzeDocument(@RequestParam String bucket, @RequestParam String document) {
        return textractService.analyzeDocument(bucket, document);
    }
    
  
			
}