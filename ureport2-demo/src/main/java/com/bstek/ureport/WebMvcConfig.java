package com.bstek.ureport;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ImportResource(locations = "classpath:ureport-console-context.xml")
public class WebMvcConfig implements WebMvcConfigurer {}
