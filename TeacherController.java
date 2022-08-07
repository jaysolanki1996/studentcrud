package com.eptl.teachercrud.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eptl.teachercrud.model.Teacher;
import com.eptl.teachercrud.service.TeacherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

	@Autowired
	private TeacherService teacherService;

	@GetMapping("/list")
	public ResponseEntity<Object> list() {
		List<Teacher> teacherList = teacherService.getAll();
		return new ResponseEntity<Object>(teacherList, HttpStatus.OK);
	}

	@PostMapping("/create")
	public ResponseEntity<Object> save(@RequestBody String teacherdataString) {
		Teacher teacher = new Teacher();
		try {
			teacher = new ObjectMapper().readerForUpdating(teacher).readValue(teacherdataString);
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		teacher = teacherService.save(teacher);

		return new ResponseEntity<Object>(teacher, HttpStatus.OK);
	}

	@PostMapping("/update/{id}")
	public ResponseEntity<Object> save(@RequestBody String teacherdataString, @PathVariable(name = "id") String id) {
		Teacher teacher = teacherService.getById(id);

		try {
			teacher = new ObjectMapper().readerForUpdating(teacher).readValue(teacherdataString);
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		teacher = teacherService.save(teacher);

		return new ResponseEntity<Object>(teacher, HttpStatus.OK);
	}

}
