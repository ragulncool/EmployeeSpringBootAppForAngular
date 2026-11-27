package com.employee.springapp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@GetMapping
	public EmployeePage getEmployees(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "emp_id") String sortBy,
			@RequestParam(defaultValue = "asc") String direction) {
		return employeeService.getEmployees(page, size, sortBy, direction);
	}

	@GetMapping("/{empId}")
	public Employee getEmployee(@PathVariable int empId) {
		return employeeService.getEmployee(empId);
	}

	@PostMapping
	public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
		return ResponseEntity.created(URI.create("/api/employees/" + employee.emp_id()))
				.body(employeeService.createEmployee(employee));
	}

	@PutMapping("/{empId}")
	public Employee updateEmployee(@PathVariable int empId, @RequestBody Employee employee) {
		return employeeService.updateEmployee(empId, employee);
	}

	@DeleteMapping("/{empId}")
	public ResponseEntity<Void> deleteEmployee(@PathVariable int empId) {
		employeeService.deleteEmployee(empId);
		return ResponseEntity.noContent().build();
	}
}
