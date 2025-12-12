import org.jspecify.annotations.NullMarked;

@NullMarked
module spectra.license {
    requires com.fasterxml.jackson.annotation;
    requires com.github.oshi;
    requires jakarta.annotation;
    requires static lombok;
    requires org.bouncycastle.provider;
    requires spectra.common;
    requires spring.boot;
    requires spring.context;
    requires spring.core;
    requires spring.web;
    requires tools.jackson.databind;
    requires org.jspecify;
}