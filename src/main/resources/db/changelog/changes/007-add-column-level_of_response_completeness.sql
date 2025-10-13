-- liquibase formatted sql

-- changeset ai:007-add-column-level_of_response_completeness add column level_of_response_completeness to dialog_metainfo
ALTER TABLE dialog_metainfo
    ADD COLUMN level_of_response_completeness VARCHAR(32) DEFAULT 'MEDIUM';
