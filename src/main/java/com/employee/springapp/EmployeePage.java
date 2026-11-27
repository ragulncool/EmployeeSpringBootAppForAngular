package com.employee.springapp;

import java.util.List;

public record EmployeePage(
		List<Employee> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		String sortBy,
		String direction) {
}
