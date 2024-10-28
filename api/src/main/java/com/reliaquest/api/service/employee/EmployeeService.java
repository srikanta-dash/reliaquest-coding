package com.reliaquest.api.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.Exception.ExceptionHelper;
import com.reliaquest.api.constant.Constant;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.Response;
import com.reliaquest.api.service.api.IApiService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService implements IEmployeeService{
    @Autowired
    private IApiService apiService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    ExceptionHelper exceptionHelper;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Override
    public List<Employee> getAllEmployees() {
        ResponseEntity<JsonNode> responseEntity = apiService.get(Constant.EMPLOYEES);
        return processResponse(responseEntity, new TypeReference<>() {});
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        return getAllEmployees().stream()
                .filter(emp -> emp.getName() != null && emp.getName().contains(searchString))
                .collect(Collectors.toList());
    }

    @Override
    public Employee getEmployeeById(String id) {
        ResponseEntity<JsonNode> responseEntity = apiService.get(Constant.EMPLOYEES + "/" + id);
        return processResponse(responseEntity, new TypeReference<>() {});
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        int highest = Integer.MIN_VALUE;
        for(Employee emp : getAllEmployees()) {
            if(emp.getSalary() > highest) {
                highest = emp.getSalary();
            }
        }
        return highest;
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        List<String> sortedEmpNames = getAllEmployees()
                .stream().sorted((o1, o2) -> o2.getSalary() - o1.getSalary())
                .map(Employee::getName)
                .collect(Collectors.toList());
        return sortedEmpNames.subList(0, Math.min(sortedEmpNames.size(), 10));
    }

    @Override
    public Employee createEmployee(Object employeeInput) {
        ResponseEntity<JsonNode> responseEntity = apiService.post(Constant.EMPLOYEES, employeeInput);
        return processResponse(responseEntity, new TypeReference<>() {});
    }

    @Override
    public String deleteEmployeeById(String id) {
        Employee employee = getEmployeeById(id);
        ResponseEntity<JsonNode> responseEntity = apiService.delete(Constant.EMPLOYEES, id);
        processResponse(responseEntity, new TypeReference<>() {});
        return employee.getName();
    }

    private <T> T processResponse(ResponseEntity<JsonNode> responseEntity, TypeReference<T> type) {
        if(responseEntity.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
            logger.info("API server execution successfully and status code: {}", responseEntity.getStatusCode());
            Response response = mapper.convertValue(responseEntity.getBody(), Response.class);
            return mapper.convertValue(response.getData(), type);
        } else {
            logger.error("API server execution failed and status code: {}", responseEntity.getStatusCode());
            throw exceptionHelper.exceptionByStatus(responseEntity);
        }
    }
}
