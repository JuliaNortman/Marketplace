package com.ncgroup.marketplaceserver.model.dto;

import lombok.Data;

@Data
public class PaginationRequestDto {
    private String filter = "all";
    private String search = "";
    private int page = 1;
}
