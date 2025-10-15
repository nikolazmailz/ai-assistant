-- liquibase formatted sql

-- changeset ai:008-alter-dialog_metainfo_title_not_null

ALTER TABLE dialog_metainfo
ALTER COLUMN title DROP NOT NULL;
