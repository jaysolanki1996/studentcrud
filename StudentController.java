package com.student.infra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.student.infra.service.StudentService;
import com.student.model.Student;

@Controller
@RequestMapping("student")
public class StudentController extends GenericController<Student, StudentService> {

	@Autowired
	public StudentController(StudentService service) {
		super(Student.class, service);
	}
}
