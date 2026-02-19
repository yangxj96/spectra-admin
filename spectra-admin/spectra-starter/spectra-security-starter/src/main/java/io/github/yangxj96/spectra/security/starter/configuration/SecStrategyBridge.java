package io.github.yangxj96.spectra.security.starter.configuration;

import io.github.yangxj96.spectra.security.base.holder.SecHolderStrategy;
import io.github.yangxj96.spectra.security.base.holder.SecUtil;

public class SecStrategyBridge {

    public SecStrategyBridge(SecHolderStrategy strategy) {
        SecUtil.setStrategy(strategy);
    }

}
