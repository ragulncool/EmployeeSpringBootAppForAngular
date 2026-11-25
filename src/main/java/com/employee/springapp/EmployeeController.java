package com.employee.springapp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

	private final JdbcTemplate jdbcTemplate;

	public EmployeeController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping
	public List<Employee> getEmployees() {
		return jdbcTemplate.query(
				"SELECT emp_id, emp_name, emp_dept FROM employee ORDER BY emp_id",
				(rs, rowNum) -> new Employee(
						rs.getInt("emp_id"),
						rs.getString("emp_name"),
						rs.getString("emp_dept")));
	}

	@GetMapping("/{empId}")
	public Employee getEmployee(@PathVariable int empId) {
		return findEmployee(empId);
	}

	@PostMapping
	public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
		jdbcTemplate.update(
				"INSERT INTO employee (emp_id, emp_name, emp_dept) VALUES (?, ?, ?)",
				employee.emp_id(), employee.emp_name(), employee.emp_dept());

		return ResponseEntity.created(URI.create("/api/employees/" + employee.emp_id()))
				.body(employee);
	}

	@PutMapping("/{empId}")
	public Employee updateEmployee(@PathVariable int empId, @RequestBody Employee employee) {
		if (empId != employee.emp_id()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path empId must match body emp_id");
		}

		int updated = jdbcTemplate.update(
				"UPDATE employee SET emp_name = ?, emp_dept = ? WHERE emp_id = ?",
				employee.emp_name(), employee.emp_dept(), empId);
		if (updated == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
		}
		return employee;
	}

	@DeleteMapping("/{empId}")
	public ResponseEntity<Void> deleteEmployee(@PathVariable int empId) {
		int deleted = jdbcTemplate.update("DELETE FROM employee WHERE emp_id = ?", empId);
		if (deleted == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
		}
		return ResponseEntity.noContent().build();
	}

	private Employee findEmployee(int empId) {
		List<Employee> employees = jdbcTemplate.query(
				"SELECT emp_id, emp_name, emp_dept FROM employee WHERE emp_id = ?",
				(rs, rowNum) -> new Employee(
						rs.getInt("emp_id"),
						rs.getString("emp_name"),
						rs.getString("emp_dept")),
				empId);
		if (employees.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
		}
		return employees.get(0);
	}
}
