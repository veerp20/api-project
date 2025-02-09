package com.cloud.aws.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.s3.AmazonS3;
import com.cloud.aws.model.Invoice;
import com.cloud.aws.model.InvoiceResponse;
import com.cloud.aws.model.User;

@Service
public class PDFExtractorService {

	@Autowired
	private DynamicSchemaService dynamicSchemaService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private  TextractService textractService ;

	@Autowired
	private AmazonS3 amazonS3;

	@Value("${cloud.aws.s3.bucket}")
	private String BUCKET_NAME;

	@Value("${file.upload-dir}")
	private String uploadDir;

	@Transactional
	public Map<String, Object> extractData(InputStream inputStream, String fileName, long id, File localFile)
			throws Exception {
		User user = null;
		try (PDDocument document = PDDocument.load(inputStream)) {
			PDFTextStripper stripper = new PDFTextStripper();
			String text = stripper.getText(document);
			PDFRenderer pdfRenderer = new PDFRenderer(document);

			Path localfile = localFile.toPath();
			String path = localfile.toString();
			String localFilePath = path.replace(".pdf", "");
			for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
				BufferedImage bim = pdfRenderer.renderImageWithDPI(pageIndex, 300); // Render with 300 DPI
				ImageIO.write(bim, "PNG", new File(localFilePath + ".png"));
				// imagePaths.append(imagePath).append("\n");
			}
			user = userService.isFindById(id);
			Map<String, Object> extractedData = new HashMap<>();
			extractedData.put("userid", user.getId());
			extractedData.put("filePath", localFilePath);
			// Extract invoice details
			extractedData.put("invoice_Number", extractField(text, "Invoice\\s*Number\\s*([\\w-]+)"));
			extractedData.put("invoiceDate", extractValue(text, "Invoice Date\\s*(\\S+)"));

			extractedData.put("companyName", extractMultiLineField(text, "Company\\s*([\\s\\S]*?)(?=\\r?\\n|$)"));

			// Extract billing/shipping information
			extractedData.put("name", extractField(text, "Name\\s*([\\w\\s]+)"));
			extractedData.put("address", extractMultiLineField(text, "Address\\s*([\\s\\S]*?)(?=Email|$)"));
			extractedData.put("city", extractField(text, "([\\w\\s]+),\\s*[\\w\\s]+,\\s*\\d{5}"));
			extractedData.put("state", extractField(text, ",\\s*([\\w\\s]+),\\s*\\d{5}"));
			extractedData.put("zipCode", extractField(text, "(\\d{5})"));
			extractedData.put("country", extractField(text, "(United\\s*States)"));
			extractedData.put("email", extractField(text, "([\\w.-]+@[\\w.-]+\\.[\\w]+)"));

			// Extract total amount
			extractedData.put("totalAmount", extractField(text, "Total\\s*\\$(\\d+\\.\\d{2})"));

			// Extract product details
			extractedData.put("products", extractProducts(text));

			// Save extracted invoice data to the database
			saveInvoiceData(extractedData, fileName);

			return extractedData;
		}
	}

	@Transactional
	public void saveInvoiceData(Map<String, Object> extractedData, String fileName) throws Exception {
		// Assuming 'invoice' is the table name
		String tableName = "invoice";
		Object filePathObject = extractedData.get("filePath");
		filePathObject +=".pdf";
		File file = null;

		if (filePathObject instanceof String) {
			file = new File((String) filePathObject);
		} else if (filePathObject instanceof File) {
			file = (File) filePathObject;
		}

		if (file != null && file.exists()) {
			List<String> analyzeDocument = textractService.analyzeDocument(BUCKET_NAME, fileName);
			amazonS3.putObject(BUCKET_NAME, file.getName(), file);
			System.out.println("File uploaded: " + file.getName());
			//List<String> analyzeDocument = textractService.analyzeDocument(BUCKET_NAME, fileName);
		} else {
			System.out.println("File does not exist or file path is not provided.");
		}
		// Convert extractedData to String-based Map for DynamicSchemaService
		Map<String, String> stringData = new HashMap<>();
		extractedData.forEach((key, value) -> {
			// Convert value to string, handle null values
			stringData.put(key, value != null ? value.toString() : "");
		});

		// Add current date and time to the data map
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String currentDateTime = LocalDateTime.now().format(formatter);
		stringData.put("createdAt", currentDateTime);

		stringData.put("fileName", fileName);

		// Use the DynamicSchemaService to create/update the table and insert data
		dynamicSchemaService.updateSchemaAndStoreData(tableName, stringData);
	}

	private String extractField(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	private String extractMultiLineField(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			return matcher.group(1).trim();
		}
		return null;
	}

	private List<Map<String, String>> extractProducts(String text) {
		List<Map<String, String>> products = new ArrayList<>();
		Pattern pattern = Pattern.compile("(.*?)\\$([\\d.]+)\\s+(\\d+)\\s+\\$([\\d.]+)");
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			Map<String, String> product = new HashMap<>();
			product.put("description", matcher.group(1).trim());
			product.put("unitPrice", matcher.group(2));
			product.put("quantity", matcher.group(3));
			product.put("totalPrice", matcher.group(4));
			products.add(product);
		}

		return products;
	}

	public File getPDFById(long id, Invoice inv) throws FileNotFoundException {
		User user = userService.isFindById(id);
		File pdfFile = null;
		File directory = null;
		if (user != null) {
			if (inv != null) {
				if (inv.getUserid().equals(id)) {
					/*
					 * String uploadDir1 = System.getProperty("user.dir") ; uploadDir +=
					 * "\\src\\main\\resources\\uploads\\";
					 */
					directory = new File(uploadDir);
					System.out.println("******************" + directory + "*************");
					if (inv.getFileName() != null)
						if (!directory.exists()) {
							directory.mkdirs();
							System.out.println(directory.mkdirs());
						}
					pdfFile = new File(directory, inv.getFileName());
					System.out.println("**************" + pdfFile + "*********");
				}
			}
		} else
			throw new FileNotFoundException("user is not varified: " + id);
		if (!pdfFile.exists()) {
			throw new FileNotFoundException("PDF not found fro this User: " + user.getName());
		}
		return pdfFile;
	}

	@SuppressWarnings("null")
	public List<Invoice> getViewData(long id) {
		User user = userService.isFindById(id);
		List<Invoice> invoice = null;
		if (user != null) {
			invoice = userService.retrieveDataByUserId(user.getId());
			if (invoice == null && invoice.isEmpty()) {
				throw new UsernameNotFoundException("Invoice data is not found: " + user.getName());
			}
			/*
			 * if (invoice != null && !invoice.isEmpty()) { for (int i = 0; i <
			 * invoice.size(); i++) { if (invoice.get(i).getUserid() == (user.getId())) {
			 * return invoice; } } }else throw new
			 * UsernameNotFoundException("View data is not found: " + user.getName());
			 */
		} else
			throw new UsernameNotFoundException("user is not found: " + user.getName());

		return invoice;
	}

	private static String extractValue(String content, String regex) {
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);
		return matcher.find() ? matcher.group(1).trim() : "";
	}

	public InvoiceResponse getPdfDetails(long userId, long pdfId) {
		InvoiceResponse response=null;
		try {
			// Retrieve the invoice
			Invoice invoice = userService.retrieveDataById(pdfId);

			// Get the PDF file
			File pdfFile = getPDFById(userId, invoice);

			// Read the PDF into a byte array

			// Convert the PDF to a PNG image and encode it to Base64
			String imageData;
			try (PDDocument document = PDDocument.load(pdfFile)) {
				PDFRenderer pdfRenderer = new PDFRenderer(document);
				BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 300); // First page, 300 DPI

				// Convert the image to a ByteArrayOutputStream
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, "PNG", baos);
				byte[] imageBytes = baos.toByteArray();

				// Encode to Base64
				imageData = Base64.getEncoder().encodeToString(imageBytes);
			}

			// Set the file path in the invoice object
			String local = pdfFile.getAbsolutePath() + ".png";
			if (!pdfFile.getName().contains(".png")) {
				local = local.replace(".pdf", "");
			}
			local = local.replace("\\", "//");
			invoice.setFilePath(local);

			// Create the response object
			 response = new InvoiceResponse(invoice, imageData);
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	public void uploadInvoicePdf(File file, long id,String filePath) throws Exception {
				Map<String, Object> extractedData =null;
				// String uploadDir = "D:\\SpringTest\\CloudWebApplication\\src\\main\\resources\\uploads";
				/*
				 * String uploadDir = System.getProperty("user.dir") ; uploadDir +=
				 * "\\src\\main\\resources\\uploads\\";
				 */
				File directory =null;
				try {
					// Check if file is not empty
					
					if(!file.getName().contains("pdf")) {		
						User user = userService.isFindById(id);
						  directory = new File(uploadDir);

						// Create the directory if it doesn't exist
						if (!directory.exists()) {
							directory.mkdirs();
						}
						
						if(user.getEmail() != null) {
						// Save the file locally
						//String filePath = uploadDir + "\\" + file.getName();
						System.out.println( uploadDir  );
						File localFile = new File(filePath);
						 extractedData = new HashMap<>();
						 extractedData.put("userId", user.getId());
						 extractedData.put("user_id", user.getId());
						 extractedData.put("filePath", filePath);
						 saveInvoiceData(extractedData, file.getName());
							System.out.println("File saved at: " + localFile.getAbsolutePath());
						}
					}else {
					// Extract data from PDF
						File file1 = new File(filePath);
						 InputStream inputStream = new FileInputStream(file);
						//pdfExtractorService.convertPdfToImage(inputStream,uploadDir);
					
					// Save the file to local directory (src/main/resources)
					 directory = new File(uploadDir);

					// Create the directory if it doesn't exist
					if (!directory.exists()) {
						directory.mkdirs();
					}

					// Save the file locally
					 //String filePath = uploadDir + "\\" + file.getName();
					 System.out.println("*****************+uploadDir+****************");
					 File localFile = new File(filePath);
					 extractedData = extractData(inputStream,
							file.getName(),id,file1);
								
						System.out.println("File saved at: " + localFile.getAbsolutePath());
					}
				}catch (FileNotFoundException fnf) {
					fnf.getMessage();
				}
		  }
}
