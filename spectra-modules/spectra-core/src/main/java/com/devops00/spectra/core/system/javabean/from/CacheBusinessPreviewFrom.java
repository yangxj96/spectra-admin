/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.from;

import java.util.List;

/** 普通业务缓存清理预览请求。 */
public record CacheBusinessPreviewFrom(List<String> regionCodes, boolean allRegions, boolean allInstances) {
}
