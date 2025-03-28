package com.cvlens.InsightsAI.service.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cvlens.InsightsAI.service.AIServices;

@Service
public class AIServiceImpl implements AIServices{
	
	private final ChatModel chatModel;
	private final VectorStore vectorStore;
	private final JdbcTemplate jdbcTemplate;
	
	Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);
	
	public AIServiceImpl(ChatModel chatModel, VectorStore vectorStore, JdbcTemplate jdbcTemplate) {
		this.chatModel = chatModel;
		this.vectorStore = vectorStore;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public String processJobDescription(String jobDescription) {
		
		return "yolo";
	}

	@Override
	public String processFile(MultipartFile file) throws IOException {
		
		if (file.isEmpty()) {
			logger.error("Empty file cannot be processed. Please upload a valid file.");
			throw new IllegalArgumentException("Uploaded file is empty.");
		}
		
		logger.info("Processing file. Please wait...");
		
		Resource resource = file.getResource();
		
		TikaDocumentReader reader = new TikaDocumentReader(resource);
		
		// ensure vector_store table is empty to avoid data mix-up
		jdbcTemplate.update("TRUNCATE TABLE vector_store");
		
		// initialize splitter. this will split the text to separate chunks
		TextSplitter splitter = new TokenTextSplitter();

		// split the texts to chunks
		var chunks = splitter.apply(reader.get());
		
		logger.info("Uploading chunks to Vector Database. Please wait...");
					
		// feed the chunks to the vector_database
		vectorStore.accept(chunks);
		
		return "File has been processed successfully";
		
	}
}
