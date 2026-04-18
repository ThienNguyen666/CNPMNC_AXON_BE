package asset.project.controller;

import asset.project.dto.request.DepartmentReq;
import asset.project.dto.response.DepartmentRes;
import asset.project.service.DepartmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private DepartmentRes testDepartmentRes;
    private final UUID departmentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(departmentController).build();

        testDepartmentRes = new DepartmentRes(
                departmentId,
                "IT Department",
                "IT"
        );
    }

    @Test
    void getAll() throws Exception {
        when(departmentService.getAll()).thenReturn(Arrays.asList(testDepartmentRes));

        mockMvc.perform(get("/api/departments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data[0].id").value(departmentId.toString()))
                .andExpect(jsonPath("$.data[0].name").value("IT Department"))
                .andExpect(jsonPath("$.data[0].code").value("IT"));
    }

    @Test
    void getById() throws Exception {
        when(departmentService.getById(departmentId)).thenReturn(testDepartmentRes);

        mockMvc.perform(get("/api/departments/{id}", departmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(departmentId.toString()))
                .andExpect(jsonPath("$.data.name").value("IT Department"))
                .andExpect(jsonPath("$.data.code").value("IT"));
    }

    @Test
    void create() throws Exception {
        DepartmentReq req = new DepartmentReq("IT Department", "IT");
        when(departmentService.create(any(DepartmentReq.class))).thenReturn(testDepartmentRes);

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Created"))
                .andExpect(jsonPath("$.data.id").value(departmentId.toString()))
                .andExpect(jsonPath("$.data.name").value("IT Department"))
                .andExpect(jsonPath("$.data.code").value("IT"));
    }

    @Test
    void update() throws Exception {
        DepartmentReq req = new DepartmentReq("IT Dept", "IT_NEW");
        DepartmentRes updatedRes = new DepartmentRes(departmentId, "IT Dept", "IT_NEW");

        when(departmentService.update(eq(departmentId), any(DepartmentReq.class))).thenReturn(updatedRes);

        mockMvc.perform(put("/api/departments/{id}", departmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(departmentId.toString()))
                .andExpect(jsonPath("$.data.name").value("IT Dept"))
                .andExpect(jsonPath("$.data.code").value("IT_NEW"));
    }
}
