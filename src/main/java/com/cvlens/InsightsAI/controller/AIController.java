package com.cvlens.InsightsAI.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cvlens.InsightsAI.service.AIServices;

@RestController
public class AIController {
	
	private final AIServices aiService;
	
	public AIController(AIServices aiService) {
		this.aiService = aiService;
	}
	
	@GetMapping("/description")
	public ResponseEntity<String> uploadDescription (@RequestParam("job-description") String jd) {
		
		return ResponseEntity.status(HttpStatus.OK).body(aiService.processJobDescription(jd));
	}
	
	@PostMapping("/upload")
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
		
		return ResponseEntity.status(HttpStatus.OK).body(aiService.processFile(file));
	}
}
