package vn.hcmute.edu.dp.nhom10.backend.pattern.command.catalog;

public interface CatalogCommand<T> {
    T execute();
    String getDescription();
}
