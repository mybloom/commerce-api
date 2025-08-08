-- 브랜드 및 상품
INSERT INTO brand (id, name, description, status) VALUES (1, '나이키', '글로벌 브랜드', 'ACTIVE');
 INSERT INTO product (name, price, like_count, stock_quantity, brand_id, status, created_at, sale_start_date)
 VALUES ('테스트상품', 1000, 0, 1000, 1, 'AVAILABLE', NOW(), NOW());


-- 포인트
INSERT INTO point (id, user_id, balance) VALUES (1, 1, 10000);

-- 쿠폰
INSERT INTO coupon (id, name, discount_type, discount_value, start_at, end_at)
VALUES (1, '동시성 쿠폰', 'RATE', 0.100, '2025-01-01', '2099-01-01');

-- 유저 쿠폰 (단 1개)
INSERT INTO user_coupon (id, user_id, coupon_id, used, version, issued_at)
VALUES (1, 1, 1, false, 0, now());
