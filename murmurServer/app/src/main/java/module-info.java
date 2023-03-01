module org.helmo {
    requires com.google.gson;

    opens org.helmo.reseau.infrastructures.dto to com.google.gson;
    exports org.helmo.reseau;
}