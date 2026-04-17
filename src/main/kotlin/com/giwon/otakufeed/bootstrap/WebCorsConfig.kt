package com.giwon.otakufeed.bootstrap

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebCorsConfig(
    @Value("\${otaku-feed.cors.allowed-origin-patterns:*}") private val allowedOrigins: String,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        val patterns = allowedOrigins.split(",").map { it.trim() }.toTypedArray()
        registry.addMapping("/**")
            .allowedOriginPatterns(*patterns)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
