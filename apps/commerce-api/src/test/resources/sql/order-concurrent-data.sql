
 -- 1. 유저 포인트 초기화
 INSERT INTO point (user_id, balance) VALUES (1, 100000);

 -- 2. 상품 1개 생성 (재고는 충분히 설정)
 INSERT INTO product (name, price, like_count, stock_quantity, brand_id, status, created_at, sale_start_date)
 VALUES ('테스트상품', 1000, 0, 1000, 1, 'AVAILABLE', NOW(), NOW());

 -- 1. 주문 20건 INSERT
 INSERT INTO orders
     (id, user_id, status, total_amount, discount_amount, payment_amount, order_request_id, created_at)
 VALUES
     (1001, 1, 'PENDING', 1000, 0, 1000, 'req-1001', NOW()),
     (1002, 1, 'PENDING', 1000, 0, 1000, 'req-1002', NOW()),
     (1003, 1, 'PENDING', 1000, 0, 1000, 'req-1003', NOW()),
     (1004, 1, 'PENDING', 1000, 0, 1000, 'req-1004', NOW()),
     (1005, 1, 'PENDING', 1000, 0, 1000, 'req-1005', NOW()),
     (1006, 1, 'PENDING', 1000, 0, 1000, 'req-1006', NOW()),
     (1007, 1, 'PENDING', 1000, 0, 1000, 'req-1007', NOW()),
     (1008, 1, 'PENDING', 1000, 0, 1000, 'req-1008', NOW()),
     (1009, 1, 'PENDING', 1000, 0, 1000, 'req-1009', NOW()),
     (1010, 1, 'PENDING', 1000, 0, 1000, 'req-1010', NOW()),
     (1011, 1, 'PENDING', 1000, 0, 1000, 'req-1011', NOW()),
     (1012, 1, 'PENDING', 1000, 0, 1000, 'req-1012', NOW()),
     (1013, 1, 'PENDING', 1000, 0, 1000, 'req-1013', NOW()),
     (1014, 1, 'PENDING', 1000, 0, 1000, 'req-1014', NOW()),
     (1015, 1, 'PENDING', 1000, 0, 1000, 'req-1015', NOW()),
     (1016, 1, 'PENDING', 1000, 0, 1000, 'req-1016', NOW()),
     (1017, 1, 'PENDING', 1000, 0, 1000, 'req-1017', NOW()),
     (1018, 1, 'PENDING', 1000, 0, 1000, 'req-1018', NOW()),
     (1019, 1, 'PENDING', 1000, 0, 1000, 'req-1019', NOW()),
     (1020, 1, 'PENDING', 1000, 0, 1000, 'req-1020', NOW());

 -- 2. 주문 상품 1개씩 INSERT (상품 id = 1 사용, 수량 = 1, 가격 = 1000)
 INSERT INTO order_line
     (id, order_id, product_id, quantity_amount, product_price, created_at)
 VALUES
     (2001, 1001, 1, 1, 1000, NOW()),
     (2002, 1002, 1, 1, 1000, NOW()),
     (2003, 1003, 1, 1, 1000, NOW()),
     (2004, 1004, 1, 1, 1000, NOW()),
     (2005, 1005, 1, 1, 1000, NOW()),
     (2006, 1006, 1, 1, 1000, NOW()),
     (2007, 1007, 1, 1, 1000, NOW()),
     (2008, 1008, 1, 1, 1000, NOW()),
     (2009, 1009, 1, 1, 1000, NOW()),
     (2010, 1010, 1, 1, 1000, NOW()),
     (2011, 1011, 1, 1, 1000, NOW()),
     (2012, 1012, 1, 1, 1000, NOW()),
     (2013, 1013, 1, 1, 1000, NOW()),
     (2014, 1014, 1, 1, 1000, NOW()),
     (2015, 1015, 1, 1, 1000, NOW()),
     (2016, 1016, 1, 1, 1000, NOW()),
     (2017, 1017, 1, 1, 1000, NOW()),
     (2018, 1018, 1, 1, 1000, NOW()),
     (2019, 1019, 1, 1, 1000, NOW()),
     (2020, 1020, 1, 1, 1000, NOW());
