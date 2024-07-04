package com.nuvakhov.dmitrii.bookshop.handler;

import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.CqnUpdate;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import cds.gen.catalogservice.CatalogService_;
import cds.gen.my.bookshop.Orders;
import cds.gen.my.bookshop.OrderItems;
import cds.gen.my.bookshop.Books_;
import cds.gen.my.bookshop.Books;
import cds.gen.catalogservice.Orders_;

import java.util.List;

@Component
@ServiceName(CatalogService_.CDS_NAME)
@RequiredArgsConstructor
public class CatalogServiceHandler implements EventHandler {

    private final PersistenceService persistenceService;

    @Before(event = CqnService.EVENT_CREATE, entity = Orders_.CDS_NAME)
    public void validateOrder(List<Orders> orders) {
        orders
                .stream()
                .flatMap(order -> order.getItems().stream())
                .forEach(this::validateOrderItem);
    }

    private void validateOrderItem(OrderItems orderItem) {
        Integer bookId = orderItem.getBookId();

        CqnSelect select = Select.from(Books_.class).columns(Books_::stock).where(b -> b.ID().eq(bookId));
        Books book = persistenceService.run(select)
                .first(Books.class)
                .orElseThrow(() -> new ServiceException(ErrorStatuses.NOT_FOUND, "Book does not exist"));

        book.setStock(book.getStock() - orderItem.getAmount());
        CqnUpdate update = Update.entity(Books_.class).data(book).where(b -> b.ID().eq(bookId));
        persistenceService.run(update);
    }
}
