create table if not exists data_products (
    uuid                varchar(36) primary key,
    fqn                 varchar(255),
    domain              varchar(255),
    name                varchar(255),
    display_name        varchar(255),
    description         text,
    created_at          timestamp,
    updated_at          timestamp
);

create table if not exists data_products_repositories (
    uuid                varchar(36) primary key,
    external_identifier varchar(255),
    name                varchar(255),
    description         text,
    descriptor_root_path varchar(500),
    remote_url_http     varchar(500),
    remote_url_ssh      varchar(500),
    default_branch      varchar(255),
    provider_type       varchar(50),
    provider_base_url   varchar(500),
    data_product_uuid   varchar(36) references data_products(uuid) on delete cascade,
    created_at          timestamp,
    updated_at          timestamp
);