package org.quarkus.assignment.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;
import org.quarkus.assignment.dto.SummaryDto;
import org.quarkus.assignment.model.Product;
import org.quarkus.assignment.model.SortOrder;
import org.quarkus.assignment.persistence.ProductRepository;

import io.quarkus.panache.common.Sort;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductService {

    private static final Logger LOG = Logger.getLogger(ProductService.class);

    @Inject
    ProductRepository repository;

    public Uni<List<Product>> getAllProducts() {
        return repository.listAll();
    }

    public Uni<Product> getProductById(long id) {
        return repository.findById(id);
    }

    public Uni<Product> getRequiredProductById(long id) {
        return repository.findById(id).onItem().ifNull().failWith(NotFoundException::new);
    }

    public Uni<SummaryDto> createProducts(List<Product> products) {
        Set<Long> ids = products.stream().map(Product::getId).collect(Collectors.toSet());
        return repository.find("_id in ?1", ids).list().onItem().transformToUni(existing -> {
            Set<Long> existingIds = existing.stream().map(Product::getId).collect(Collectors.toSet());
            List<Product> onlyNew = products.stream().filter(p -> !existingIds.contains(p.getId())).collect(Collectors.toList());
            int created = onlyNew.size();
            int duplicates = products.size() - created;
            if (onlyNew.isEmpty()) {
                return Uni.createFrom().item(SummaryDto.builder().created(0).duplicates(duplicates).updated(0).total(products.size()).build());
            }
            return repository.persist(onlyNew)
                .replaceWith(SummaryDto.builder().created(created).duplicates(duplicates).updated(0).total(products.size()).build());
        });
    }

    public Uni<SummaryDto> updateProducts(List<Product> products) {
        Set<Long> ids = products.stream().map(Product::getId).collect(Collectors.toSet());
        return repository.find("_id in ?1", ids).list().onItem().transformToUni(existing -> {
            Set<Long> existingIds = existing.stream().map(Product::getId).collect(Collectors.toSet());
            int updated = (int) products.stream().filter(p -> existingIds.contains(p.getId())).count();
            int created = products.size() - updated;
            return repository.persistOrUpdate(products)
                .replaceWith(SummaryDto.builder().created(created).updated(updated).duplicates(0).total(products.size()).build());
        });
    }

    public Uni<SummaryDto> patchProducts(List<Product> products) {
        Set<Long> ids = products.stream().map(Product::getId).collect(Collectors.toSet());
        return repository.find("_id in ?1", ids).list().onItem().transformToUni(existing -> {
            // Merge existing fields where present
            java.util.Map<Long, Product> idToExisting = existing.stream().collect(Collectors.toMap(Product::getId, p -> p));
            int updated = 0;
            int created = 0;
            for (Product patch : products) {
                Product ex = idToExisting.get(patch.getId());
                if (ex != null) {
                    if (patch.getName() != null) ex.setName(patch.getName());
                    if (patch.getDescription() != null) ex.setDescription(patch.getDescription());
                    if (patch.getPrice() != null) ex.setPrice(patch.getPrice());
                    if (patch.getQuantity() != null) ex.setQuantity(patch.getQuantity());
                    updated++;
                } else {
                    idToExisting.put(patch.getId(), patch);
                    created++;
                }
            }
            List<Product> toPersist = idToExisting.values().stream().collect(Collectors.toList());
            return repository.persistOrUpdate(toPersist)
                .replaceWith(SummaryDto.builder().created(created).updated(updated).duplicates(0).total(products.size()).build());
        });
    }

    public Uni<Void> deleteByIdOrThrow(long id) {
        return repository.deleteById(id).onItem().transformToUni(deleted -> {
            if (Boolean.TRUE.equals(deleted)) return Uni.createFrom().voidItem();
            return Uni.createFrom().failure(new NotFoundException("Requested id not found for deletion"));
        });
    }

    public Uni<Boolean> isAvailable(long id, int count) {
        return getRequiredProductById(id).onItem().transform(p -> p.getQuantity() != null && p.getQuantity() >= count);
    }

    public Uni<Integer> getAvailableQuantity(long id) {
        return getRequiredProductById(id).onItem().transform(p -> p.getQuantity() != null ? p.getQuantity() : 0);
    }

    public Uni<List<Product>> getAllSortedByPrice(SortOrder order) {
        Sort sort = order == SortOrder.DESC ? Sort.by("price").descending() : Sort.by("price").ascending();
        return repository.findAll(sort).list();
    }
}
