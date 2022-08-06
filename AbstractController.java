package com.student.infra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;

@Controller
public abstract class AbstractController {

	@Autowired
    protected ApplicationContext context;
	
	public String getContextMessage(String code) {
        return context.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    public String getContextMessage(String code, Object[] args) {
        return context.getMessage(code, args, LocaleContextHolder.getLocale());
    }
    
    
}
