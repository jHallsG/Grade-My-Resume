package com.cvlens.InsightsAI.service.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
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
		
		String resumeContent = jdbcTemplate.queryForObject("SELECT content FROM vector_store", String.class);
		
		Message systemMessage = new SystemMessage(""
				+ "You are an AI-powered resume analysis assistant. Your task is to evaluate resumes against a given job description, highlighting strengths, identifying gaps, and suggesting improvements.\r\n"
				+ "\r\n"
				+ "Analyze work experience, technical skills, and education, ensuring responses are factual, structured, and neutral. Do not assume missing details; instead, point them out.\r\n"
				+ "\r\n"
				+ "At the end, provide a rating from 1 to 10, where:\r\n"
				+ "\r\n"
				+ "1 = Very poor match (few/no relevant qualifications).\r\n"
				+ "\r\n"
				+ "5 = Partial match (some relevant skills, key gaps remain).\r\n"
				+ "\r\n"
				+ "8 = Strong match (meets most requirements, minor gaps).\r\n"
				+ "\r\n"
				+ "10 = Perfect match (fully aligned with job needs).\r\n"
				+ "\r\n"
				+ "Justify your rating with specific reasons based on the resume's content."
				+ "\r\n"
				+ "The resume content are as follows : \r\n\n\n" + resumeContent);
		
		Message jobDescriptionContent = new UserMessage(jobDescription);
		
		Prompt prompts = new Prompt(systemMessage, jobDescriptionContent);
		
		String result = chatModel.call(prompts).getResult().getOutput().getText();
		
		return result;
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
