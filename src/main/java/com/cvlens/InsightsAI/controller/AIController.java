package com.cvlens.InsightsAI.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cvlens.InsightsAI.service.AIServices;

@RestController
public class AIController {
	
	private final AIServices aiService;
	
	public AIController(AIServices aiService) {
		this.aiService = aiService;
	}
	
	@GetMapping("/description")
	public void getJobDescription(@RequestParam("job-description") String jd) {
		aiService.processJobDescription(jd);
	}
}
