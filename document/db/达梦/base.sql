CREATE TABLE DB_AUTH."t_account"
(
    "id"         BIGINT NOT NULL,



    "created_by" BIGINT,
    "created_at" TIMESTAMP,
    "updated_by" BIGINT,
    "updated_at" TIMESTAMP,
    "deleted"    TIMESTAMP,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE DB_AUTH."t_account" IS '账号表';
COMMENT ON COLUMN DB_AUTH."t_account"."id" IS '主键ID';
COMMENT ON COLUMN DB_AUTH."t_account"."created_by" IS '创建人';
COMMENT ON COLUMN DB_AUTH."t_account"."created_at" IS '创建时间';
COMMENT ON COLUMN DB_AUTH."t_account"."updated_by" IS '最后更新人';
COMMENT ON COLUMN DB_AUTH."t_account"."updated_at" IS '最后更新时间';
COMMENT ON COLUMN DB_AUTH."t_account"."deleted" IS '删除时间';