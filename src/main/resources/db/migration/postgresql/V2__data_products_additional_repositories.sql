create table if not exists data_products_additional_repositories (
    uuid varchar(36) primary key,
    manifest_key varchar(255) not null,
    external_identifier varchar(255),
    name varchar(255),
    description text,
    remote_url_http text,
    remote_url_ssh text,
    default_branch varchar(255),
    provider_type varchar(255),
    provider_base_url text,
    owner_id varchar(255),
    owner_type varchar(255),
    data_product_uuid varchar(36) not null references data_products(uuid) on delete cascade
);
