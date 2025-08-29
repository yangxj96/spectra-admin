package io.github.yangxj96.spectra.core.system.javabean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内存信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RAMInfoVO {

    private String summary;
    private String count;
    private Long totalCapacityBytes;
    private String totalCapacityGB;

    private List<RAMSlot> slots;

    @Data
    @Builder
    public static class RAMSlot {

        private Integer slot;

        private String memoryType;

        private Long clockSpeedHz;

        private String clockSpeedMHz;

        private Long capacityBytes;

        private String capacityGB;

    }

}
