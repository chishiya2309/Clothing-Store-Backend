package vn.hcmute.edu.dp.nhom10.backend.pattern.command.review;

public interface ReviewCommand<T> {
    T execute();
    String getDescription();
}
