package com.ncgroup.marketplaceserver.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailParamsDto {
    private final String subject;
    private final String message;
    private final String receiver;
    private final String name;
    private final String redirectUrl;
}
