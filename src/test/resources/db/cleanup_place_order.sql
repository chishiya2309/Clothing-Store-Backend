TRUNCATE TABLE
    payment_attempts,
    payments,
    order_items,
    orders,
    checkout_session_items,
    voucher_reservations,
    inventory_reservations,
    checkout_sessions,
    cart_items,
    addresses,
    product_variants,
    products,
    categories,
    vouchers,
    users
RESTART IDENTITY CASCADE;
