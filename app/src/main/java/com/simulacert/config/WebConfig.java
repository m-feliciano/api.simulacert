package com.simulacert.config;

import com.simulacert.rest.controller.param.ContentLanguageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new ContentLanguageConverter());
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		String forwardToSwagger = "forward:/swagger-ui/index.html";
		registry.addViewController("/swagger-ui").setViewName(forwardToSwagger);
		registry.addViewController("/swagger-ui/").setViewName(forwardToSwagger);
	}
}
