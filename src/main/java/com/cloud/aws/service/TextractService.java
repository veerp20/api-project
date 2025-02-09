package com.cloud.aws.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.model.AnalyzeDocumentRequest;
import com.amazonaws.services.textract.model.AnalyzeDocumentResult;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.services.textract.model.S3Object;

@Service
public class TextractService {

	@Autowired
    private final AmazonTextract textractClient;

    public TextractService(AmazonTextract textractClient) {
        this.textractClient = textractClient;
    }

    public List<String> analyzeDocument(String bucketName, String documentName) {
        System.out.println("Bucket: " + bucketName);
        System.out.println("Document: " + documentName);

        S3Object s3Object = new S3Object()
                .withBucket(bucketName)
                .withName(documentName);

        System.out.println("s3Object: " + s3Object);
        Document document = new Document().withS3Object(s3Object);

        System.out.println("document: " + document);
        
        AnalyzeDocumentRequest request = new AnalyzeDocumentRequest()
                .withDocument(document)
                .withFeatureTypes("TABLES", "FORMS","LAYOUT","QUERY");
        System.out.println("request: " + request);

        AnalyzeDocumentResult result = textractClient.analyzeDocument(request);
        
        System.out.println("result: " + result);
        
        return result.getBlocks().stream()
                .filter(block -> "LINE".equals(block.getBlockType()))
                .map(Block::getText)
                .collect(Collectors.toList());
    }
}
