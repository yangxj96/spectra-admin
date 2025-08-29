package io.github.yangxj96.spectra.core.system.service.impl;

import io.github.yangxj96.spectra.core.system.javabean.vo.CPUInfoVO;
import io.github.yangxj96.spectra.core.system.javabean.vo.JVMInfoVO;
import io.github.yangxj96.spectra.core.system.javabean.vo.RAMInfoVO;
import io.github.yangxj96.spectra.core.system.service.ServiceMonitorService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 服务器信息监控
 */
@Service
public class ServiceMonitorServiceImpl implements ServiceMonitorService {

    @Override
    public CPUInfoVO getCPUInfo() {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        CentralProcessor processor = hardware.getProcessor();
        // 获取 CPU 标识信息（来自 ProcessorIdentifier）
        CentralProcessor.ProcessorIdentifier identifier = processor.getProcessorIdentifier();
        long maxFreq = processor.getMaxFreq();
        return CPUInfoVO.builder()
                .vendor(identifier.getVendor())
                .name(identifier.getName())
                .family(identifier.getFamily())
                .model(identifier.getModel())
                .stepping(identifier.getStepping())
                .identifier(identifier.getIdentifier())
                .is64bit(identifier.isCpu64bit())
                .physicalCores(processor.getPhysicalProcessorCount())
                .logicalCores(processor.getLogicalProcessorCount())
                .maxFrequencyHz(maxFreq)
                .maxFrequencyGhz(maxFreq > 0 ? String.format("%.2f GHz", maxFreq / 1e9) : "N/A")
                .build();

    }

    @Override
    public RAMInfoVO getRAMInfo() {
        // 预定返回
        var vo = new RAMInfoVO();
        vo.setSlots(new ArrayList<>());

        // 获取信息
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        List<PhysicalMemory> memoryList = hardware.getMemory().getPhysicalMemory();
        if (memoryList.isEmpty()) {
            // 如果无法获取物理内存条信息（如在某些虚拟机中），提供一个默认信息
            RAMInfoVO.RAMSlot unknown = RAMInfoVO.RAMSlot
                    .builder()
                    .slot(0)
                    .memoryType("Unknown")
                    .clockSpeedHz(0L)
                    .clockSpeedMHz("Unknown")
                    .capacityBytes(0L)
                    .capacityGB("Unknown")
                    .build();
            vo.getSlots().add(unknown);
            return vo;
        }

        for (int i = 0; i < memoryList.size(); i++) {
            PhysicalMemory memory = memoryList.get(i);
            RAMInfoVO.RAMSlot unknown = RAMInfoVO.RAMSlot
                    .builder()
                    .slot(i + 1)
                    .memoryType(memory.getMemoryType())
                    .clockSpeedHz(memory.getClockSpeed())
                    .clockSpeedMHz(memory.getClockSpeed() / 1_000_000 + " MHz")
                    .capacityBytes(memory.getCapacity())
                    .capacityGB(String.format("%.2f GB", memory.getCapacity() / Math.pow(1024, 3)))
                    .build();
            vo.getSlots().add(unknown);
        }

        long totalCapacity = memoryList.stream().mapToLong(PhysicalMemory::getCapacity).sum();

        vo.setSummary("Total Physical Memory");
        vo.setCount(memoryList.size() + " sticks");
        vo.setTotalCapacityBytes(totalCapacity);
        vo.setTotalCapacityGB(String.format("%.2f GB", totalCapacity / Math.pow(1024, 3)));

        return vo;
    }

    @Override
    public JVMInfoVO getJVMInfo() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return JVMInfoVO.builder()
                // 基础信息
                .jvmName(runtimeMXBean.getVmName())
                .jvmVendor(runtimeMXBean.getVmVendor())
                .jvmVersion(runtimeMXBean.getVmVersion())
                .jvmSpecName(runtimeMXBean.getSpecName())
                .jvmSpecVersion(runtimeMXBean.getSpecVersion())
                .jvmSpecVendor(runtimeMXBean.getSpecVendor())
                // 运行时环境
                .javaVersion(System.getProperty("java.version"))
                .javaHome(System.getProperty("java.home"))
                .javaVendor(System.getProperty("java.vendor"))
                .javaVendorUrl(System.getProperty("java.vendor.url"))
                // 启动信息
                .startTime(new Date(runtimeMXBean.getStartTime()))
                .pid(runtimeMXBean.getName())
                .processId(runtimeMXBean.getName().split("@")[0])
                // 启动参数
                .jvmArguments(runtimeMXBean.getInputArguments())
                // 系统属性
                .systemProperties(getFilteredProps())
                // 运行时类路径
                .classPath(runtimeMXBean.getClassPath())
                .libraryPath(runtimeMXBean.getLibraryPath())
                .build();
    }

    /**
     * 过滤敏感属性,只保留常见属性,防止泄露
     *
     * @return 属性map
     */
    private static Map<String, String> getFilteredProps() {
        Properties systemProps = System.getProperties();
        Map<String, String> filteredProps = new HashMap<>();
        // 只保留常见的、非敏感的系统属性
        List<String> includedKeys = Arrays.asList(
                "os.name",
                "os.version",
                "os.arch",
                "user.name",
                "user.home",
                "user.dir",
                "file.separator",
                "path.separator",
                "line.separator",
                "java.class.version",
                "java.io.tmpdir"
        );
        for (String key : includedKeys) {
            String value = systemProps.getProperty(key);
            if (value != null) {
                filteredProps.put(key, value);
            }
        }
        return filteredProps;
    }
}
