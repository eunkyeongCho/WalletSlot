package com.ssafy.b108.walletslot.backend.config.web;

import com.ssafy.b108.walletslot.backend.config.web.converter.YearMonthConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final YearMonthConverter yearMonthConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(yearMonthConverter);
    }
}
