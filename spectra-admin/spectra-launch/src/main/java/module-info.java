import org.jspecify.annotations.NullMarked;

@NullMarked
module spectra.launch {
    requires static lombok;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires org.jspecify;
}