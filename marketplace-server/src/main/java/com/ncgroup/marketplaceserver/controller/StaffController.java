package com.ncgroup.marketplaceserver.controller;

import com.ncgroup.marketplaceserver.model.Role;
import com.ncgroup.marketplaceserver.model.User;
import com.ncgroup.marketplaceserver.model.dto.PaginationRequestDto;
import com.ncgroup.marketplaceserver.model.dto.UserDto;
import com.ncgroup.marketplaceserver.service.StaffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

@RequestMapping("/api/staff")
@RestController
@Slf4j
public class StaffController {
    private StaffService staffService;

    @Autowired
    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping()
    public ResponseEntity<UserDto> manager(@Valid @RequestBody UserDto user) {
        UserDto newUser = staffService.save(
                user.getName(), user.getSurname(), user.getEmail(), user.getPhone(), user.getBirthday(), user.getStatus(), user.getRole());
        return new ResponseEntity<>(newUser, OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable int id) {
        User staff = staffService.getById(id);
        return new ResponseEntity<>(UserDto.convertToDto(staff), OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> updateManager(
            @Valid @RequestBody User staff,
            @PathVariable("id") long id
    ) {
        return new ResponseEntity<>(staffService.updateStaff(id, staff), OK);
    }

    @GetMapping()
    public ResponseEntity<Map<String, Object>> findByNameSurname(PaginationRequestDto request,
                                                                 @RequestParam String role) {
        return new ResponseEntity<>(staffService.getByNameSurname(request, Role.valueOf(role)), OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> findAll() {
        return new ResponseEntity<>(staffService.findAll(), OK);
    }
}
