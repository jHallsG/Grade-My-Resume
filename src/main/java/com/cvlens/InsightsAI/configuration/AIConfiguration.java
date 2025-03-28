package com.cvlens.InsightsAI.configuration;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfiguration {
	
	// dont delete this. OllamaAPI should have already set localhost:11434 as the default value but somehow it results to
	// embedding model NOT FOUND error unless explicitly stated
	@Bean
	public OllamaApi ollamaApi(){
		return new OllamaApi("http://localhost:11434");
	}
}
