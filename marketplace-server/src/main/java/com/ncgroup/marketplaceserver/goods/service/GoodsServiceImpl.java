
package com.ncgroup.marketplaceserver.goods.service;

import com.ncgroup.marketplaceserver.file.service.MediaService;
import com.ncgroup.marketplaceserver.goods.exceptions.GoodAlreadyExistsException;
import com.ncgroup.marketplaceserver.goods.model.Good;
import com.ncgroup.marketplaceserver.goods.model.GoodDto;
import com.ncgroup.marketplaceserver.goods.repository.GoodsRepository;
import com.ncgroup.marketplaceserver.exception.domain.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class GoodsServiceImpl implements GoodsService {

    static final Integer PAGE_CAPACITY = 10;

    private GoodsRepository repository;

    private MediaService mediaService;

    @Autowired
    public GoodsServiceImpl(GoodsRepository repository, MediaService mediaService) {
        this.repository = repository;
        this.mediaService = mediaService;
    }

    @Override
    public Good create(GoodDto goodDto) throws GoodAlreadyExistsException {
        goodDto.setImage(this.mediaService.confirmUpload(goodDto.getImage()));
        Long goodId = repository.createGood(goodDto); // get the id of new good if it is new
        Good good = new Good();
        good.setProperties(goodDto, goodId);
        good.setImage(mediaService.getCloudStorage().getResourceUrl(good.getImage()));
        return good;
    }

    @Override
    public Good edit(GoodDto goodDto, long id) throws NotFoundException {
        Good good = this.findById(id); // pull the good object if exists
        String oldImage = good.getImage();
        String newImage = goodDto.getImage();
        goodDto.setImage(this.mediaService.confirmUpload(good.getImage()));
        if(!newImage.isEmpty() && !oldImage.isEmpty() && !oldImage.equals(newImage)){
            log.info("Deleting old image");
            mediaService.delete(oldImage);
        }
        good.setProperties(goodDto, id);
        repository.editGood(goodDto, id); // push the changed good object
        good.setImage(mediaService.getCloudStorage().getResourceUrl(good.getImage()));
        return good;
    }

    @Override
    public Good find(long id) throws NotFoundException {
        Good good = findById(id);
        good.setImage(mediaService.getCloudStorage().getResourceUrl(good.getImage()));
        return good;
    }

    private Good findById(long id) throws NotFoundException{
        Optional<Good> goodOptional = repository.findById(id);
        return goodOptional.orElseThrow(() -> new NotFoundException("Product with " + id + " not found."));
    }

    @Override
    public Map<String, Object> display(Optional<String> name, Optional<String> category,
                                       Optional<String> minPrice, Optional<String> maxPrice,
                                       Optional<String> sortBy, Optional<String> sortDirection,
                                       Optional<Integer> page) throws NotFoundException {

        int counter = 0;
        List<String> concatenator = new ArrayList<>();

        String flexibleQuery = "SELECT goods.id, product.name AS product_name, " +
                "firm.name AS firm_name, category.name AS category_name, unit, " +
                " goods.quantity, goods.price, goods.discount, goods.in_stock," +
                " goods.description, goods.image FROM goods INNER JOIN " +
                "product ON goods.prod_id = product.id " +
                "INNER JOIN firm ON goods.firm_id = firm.id " +
                "INNER JOIN category ON category.id = product.category_id";

        // Sort can be by: price, product.name, discount.



        if (name.isPresent()) {
            concatenator.add(" product.name LIKE '%" + name.get().toLowerCase() + "%'");
            counter++;
        }

        if (category.isPresent() && !category.get().equals("all")) {
            concatenator.add(" category.name = " + "'" + category.get() + "'");
            counter++;
        }

        if (minPrice.isPresent()) {
            concatenator.add(" price >= " + minPrice.get());
            counter++;
        }

        if (maxPrice.isPresent()) {
            concatenator.add(" price <= " + maxPrice.get());
            counter++;
        }



        if (counter > 0) {
            flexibleQuery += " WHERE" + concatenator.get(0);
            for (int i = 1; i < counter; i++) {
                flexibleQuery += " AND" + concatenator.get(i);
            }
        }



        if (sortBy.isPresent()) {
            if(sortBy.get().equals("price")) {
                flexibleQuery += " ORDER BY goods.price";
            } else if (sortBy.get().equals("name")) {
                flexibleQuery += " ORDER BY product.name";
            }
        } else {
            flexibleQuery += " ORDER BY product.name";
        }

        if (sortDirection.isPresent()) {
            flexibleQuery += " " + sortDirection.get().toUpperCase();
        } else {
            flexibleQuery += " DESC";
        }


        List<Good> res = repository.display(flexibleQuery);
        if (res.isEmpty()) {
            throw new NotFoundException
                    ("Sorry, but there are no products corresponding to your criteria.");
        }



        int numOfPages = res.size() % PAGE_CAPACITY == 0 ?
                res.size() / PAGE_CAPACITY : (res.size() / PAGE_CAPACITY) + 1;

        if (page.isPresent()) {
            res = res.subList(
                    (page.get() - 1) * PAGE_CAPACITY,
                    Math.min(res.size(), (page.get() - 1) * PAGE_CAPACITY + PAGE_CAPACITY));
        } else {
            page = Optional.of(1);
            res = res.subList(0, Math.min(res.size(), PAGE_CAPACITY));
        }

        for (Good good : res) {
            good.setPrice(good.getPrice(), good.getDiscount());
            good.setImage(mediaService.getCloudStorage().getResourceUrl(good.getImage()));
        }
//        if (page.isPresent()) {
//            flexibleQuery += " LIMIT " + PAGE_CAPACITY + " OFFSET " + (page.get() - 1) * PAGE_CAPACITY;
//        } else {
//            flexibleQuery += " LIMIT " + PAGE_CAPACITY;
//            page = Optional.of(1);
//        }
        Map<String, Object> response = new HashMap<>();
        response.put("current", page.get());
        response.put("total", numOfPages);
        response.put("result_set", res);

        return response;
    }

    @Override
    public List<String> getCategories() throws NotFoundException{
        return repository.getCategories();
    }
}
