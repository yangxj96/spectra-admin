import org.jspecify.annotations.NullMarked;

@NullMarked
module spectra.workflow {
    requires jakarta.annotation;
    requires static lombok;
    requires spring.context;
    requires spring.web;
    requires org.jspecify;
}
