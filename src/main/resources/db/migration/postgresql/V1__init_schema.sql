-- Registry Server Database Schema
-- This file will contain the initial database schema for the ODM Platform Registry Server


CREATE TABLE data_product (
    uuid                VARCHAR(36) PRIMARY KEY,
    fqn                 VARCHAR(255),
    domain              VARCHAR(255),
    name                VARCHAR(255),
    display_name        VARCHAR(255),
    description         TEXT,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE TABLE data_product_repository (
    uuid                VARCHAR(36) PRIMARY KEY,
    external_identifier VARCHAR(255),
    name                VARCHAR(255),
    description         TEXT,
    descriptor_root_path VARCHAR(500),
    remote_url_http     VARCHAR(500),
    remote_url_ssh      VARCHAR(500),
    default_branch      VARCHAR(255),
    provider_type       VARCHAR(50),
    provider_base_url   VARCHAR(500),
    data_product_uuid   VARCHAR(36),
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    CONSTRAINT fk_data_product
        FOREIGN KEY (data_product_uuid) REFERENCES data_product (uuid)
);