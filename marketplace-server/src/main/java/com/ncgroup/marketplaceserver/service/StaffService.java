package com.ncgroup.marketplaceserver.service;

import com.ncgroup.marketplaceserver.model.Role;
import com.ncgroup.marketplaceserver.model.User;
import com.ncgroup.marketplaceserver.model.dto.PaginationRequestDto;
import com.ncgroup.marketplaceserver.model.dto.UserDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StaffService {
    UserDto save(String name, String surname, String email, String phone, LocalDate birthday, String status, Role role);
    User getById(long id);
    User updateStaff(long id, User staff);
    Map<String, Object> getByNameSurname(PaginationRequestDto pagination, Role role);
    List<User> findAll();
}
