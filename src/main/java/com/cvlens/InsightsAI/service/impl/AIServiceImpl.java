package com.cvlens.InsightsAI.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
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

	// the methods extracts the vector database contents (basically the contents of the resume) and compares it to the job description
	// doesn't make use of vector embeddings. i'm thinking using a vector database for this approach is overkill.
	@Override
	public String processJobDescription(String jobDescription) {
		
		String resumeContent = "";
		
		try {
		    resumeContent = jdbcTemplate.queryForObject("SELECT content FROM vector_store", String.class);
		} catch (EmptyResultDataAccessException e) {
			logger.error("No CV / resume content found in vector_store.", e);
		}
		
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

	// this method accepts a cv / resume file, extracts the texts and creates the necessary vector embeddings
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
	
	// the method takes a user's query, pairs it with the system prompt and generate AI appropriate answers.
	public String askAI(String userQuery) {
		
		// take the user's query, convert it to vector numbers, and retrieve context based on number similarities
		List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
				.query(userQuery)
				// play around with the topK() configurations to achieve max compatibility, lower is more focused, higher provides a bit of context
				// do note a higher topK value can lead to AI hallucinations
				.topK(5)		
				.build());
		
		// retrieve similar texts/ context
		String retrievedContext = documents.stream()
				.map(document -> document.getFormattedContent())
				.collect(Collectors.joining("\n"));
		
		logger.info("Entries retrieved : " + String.valueOf(documents.size()));
		logger.info("Entries retrieved are as follows : \n\n\n" + retrievedContext + "\n\n\n");
		
		Message systemPrompt = new SystemMessage(""
				+ "Take the role of a professional resume or CV reviewer. Answer the user's query or questions as an impartial professional."
				+ "Provide critiques, reviews and insights. Use the provided resume or CV for reference : \n\n\n" + retrievedContext);
		
		Message userPrompt = new UserMessage(userQuery);
		
		Prompt prompts = new Prompt(systemPrompt, userPrompt);
		
		String result = chatModel.call(prompts).getResult().getOutput().getText();
		
		return result;
	}
}
