package com.employee.springapp;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeRepository {

	private final JdbcTemplate jdbcTemplate;

	public EmployeeRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Employee> findPage(int page, int size, String sortBy, String direction) {
		String orderBy = sortBy.equals("emp_id")
				? sortBy + " " + direction
				: sortBy + " " + direction + ", emp_id ASC";
		return jdbcTemplate.query(
				"SELECT emp_id, emp_name, emp_dept FROM employee ORDER BY " + orderBy + " LIMIT ? OFFSET ?",
				(rs, rowNum) -> new Employee(
						rs.getInt("emp_id"),
						rs.getString("emp_name"),
						rs.getString("emp_dept")),
				size, (long) page * size);
	}

	public long count() {
		return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employee", Long.class);
	}

	public Optional<Employee> findById(int empId) {
		List<Employee> employees = jdbcTemplate.query(
				"SELECT emp_id, emp_name, emp_dept FROM employee WHERE emp_id = ?",
				(rs, rowNum) -> new Employee(
						rs.getInt("emp_id"),
						rs.getString("emp_name"),
						rs.getString("emp_dept")),
				empId);
		return employees.stream().findFirst();
	}

	public void insert(Employee employee) {
		jdbcTemplate.update(
				"INSERT INTO employee (emp_id, emp_name, emp_dept) VALUES (?, ?, ?)",
				employee.emp_id(), employee.emp_name(), employee.emp_dept());
	}

	public boolean update(Employee employee) {
		return jdbcTemplate.update(
				"UPDATE employee SET emp_name = ?, emp_dept = ? WHERE emp_id = ?",
				employee.emp_name(), employee.emp_dept(), employee.emp_id()) > 0;
	}

	public boolean deleteById(int empId) {
		return jdbcTemplate.update("DELETE FROM employee WHERE emp_id = ?", empId) > 0;
	}
}
