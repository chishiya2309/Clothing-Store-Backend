package vn.hcmute.edu.dp.nhom10.backend.pattern.chain.product;

public abstract class ProductValidationHandler {
    private ProductValidationHandler next;

    public ProductValidationHandler setNext(ProductValidationHandler next) {
        this.next = next;
        return next;
    }

    public void handle(ProductValidationContext context) {
        validate(context);
        if (next != null) {
            next.handle(context);
        }
    }

    protected abstract void validate(ProductValidationContext context);
}
