create table if not exists data_products_additional_repositories (
    uuid varchar(36) primary key,
    repository_key varchar(255) not null,
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

create table if not exists data_products_versions_additional_tags (
    sequence_id bigserial primary key,
    repository_key varchar(255) not null,
    tag varchar(255) not null,
    data_product_version_uuid varchar(36) not null references data_products_versions(uuid) on delete cascade
);
