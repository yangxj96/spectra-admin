package com.devops00.spectra.security.starter.configuration;

import com.devops00.spectra.security.base.holder.SecHolderStrategy;
import com.devops00.spectra.security.base.holder.SecUtil;

public class SecStrategyBridge {

    public SecStrategyBridge(SecHolderStrategy strategy) {
        SecUtil.setStrategy(strategy);
    }

}
