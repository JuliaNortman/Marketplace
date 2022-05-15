package com.ncgroup.marketplaceserver.repository.impl;

import com.ncgroup.marketplaceserver.model.Courier;
import com.ncgroup.marketplaceserver.model.Role;
import com.ncgroup.marketplaceserver.model.User;
import com.ncgroup.marketplaceserver.model.dto.CourierUpdateDto;
import com.ncgroup.marketplaceserver.model.mapper.UserRowMapper;
import com.ncgroup.marketplaceserver.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@PropertySources({
        @PropertySource("classpath:database/queries.properties"),
        @PropertySource("classpath:application.properties")
})
public class StaffRepositoryImpl implements StaffRepository {
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("${page.capacity}")
    private Integer PAGE_SIZE;

    @Value("${staff.update-person}")
    private String updatePerson;

    @Value("${staff.update-credentials}")
    private String updateCredentials;

    @Value("${staff.find-by-name-surname}")
    private String filterNameQuery;

    @Value("${staff.number-of-rows}")
    private String selectNumberOfRows;

    @Value("${staff.number-of-rows-all}")
    private String selectNumberOfRowsAll;

    @Value("${staff.find-by-name-surname-all}")
    private String filterNameQueryAll;

    public StaffRepositoryImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Transactional
    @Override
    public User update(User staff, long id) {
        SqlParameterSource credentialsParams = new MapSqlParameterSource()
                .addValue("is_enabled", staff.isEnabled())
                .addValue("status", staff.getStatus())
                .addValue("id", id);
        namedParameterJdbcTemplate.update(updateCredentials, credentialsParams);


        SqlParameterSource personParams = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", staff.getName())
                .addValue("surname", staff.getSurname())
                .addValue("phone", staff.getPhone())
                .addValue("birthday", staff.getBirthday());
        namedParameterJdbcTemplate.update(updatePerson, personParams);
        return staff;
    }

    @Override
    public List<User> getByNameSurname(String search, String status, int page, Role role) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("search", search)
                .addValue("status", status)
                .addValue("page", page)
                .addValue("pageSize", PAGE_SIZE)
                .addValue("role", role.name());
        return namedParameterJdbcTemplate.query(filterNameQuery, params, new UserRowMapper());
    }

    @Override
    public int getNumberOfRows(String search, String status, Role role) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("search", search)
                .addValue("status", status)
                .addValue("role", role.name());
        return namedParameterJdbcTemplate.queryForObject(selectNumberOfRows, params, Integer.class);
    }

    @Override
    public List<User> getByNameSurnameAll(String search, int page, Role role) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("search", search)
                .addValue("page", page)
                .addValue("pageSize", PAGE_SIZE)
                .addValue("role", role.name());
        return namedParameterJdbcTemplate.query(filterNameQueryAll, params, new UserRowMapper());
    }

    @Override
    public Integer getNumberOfRowsAll(String search, Role role) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("search", search)
                .addValue("role", role.name());
        return namedParameterJdbcTemplate.queryForObject(selectNumberOfRowsAll, params, Integer.class);
    }
}
