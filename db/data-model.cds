namespace my.bookshop;
using {User, Country, managed, cuid, Currency} from '@sap/cds/common';

entity Books {
    key ID : Integer;
    title : localized String;
    author : Association to Authors;
    stock : Integer;
}

entity Authors {
    key ID : Integer;
    name : String;
    books : Association to many Books on books.author = $self;
}

@Capabilities.Updatable : false
entity Orders : cuid, managed {
    items : Composition of many OrderItems on items.parent = $self;
    total : Decimal(9, 2)@readonly;
    currency : Currency;
}

@Capabilities.Updatable : false
entity OrderItems : cuid {
    parent : Association to Orders not null;
    book_ID : Integer;
    amount : Integer;
    netAmount : Decimal(9, 2)@readonly
}