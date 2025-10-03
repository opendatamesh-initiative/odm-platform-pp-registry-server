create table if not exists data_products (
    uuid                varchar(36) primary key,
    fqn                 varchar(255),
    domain              varchar(255),
    name                varchar(255),
    display_name        varchar(255),
    description         text,
    validation_state    varchar(255),
    created_at          timestamp,
    updated_at          timestamp
);

create table if not exists data_products_repositories (
    uuid                varchar(36) primary key,
    external_identifier varchar(255),
    name                varchar(255),
    description         text,
    descriptor_root_path text,
    remote_url_http     text,
    remote_url_ssh      text,
    default_branch      varchar(255),
    provider_type       varchar(255),
    provider_base_url   text,
    data_product_uuid   varchar(36) references data_products(uuid) on delete cascade
);

create table if not exists data_products_versions (
    uuid                varchar(36) primary key,
    data_product_uuid   varchar(36) references data_products(uuid) on delete cascade,
    name                varchar(255),
    description         text,
    tag                 varchar(255),
    validation_state    varchar(255),
    descriptor_spec                varchar(255),
    descriptor_spec_version        varchar(255),
    descriptor_content             jsonb,
    created_at          timestamp,
    updated_at          timestamp
);