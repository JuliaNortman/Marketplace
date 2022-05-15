package com.ncgroup.marketplaceserver.service.impl;

import com.ncgroup.marketplaceserver.constants.StatusConstants;
import com.ncgroup.marketplaceserver.exception.constants.ExceptionMessage;
import com.ncgroup.marketplaceserver.exception.domain.InvalidStatusException;
import com.ncgroup.marketplaceserver.model.Role;
import com.ncgroup.marketplaceserver.model.User;
import com.ncgroup.marketplaceserver.model.dto.PaginationRequestDto;
import com.ncgroup.marketplaceserver.model.dto.UserDto;
import com.ncgroup.marketplaceserver.repository.ManagerRepository;
import com.ncgroup.marketplaceserver.repository.StaffRepository;
import com.ncgroup.marketplaceserver.repository.UserRepository;
import com.ncgroup.marketplaceserver.service.EmailSenderService;
import com.ncgroup.marketplaceserver.service.StaffService;
import com.ncgroup.marketplaceserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StaffServiceImpl implements StaffService {
    private StaffRepository staffRepository;
    private EmailSenderService emailSenderService;
    private UserService userService;
    private UserRepository userRepository;

    @Value("${page.capacity}")
    private Integer PAGE_SIZE;

    @Autowired
    StaffServiceImpl(StaffRepository staffRepository, EmailSenderService emailSenderService,
                       UserService userService, UserRepository userRepository) {
        this.staffRepository = staffRepository;
        this.emailSenderService = emailSenderService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public UserDto save(String name, String surname, String email, String phone, LocalDate birthday, String status, Role role) {
        userService.validateNewEmail(StringUtils.EMPTY, email);
        boolean isEnabled = isEnabled(status);
        User user = User.builder()
                .name(name)
                .surname(surname)
                .phone(phone)
                .email(email)
                .birthday(birthday)
                .lastFailedAuth(OffsetDateTime.now())
                .role(role)
                .isEnabled(isEnabled)
                .status(status)
                .build();
        String authlink = null;
        try {
            authlink = emailSenderService.sendSimpleEmailPasswordCreation(email, name);
        } catch (MessagingException e) {
            log.error(e.getMessage());
        }
        user.setAuthLink(authlink);
        user = userRepository.save(user);
        log.info("New manager registered");
        return UserDto.convertToDto(user);
    }

    @Override
    public User getById(long id) {
        User staff = userRepository.findById(id);
        return staff;
    }

    @Override
    public User updateStaff(long id, User staff) {
        return staffRepository.update(staff, id);
    }

    @Override
    public Map<String, Object> getByNameSurname(PaginationRequestDto pagination, Role role) {
        List<User> staff;
        int allPages;
        if("all".equals(pagination.getFilter())) {
            staff = staffRepository.getByNameSurnameAll(pagination.getSearch(), (pagination.getPage() - 1) * PAGE_SIZE, role);
            allPages = staffRepository.getNumberOfRowsAll(pagination.getSearch(), role);
        } else {
            staff = staffRepository.getByNameSurname(pagination.getSearch(), pagination.getFilter(), (pagination.getPage() - 1) * PAGE_SIZE, role);
            allPages = staffRepository.getNumberOfRows(pagination.getSearch(), pagination.getFilter(), role);
        }


        Map<String, Object> result = new HashMap<>();
        result.put("users", staff);
        result.put("currentPage", pagination.getPage());
        result.put("pageNum", allPages % PAGE_SIZE == 0 ? allPages / PAGE_SIZE : allPages / PAGE_SIZE + 1);

        return result;
    }

    @Override
    public List<User> findAll() {
        return userRepository.allCouriersManagers();
    }

    private boolean isEnabled(String status) {
        return !status.equals(StatusConstants.TERMINATED);
    }
}
