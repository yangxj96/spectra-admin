package com.yangxj96.spectra.core.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 *
 * @author 杨新杰
 * @since 2025/5/26 17:04
 */
@Data
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity implements Serializable {

    /**
     * 数据id.
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "修改数据时,ID不能为null", groups = {Verify.Insert.class})
    @Null(message = "插入数据时,ID不能有值", groups = {Verify.Insert.class})
    private Long id;

    /**
     * 创建人
     */
    @JsonIgnore
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    @Null(message = "非公开字段", groups = {Verify.Insert.class, Verify.Update.class})
    private Long createdBy;

    /**
     * 创建时间
     */
    @JsonIgnore
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Null(message = "非公开字段", groups = {Verify.Insert.class, Verify.Update.class})
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    @JsonIgnore
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    @Null(message = "非公开字段", groups = {Verify.Insert.class, Verify.Update.class})
    private Long updatedBy;

    /**
     * 更新时间
     */
    @JsonIgnore
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @Null(message = "非公开字段", groups = {Verify.Insert.class, Verify.Update.class})
    private LocalDateTime updatedAt;

    /**
     * 删除标识
     */
    @JsonIgnore
    @TableField(value = "deleted")
    @Null(message = "非公开字段", groups = {Verify.Insert.class, Verify.Update.class})
    private LocalDateTime deleted;
}
