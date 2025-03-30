package com.cvlens.InsightsAI.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface AIServices {
	
	String processJobDescription(String jobDescription);
	String processFile(MultipartFile file) throws IOException;
	String askAI(String userPrompt);

}
