package com.student.infra.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.student.infra.service.GenericService;
import com.student.model.GenericModel;

public abstract class GenericController<T extends GenericModel, S extends GenericService<T>>  {

	protected S service;

    private Class<T> modelClass;

    protected ObjectMapper objectMapper;
    
    
    
    public GenericController() {
        this(null, null);
    }

    public GenericController(Class<T> modelClass, S service) {
        this.modelClass = modelClass;
        this.service = service;
        objectMapper = new ObjectMapper();
        //ThirdPartyAwareObjectMapper.configureAndRegisterModules(objectMapper);
    }
    
    
 // ********************* COMMON SPRING COMPONENTS *********************
    public T initDefaultModelAttribute() {
        try {
            return modelClass.newInstance();
        } catch (Exception e) {
            // Handle Exception in exception handler
        }
        return null;
    }

    @ModelAttribute
    public T setupDefaultModelAttribute(@RequestParam(value = "id", required = false) String id) {
        T model = (id != null ? service.retrieve(id) : initDefaultModelAttribute());
        if (model == null) {
        	// Handle Exception in exception handler (Object not found)
        }
        return model;
    }
    
    @RequestMapping("/list")
    public Object list(HttpServletRequest request) {
        
    	// NOTE : Use Paginated Record
    	List<T> modelList = service.retrieveAll();
    	ModelAndView modelAndView = new ModelAndView(getFullJspPath("list"));
        modelAndView.addObject("list", modelList);
        
        return modelAndView;
    }
    
    @RequestMapping(value = { "/create", "/update" }, method = RequestMethod.GET)
    public Object createUpdateGet(@ModelAttribute T model, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView(getFullJspPath("fields"));
        modelAndView.addObject("model", model);
        
        String formPostUrl = model.getId() == null || "".equals(model.getId().trim()) ? "create" : "update?id="+model.getId();
        
        modelAndView.addObject("formPostUrl", formPostUrl);
        return modelAndView;
    }
    
    @Transactional(readOnly = false)
	@RequestMapping(value = { "/create", "/update" }, method = RequestMethod.POST)
	public Object createUpdatePost(@ModelAttribute T model, HttpServletRequest request) {
		service.saveWithEvict(model);
		return "redirect:list";
	}
    
    
    public String getFullJspPath(String jspFilename) {
        String ret = "";
        if (getModelName() != null)
            ret += "/" + getModelName();
        ret += "/" + jspFilename;

        return ret;
    }
    
    public String getModelName() {
        if (modelClass == null)
            return null;
        return modelClass.getSimpleName();
    }
}
