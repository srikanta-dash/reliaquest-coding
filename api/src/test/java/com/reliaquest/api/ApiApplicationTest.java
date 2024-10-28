package com.reliaquest.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.constant.Constant;
import com.reliaquest.api.model.Employee;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@AutoConfigureMockMvc
class ApiApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    RestTemplate restTemplate;

    @Value("${api.base.url}")
    private String apiBaseUrl;

    private static JsonNode allEmployeeJson;
    private static JsonNode singleEmployeeJson;
    private static List<String> empNames;

    @BeforeAll
    public static void init() {
        allEmployeeJson = TestUtils.readJson("allEmpTestData.json");
        assertNotNull(allEmployeeJson);
        singleEmployeeJson = TestUtils.readJson("singleEmpTestData.json");
        assertNotNull(singleEmployeeJson);
        empNames = Arrays.asList("Yuk Towne", "Melita Schuppe", "Emmitt Altenwerth", "Lara Pouros DVM", "Trudi Kunze",
                "Alvaro Kohler", "Clemente McClure", "Quincy Weissnat", "Rayna Grady", "Jeanne Schmitt");
    }

    @Test
    void getAllEmployees() throws Exception {
        mockGetAllEmployees();
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    List<Employee> list = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
                    assertEquals(50, list.size());
                });
    }

    @Test
    void getEmployeesByNameSearch() throws Exception {
        mockGetAllEmployees();
        mockMvc.perform(get("/api/v1/employee/search/Mar"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    List<Employee> list = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
                    assertEquals(2, list.size());
                });
    }

    @Test
    void getEmployeeById() throws Exception {
        mockSingleEmployee();
        mockMvc.perform(get("/api/v1/employee/a997c71d-6c50-4434-8af6-81ae8507ff09"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    Employee emp = mapper.readValue(result.getResponse().getContentAsString(), Employee.class);
                    assertEquals(emp.getName(), "Marica Strosin");
                    assertEquals(emp.getSalary(), 132373);
                });
    }

    @Test
    void getHighestSalaryOfEmployees() throws Exception {
        mockGetAllEmployees();
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andDo(result -> assertEquals("496189", result.getResponse().getContentAsString()));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames() throws Exception {
        mockGetAllEmployees();
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    List<String> list = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
                    assertEquals(10, list.size());
                    assertIterableEquals(empNames, list);
                });
    }

    @Test
    void createEmployee() throws Exception {
        Map<String, Object> inputMap = new LinkedHashMap<>();
        inputMap.put("name", "Marica Strosin");
        inputMap.put("salary", 132373);
        inputMap.put("age", 24);
        String requestBody = mapper.writeValueAsString(inputMap);
        when(restTemplate.postForEntity(apiBaseUrl + Constant.EMPLOYEES, inputMap, JsonNode.class))
                .thenReturn(ResponseEntity.ok(singleEmployeeJson));
        mockMvc.perform(post("/api/v1/employee")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    Employee emp = mapper.readValue(result.getResponse().getContentAsString(), Employee.class);
                    assertEquals(emp.getName(), "Marica Strosin");
                    assertEquals(emp.getSalary(), 132373);
                });
    }

    @Test
    void deleteEmployeeById() throws Exception {
        mockSingleEmployee();
        HttpEntity<String> entity = getHttpEntity("a997c71d-6c50-4434-8af6-81ae8507ff09");
        when(restTemplate.exchange(apiBaseUrl + Constant.EMPLOYEES, HttpMethod.DELETE, entity, JsonNode.class))
                .thenReturn(ResponseEntity.ok(singleEmployeeJson));

        mockMvc.perform(delete("/api/v1/employee/a997c71d-6c50-4434-8af6-81ae8507ff09"))
                .andExpect(status().isOk())
                .andDo(result -> assertEquals("Marica Strosin", result.getResponse().getContentAsString()));
    }

    private static HttpEntity<String> getHttpEntity(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jsonBody = "{\"name\":\""+ id +"\"}";
        return new HttpEntity<>(jsonBody, headers);
    }

    private void mockGetAllEmployees() {
        when(restTemplate.getForEntity(apiBaseUrl + Constant.EMPLOYEES, JsonNode.class))
                .thenReturn(ResponseEntity.ok(allEmployeeJson));
    }

    private void mockSingleEmployee() {
        when(restTemplate.getForEntity(apiBaseUrl + Constant.EMPLOYEES + "/a997c71d-6c50-4434-8af6-81ae8507ff09",
                JsonNode.class)).thenReturn(ResponseEntity.ok(singleEmployeeJson));
    }
}
