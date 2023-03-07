module org.helmo {
    requires com.google.gson;

    opens org.helmo.reseau.infrastructures to com.google.gson;
    exports org.helmo.reseau;
}