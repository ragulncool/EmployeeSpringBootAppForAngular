package com.employee.springapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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

	private String authToken;

	@BeforeEach
	void cleanTestData() {
		jdbcTemplate.update("DELETE FROM employee WHERE emp_id = ?", 987654321);
		jdbcTemplate.update("DELETE FROM auth_token WHERE username = ?", "test.user");
		jdbcTemplate.update("DELETE FROM app_user WHERE username = ?", "test.user");
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
	void servesHelloWorldAtHelloPath() throws Exception {
		mockMvc.perform(get("/hello"))
				.andExpect(status().isOk())
				.andExpect(content().string("Hello, World!"));
	}

	@Test
	void requiresBearerTokenForEmployeeApi() throws Exception {
		mockMvc.perform(get("/api/employees"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/auth/logout"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void signsUpLogsInAndRevokesBearerToken() throws Exception {
		MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"test.user","password":"correct-horse"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andReturn();
		String token = com.jayway.jsonpath.JsonPath.read(
				signupResult.getResponse().getContentAsString(), "$.accessToken");

		mockMvc.perform(get("/api/employees")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"test.user","password":"correct-horse"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"test.user","password":"wrong-password"}
								"""))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/auth/logout")
								.header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/employees")
								.header("Authorization", "Bearer " + token))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void supportsEmployeeCrud() throws Exception {
		authToken = signupAndGetToken();
		mockMvc.perform(post("/api/employees")
						.header("Authorization", "Bearer " + authToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"emp_id":987654321,"emp_name":"Alex Doe","emp_dept":"Engineering"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.emp_id").value(987654321))
				.andExpect(jsonPath("$.emp_name").value("Alex Doe"))
				.andExpect(jsonPath("$.emp_dept").value("Engineering"));

		mockMvc.perform(get("/api/employees/987654321")
						.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.emp_name").value("Alex Doe"));

		mockMvc.perform(put("/api/employees/987654321")
						.header("Authorization", "Bearer " + authToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"emp_id":987654321,"emp_name":"Alex Smith","emp_dept":"Platform"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.emp_name").value("Alex Smith"))
				.andExpect(jsonPath("$.emp_dept").value("Platform"));

		mockMvc.perform(delete("/api/employees/987654321")
						.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/employees/987654321")
						.header("Authorization", "Bearer " + authToken))
				.andExpect(status().isNotFound());
	}

	private String signupAndGetToken() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"test.user","password":"correct-horse"}
								"""))
				.andExpect(status().isCreated())
				.andReturn();
		return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
	}

}
