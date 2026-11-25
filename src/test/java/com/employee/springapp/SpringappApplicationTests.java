package com.employee.springapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpringappApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanEmployeeTestData() {
		jdbcTemplate.update("DELETE FROM employee WHERE emp_id = ?", 987654321);
	}

	@Test
	void contextLoads() {
	}

	@Test
	void connectsToH2Database() {
		Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
		org.junit.jupiter.api.Assertions.assertEquals(1, result);
	}

	@Test
	void servesHelloWorldAtRoot() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string("Hello, World!"));
	}

	@Test
	void supportsEmployeeCrud() throws Exception {
		mockMvc.perform(post("/api/employees")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"emp_id":987654321,"emp_name":"Alex Doe","emp_dept":"Engineering"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.emp_id").value(987654321))
				.andExpect(jsonPath("$.emp_name").value("Alex Doe"))
				.andExpect(jsonPath("$.emp_dept").value("Engineering"));

		mockMvc.perform(get("/api/employees/987654321"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.emp_name").value("Alex Doe"));

		mockMvc.perform(put("/api/employees/987654321")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"emp_id":987654321,"emp_name":"Alex Smith","emp_dept":"Platform"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.emp_name").value("Alex Smith"))
				.andExpect(jsonPath("$.emp_dept").value("Platform"));

		mockMvc.perform(delete("/api/employees/987654321"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/employees/987654321"))
				.andExpect(status().isNotFound());
	}

}
