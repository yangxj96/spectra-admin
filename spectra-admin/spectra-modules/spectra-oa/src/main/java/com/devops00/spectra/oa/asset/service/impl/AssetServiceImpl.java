package com.devops00.spectra.oa.asset.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.oa.asset.javabean.entity.Asset;
import com.devops00.spectra.oa.asset.mapper.AssetMapper;
import com.devops00.spectra.oa.asset.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/// 资产表主表-服务实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 14:07
@Slf4j
@Service
public class AssetServiceImpl extends BaseServiceImpl<AssetMapper, Asset> implements AssetService {
}
