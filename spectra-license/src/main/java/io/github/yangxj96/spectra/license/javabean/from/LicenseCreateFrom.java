package io.github.yangxj96.spectra.license.javabean.from;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建许可入参
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LicenseCreateFrom implements Serializable {

    /**
     * 产品名称
     */
    private String productName;


    /**
     * 硬件ID
     */
    private String hwid;

}
