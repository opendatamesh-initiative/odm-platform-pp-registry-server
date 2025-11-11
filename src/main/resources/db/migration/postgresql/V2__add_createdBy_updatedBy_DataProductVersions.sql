alter table if exists data_products_versions
    add column if not exists created_by varchar(255);

alter table if exists data_products_versions
    add column if not exists updated_by varchar(255);