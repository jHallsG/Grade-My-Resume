package com.cvlens.InsightsAI.service.impl;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;

import com.cvlens.InsightsAI.service.AIServices;

public class AIServiceImpl implements AIServices{
	
	private final ChatModel chatModel;
	private final VectorStore vectorStore;
	
	public AIServiceImpl(ChatModel chatModel, VectorStore vectorStore) {
		this.chatModel = chatModel;
		this.vectorStore = vectorStore;
	}

	@Override
	public void processJobDescription(String jobDescription) {
		
		
	}
}
