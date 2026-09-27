-- 默认项按字典组唯一；禁用项保留数据供历史值解析，不再参与默认选择。
WITH ranked_default_items AS (
    SELECT
        id,
        row_number() OVER (PARTITION BY gid ORDER BY sort, id) AS default_rank
    FROM spectra_core.sys_dict_item
    WHERE deleted IS NULL
      AND default_flag IS TRUE
      AND state = 0
)
UPDATE spectra_core.sys_dict_item AS dict_item
SET default_flag = false
WHERE dict_item.deleted IS NULL
  AND dict_item.default_flag IS TRUE
  AND (
      dict_item.state IS DISTINCT FROM 0
      OR dict_item.id IN (
          SELECT ranked.id
          FROM ranked_default_items AS ranked
          WHERE ranked.default_rank > 1
      )
  );

CREATE UNIQUE INDEX uk_sys_dict_item_default_gid
    ON spectra_core.sys_dict_item (gid)
    WHERE default_flag IS TRUE
      AND deleted IS NULL;
