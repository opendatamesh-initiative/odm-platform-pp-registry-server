ALTER TABLE data_products
    ADD COLUMN IF NOT EXISTS extension_properties jsonb;

ALTER TABLE data_products_versions
    ADD COLUMN IF NOT EXISTS extension_properties_snapshot jsonb;

CREATE INDEX IF NOT EXISTS idx_data_products_extension_properties_gin
    ON data_products USING gin (extension_properties jsonb_path_ops);

CREATE INDEX IF NOT EXISTS idx_data_products_versions_extension_snapshot_gin
    ON data_products_versions USING gin (extension_properties_snapshot jsonb_path_ops);
