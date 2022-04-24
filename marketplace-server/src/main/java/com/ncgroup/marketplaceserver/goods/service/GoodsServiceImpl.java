package com.ncgroup.marketplaceserver.goods.service;

import com.ncgroup.marketplaceserver.exception.basic.NotFoundException;
import com.ncgroup.marketplaceserver.file.service.MediaService;
import com.ncgroup.marketplaceserver.goods.exceptions.GoodAlreadyExistsException;
import com.ncgroup.marketplaceserver.goods.model.*;
import com.ncgroup.marketplaceserver.goods.repository.GoodsRepository;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@PropertySource("classpath:application.properties")
public class GoodsServiceImpl implements GoodsService {

    @Value("${page.capacity}")
    private Integer PAGE_CAPACITY;

    private final GoodsRepository repository;
    private final MediaService mediaService;

    @Autowired
    public GoodsServiceImpl(GoodsRepository repository, MediaService mediaService) {
        this.repository = repository;
        this.mediaService = mediaService;
    }

    @Override
    public Good create(GoodDto goodDto) throws GoodAlreadyExistsException {
        String newImage = goodDto.getImage();

        if (newImage != null && !newImage.isEmpty()) {
            goodDto.setImage(this.mediaService.confirmUpload(newImage));
        }
        return new Good(goodDto, repository.getGoodId(goodDto), mediaService);
    }

    @Override
    public Good edit(GoodDto goodDto, long id) throws NotFoundException {
        Good good = this.findById(id);

        String newImage = goodDto.getImage();

        if (newImage != null && !newImage.isEmpty()) {
            String oldImage = good.getImage();
            if (!oldImage.isEmpty() && !oldImage.equals(newImage)) {
                goodDto.setImage(this.mediaService.confirmUpload(newImage));
                mediaService.delete(oldImage);
            }
        }

        repository.editGood(goodDto, id);
        good.setProperties(goodDto, id, mediaService);
        return good;
    }

    @Override
    public Good find(long id) throws NotFoundException {
        Good good = findById(id);
        good.setImage(good.getImage(), mediaService);
        return good;
    }

    private Good findById(long id) throws NotFoundException {
        Optional<Good> goodOptional = repository.findById(id);
        return goodOptional.orElseThrow(() ->
                new NotFoundException("Product with " + id + " not found."));
    }

    private final String FROM_QUERY = "FROM goods INNER JOIN product ON goods.prod_id = product.id " +
            "INNER JOIN firm ON goods.firm_id = firm.id " +
            "INNER JOIN category ON category.id = product.category_id";

    private final String SELECT_QUERY = "SELECT goods.id, product.name AS product_name, status, shipping_date, " +
            "firm.name AS firm_name, category.name AS category_name, unit, " +
            "goods.quantity, goods.price, goods.discount, goods.in_stock, " +
            "goods.description, goods.image";


    @Override
    public ModelView display(RequestParams params) throws NotFoundException {
        StringJoiner fromQuery = new StringJoiner(" ");
        fromQuery.add(FROM_QUERY);

        String condition = displayQueryConditions(params);
        if(StringUtils.isNotBlank(condition)) {
            fromQuery.add("WHERE").add(condition);
        }
        int numOfGoods = repository
                .countGoods("SELECT COUNT(*) " + fromQuery, params);
        fromQuery.add(getSortOrder(params));
        log.info(fromQuery.toString());
        StringJoiner flexibleQuery = new StringJoiner(" ");
        flexibleQuery.add(SELECT_QUERY);
        flexibleQuery.merge(fromQuery);

        int numOfPages = numOfGoods % PAGE_CAPACITY == 0 ?
                numOfGoods / PAGE_CAPACITY : (numOfGoods / PAGE_CAPACITY) + 1;

        if (params.getPage() != null) {
            flexibleQuery.add("LIMIT :PAGE_CAPACITY OFFSET (:page - 1) * :PAGE_CAPACITY");
        } else {
            flexibleQuery.add("LIMIT :PAGE_CAPACITY");
            params.setPage(1);
        }

        List<Good> res = repository.display(flexibleQuery.toString(), params)
                .stream()
                .peek(good -> good.setImage(good.getImage(), mediaService))
                .collect(Collectors.toList());

        if (res.isEmpty()) {
            throw new NotFoundException("Sorry, but there are no products corresponding to your criteria.");
        }
        return new ModelView(params.getPage(), numOfPages, res);
    }

    @Override
    public List<String> getCategories() throws NotFoundException {
        return repository.getCategories();
    }

    @Override
    public List<Double> getPriceRange(String category) throws NotFoundException {
        ArrayList<Double> priceRange = new ArrayList<>(2);
        if (category.equals("all")) {
            priceRange.add(repository.getTotalMinPrice());
            priceRange.add(repository.getTotalMaxPrice());
            return priceRange;
        }
        priceRange.add(repository.getMinPrice(category));
        priceRange.add(repository.getMaxPrice(category));
        return priceRange;
    }

    @Override
    public List<String> getFirms() throws NotFoundException {
        return repository.getFirms();
    }

    @Override
    public void updateQuantity(long id, double quantity) {
        // check
        repository.editQuantity(id, quantity, Double.compare(quantity, 0) != 0);
    }

    private String displayQueryConditions(RequestParams params) {
        List<String> conditions = new LinkedList<>();
        if (params.getName() != null) {
            conditions.add("UPPER(product.name) LIKE UPPER(:name)");
        }
        if (params.getCategory() != null && !params.getCategory().equals("all")) {
            conditions.add("category.name = :category");
        }
        if (params.getMinPrice() != null) {
            conditions.add("price - price*discount/100 >= :minPrice");
        }
        if (params.getMaxPrice() != null) {
            conditions.add("price - price*discount/100 <= :maxPrice");
        }
        conditions.add("status = true");
        return String.join(" AND ", conditions);
    }

    private String getSortOrder(RequestParams params) {
        String sortClause = "";
        if (SortCategory.PRICE.equals(params.getSort())) {
            sortClause = "ORDER BY goods.price";
        } else if(SortCategory.DATE.equals(params.getSort())) {
            sortClause = "ORDER BY shipping_date";
        } else {
            sortClause = "ORDER BY product.name";
        }

        if("DESC".equals(params.getDirection())) {
            sortClause += " DESC";
        }
        return sortClause;
    }
}
