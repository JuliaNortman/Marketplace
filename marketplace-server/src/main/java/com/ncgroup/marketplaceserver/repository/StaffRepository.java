package com.ncgroup.marketplaceserver.repository;

import com.ncgroup.marketplaceserver.model.Courier;
import com.ncgroup.marketplaceserver.model.Role;
import com.ncgroup.marketplaceserver.model.User;
import com.ncgroup.marketplaceserver.model.dto.CourierUpdateDto;

import java.util.List;

public interface StaffRepository {
    User update(User staff, long id);

    List<User> getByNameSurname(String search, String status, int page, Role role);

    int getNumberOfRows(String search, String status, Role role);

    List<User> getByNameSurnameAll(String search, int page, Role role);

    Integer getNumberOfRowsAll(String search, Role role);
}
