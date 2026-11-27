package com.employee.springapp;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class EmployeeService {

	private static final int MAX_PAGE_SIZE = 100;
	private static final Map<String, String> SORT_COLUMNS = Map.of(
			"emp_id", "emp_id",
			"emp_name", "emp_name",
			"emp_dept", "emp_dept");

	private final EmployeeRepository employeeRepository;

	public EmployeeService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	public EmployeePage getEmployees(int page, int size, String sortBy, String direction) {
		if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Page must be non-negative and size must be between 1 and " + MAX_PAGE_SIZE);
		}

		String sortColumn = SORT_COLUMNS.get(sortBy);
		if (sortColumn == null || (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Sort by emp_id, emp_name, or emp_dept with direction asc or desc");
		}

		String normalizedDirection = direction.toLowerCase();
		long totalElements = employeeRepository.count();
		int totalPages = (int) Math.ceil((double) totalElements / size);
		return new EmployeePage(
				employeeRepository.findPage(page, size, sortColumn, normalizedDirection),
				page,
				size,
				totalElements,
				totalPages,
				sortBy,
				normalizedDirection);
	}

	public Employee getEmployee(int empId) {
		return employeeRepository.findById(empId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
	}

	public Employee createEmployee(Employee employee) {
		employeeRepository.insert(employee);
		return employee;
	}

	public Employee updateEmployee(int empId, Employee employee) {
		if (empId != employee.emp_id()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path empId must match body emp_id");
		}
		if (!employeeRepository.update(employee)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
		}
		return employee;
	}

	public void deleteEmployee(int empId) {
		if (!employeeRepository.deleteById(empId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
		}
	}
}
