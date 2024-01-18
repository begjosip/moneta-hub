ALTER TABLE moneta_user
    ADD profile_picture TEXT;

ALTER TABLE moneta_user
    ADD CONSTRAINT uc_moneta_user__profile_picture UNIQUE (profile_picture);