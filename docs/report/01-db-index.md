# 쿼리 성능 개선 보고서

**주제:** `product.id`를 인덱스에 **추가/미추가** 했을 때의 성능 비교

최신등록순으로 상품 목록을 조회할 때 기준 컬럼의 카디널리티가 낮아 동일값 그룹 내 순서가 보장되지 않습니다. 이를 보완하기 위해 타이브레이커로 `product.id`를 정렬 조건에 추가했습니다. 
그러나 타이브레이커를 정렬 조건에만 추가하는 것으로는 충분하지 않습니다. 쿼리 성능까지 확보하려면 인덱스에도 해당 컬럼을 반영해야 합니다.

이제 `product.id`를 포함한 인덱스와 제외한 인덱스의 성능을 비교해보겠습니다.

---

## 1) 목적

- **대상 쿼리**

    ```sql
    SELECT p1_0.*, b1_0.name
    FROM product p1_0
    JOIN brand  b1_0 ON p1_0.brand_id = b1_0.id
    WHERE b1_0.status = 'ACTIVE'
      AND p1_0.status = 'AVAILABLE'
    ORDER BY p1_0.sale_start_date DESC, p1_0.id DESC
    LIMIT ?, ?; -- 1페이지(0,50), 10페이지(450,50) 등
    ```

- **비교 인덱스**
    - 1안(id미포함): `product(status, sale_start_date DESC)`
    - 2안(id포함): `product(status, sale_start_date DESC, id DESC)`
- **목표:** filesort 제거 + LIMIT 조기 종료로 응답 시간 대폭 단축

---

## 2) 환경

- DB: MySQL 8.0.x (InnoDB)
- 데이터: `product`  100만 행(AVAILABLE 비중 높음), `brand` 수 2,000행
- 동일 스냅샷/세션 파라미터에서 측정

### 실험 설계

- 시나리오: ① LIMIT 0,50 ② LIMIT 450,50
- 비교군: 1안 ↔ 2안 (비교 시에는 다른 안의 인덱스 제거)
- 측정: 각 3회 이상 반복 → **중앙값** 기록
- 수집물: EXPLAIN 테이블/JSON 결과, EXPLAIN ANALYZE 결과

---

## 3) 개선 전 — **id 미포함 인덱스**

```sql
CREATE INDEX idx_product_status_sdate
  ON product (status, sale_start_date DESC);
```

### 관찰

- EXPLAIN 실행 계획

    ```sql
    id|select_type|table|partitions|type  |possible_keys              |key                     |key_len|ref                  |rows  |filtered|Extra                                |
    --+-----------+-----+----------+------+---------------------------+------------------------+-------+---------------------+------+--------+-------------------------------------+
     1|SIMPLE     |p1_0 |          |ref   |idx_product_status_sdate   |idx_product_status_sdate|2      |const                |497394|   100.0|Using index condition; Using filesort|
     1|SIMPLE     |b1_0 |          |eq_ref|PRIMARY,idx_brand_status_id|PRIMARY                 |8      |loopers.p1_0.brand_id|     1|   68.15|Using where                          |
    ```

- EXPLAIN ANALYZE 실행 계획

    ```text
    -> Limit: 50 row(s)  
         (cost=535273 rows=50)  
         (actual time=1196..1196 rows=50 loops=1) //소요시간: 1.196s 
        
      //product를 바깥, brand를 안쪽으로 하는 NL 조인  
      -> Nested loop inner join 
           (cost=535273 rows=338974)  
           (actual time=1196..1196 rows=50 loops=1)
    				
          //Sort = filesort : 인덱스로 정렬을 커버 못해 정렬 수행	
          //정렬 기준 컬럼 인덱스에 일부 존재, 정렬 방향 불일치, WHERE조건 순서 깨지면 정렬 버퍼 사용
          //explian에서 Extra 컬럼 : Using filesort 표시
          -> Sort: p1_0.sale_start_date DESC, p1_0.id DESC  
               (cost=55966 rows=497394)  
               (actual time=1196..1196 rows=50 loops=1)
    					
              //해당 인덱스 사용, status만 탐색 키로 사용됨
              -> Index lookup on p1_0 using idx_product_status_sdate  
                   (status='AVAILABLE'),  
                   //ICP로 status 필터 적용 
                   with index condition: (p1_0.`status` = 'AVAILABLE') 
                   (cost=55966 rows=497394)  
                   //65만 행을 읽어 정렬 입력으로 넘김. 상품 총100만 행 
                   (actual time=0.415..1087 rows=654240 loops=1)
    
          -> Filter: (b1_0.`status` = 'ACTIVE') //brand 상태 필터는 조인 후 평가
               (cost=0.864 rows=0.682)  
               //최종 50행 각각에 대해 1건 통과(cheap)
               (actual time=677e-6..727e-6 rows=1 loops=50)
    					
              //brand PK 단건 조회(eq_ref). 비용 아주 작음
              -> Single-row index lookup on b1_0 using PRIMARY  
                   (id=p1_0.brand_id)  
                   (cost=0.864 rows=1)  
                   (actual time=534e-6..545e-6 rows=1 loops=50)
    ```


- 계획 핵심
    - `Index lookup on p1_0 using idx_product_status_sdate (status='AVAILABLE')`
    - `Sort: p1_0.sale_start_date DESC, p1_0.id DESC` **→ Using filesort**
- 실측 지표(대표)
    - **1페이지**(LIMIT 0,50): **~1,196 ms**
    - **10페이지**(LIMIT 450,50): **~1,061 ms**
    - `product` 단계에서 **~654,240행** 읽은 뒤 정렬 → 상위 50개만 사용

**원인:** 정렬 키(`sale_start_date, id`)가 인덱스에 없어 **임시테이블 + filesort**가 발생합니다.
LIMIT 50이라도, 원하는 순서의 상위 N을 얻기 전까지 **대량을 읽고 정렬**해야 합니다.

---

## 4) 개선안 — **id 포함 인덱스**

```sql
CREATE INDEX idx_product_status_sdate_id
  ON product (status, sale_start_date DESC, **id DESC**);
```

### 관찰

- EXPLAIN 실행 계획

    ```sql
    id|select_type|table|partitions|type  |possible_keys              |key                        |key_len|ref                  |rows  |filtered|Extra                |
    --+-----------+-----+----------+------+---------------------------+---------------------------+-------+---------------------+------+--------+---------------------+
     1|SIMPLE     |p1_0 |          |ref   |idx_product_status_sdate_id|idx_product_status_sdate_id|2      |const                |497394|   100.0|Using index condition|
     1|SIMPLE     |b1_0 |          |eq_ref|PRIMARY,idx_brand_status_id|PRIMARY                    |8      |loopers.p1_0.brand_id|     1|   68.15|Using where          |
    ```

- EXPLAIN ANALYZE 실행 계획

    ```sql
    -> Limit: 50 row(s)  
         (cost=535513 rows=50)  
         (actual time=0.431..0.468 rows=50 loops=1) //소요시간: 0.431s
    
      -> Nested loop inner join //product를 바깥, brand를 안쪽으로 하는 NL 조인
           (cost=535513 rows=338974)  
           (actual time=0.43..0.465 rows=50 loops=1)
    		
    	//Sort 노드 없어짐: filesort 제거 
        -> Index lookup on p1_0 using idx_product_status_sdate_id  
             (status='AVAILABLE'), //WHERE의 status 필터를 인덱스로 탐색
             //ICP로 status 조건 적용
             with index condition: (p1_0.`status` = 'AVAILABLE')  
             (cost=56206 rows=497394)  
             //★ **LIMIT 조기 종료**: product 단계에서 정확히 50행만 읽음
             (actual time=0.417..0.424 rows=50 loops=1)
    		
    	//brand 상태 필터. 상위 50행에 대해서만 평가
        -> Filter: (b1_0.`status` = 'ACTIVE')  
             (cost=0.864 rows=0.682)  
             //50회 반복 중 각 1건 통과(cheap)
             (actual time=575e-6..647e-6 rows=1 loops=50)
    
          //brand PK 단건 조회(eq_ref)
          -> Single-row index lookup on b1_0 using PRIMARY  
               (id=p1_0.brand_id)  
               (cost=0.864 rows=1)  
               (actual time=380e-6..406e-6 rows=1 loops=50)
    
    ```

- 계획 핵심
    - `Index lookup on p1_0 using idx_product_status_sdate_id (status='AVAILABLE')`
    - **정렬 노드 없음(= filesort 제거)**
- 실측 지표(대표)
    - **1페이지**: **0.43 ms**
    - **10페이지**: **~1.53–1.88 ms**

**개선 효과(실측 대비):**

- 1페이지: **~1291 ms → ~0.43 ms** ⇒ **대략 3,000–3,600배** 단축
- 10페이지: **~1061 ms → ~1.53 ms** ⇒ **대략 560배** 단축

---

## 5) 왜 이렇게 차이가 나는가

### ① “동일 날짜만 다시 정렬” 문제가 아님

- **오해 포인트:** “동일 날짜 묶음만 재정렬하면 비용이 작지 않나?”
- **현실:** `ORDER BY sale_start_date DESC, id DESC`는 **테이블 전체(또는 매우 큰 부분집합)** 의 전역 순서를 요구합니다.

  인덱스가 이를 **그대로 보장**하지 않으면, 엔진은 **상당량을 읽어 임시테이블에 쌓고 filesort**를 수행합니다.

  동일 날짜 묶음의 재정렬만 하는 게 아니라, **전역 Top-N**을 만들기 위해 **큰 입력을 정렬**해야 하므로 비용이 큽니다.


### ② 정렬 커버 여부

- 1안: 인덱스에 `id` 없음 → 전역 순서 보장 불가 → **filesort + 대량 읽기**
- 2안: `status, sale_start_date, id`가 **모두 인덱스에 포함** → 인덱스 순서 = 요구 정렬 → **정렬 단계 자체 삭제**

### ③ LIMIT 조기 종료(Early Termination)

- 2안은 인덱스 순서대로 스캔하며 **필요한 50개만** 읽고 즉시 멈춥니다.

  반면 1안은 “정렬 뒤 상위 50개”를 구하려고 **많이 읽고 정렬**해야 합니다.


> 참고(실제 EXPLAIN JSON):
>
>
> `used_key_parts`에 `status`만 찍혀도, "using_filesort": false
>
> 로 확인되듯 **뒤 컬럼(sale_start_date, id)** 로 정렬을 **만족**한 상태입니다. JSON은 “탐색 키”만 표시하는 특성이 있습니다.
>

---

## 6) 결론

- **정렬에 쓰는 모든 컬럼을 인덱스에 포함하면 filesort가 제거**되고 인덱스 순서 스캔으로 **LIMIT 조기 종료**가 가능해져 **극적인 성능 향상**이 일어납니다.

---

## 7) 더해서, 상품 목록에도 캐시가 필요한 이유

- **동일/유사 쿼리 재사용률 높음**
    - 기본 정렬/필터 조합은 랜딩페이지 역할로 다수 사용자에게 반복됩니다. 상위 N(첫 1~3페이지)은 특히 중복도가 높습니다. 자주 찾는 페이지의 DB 지연/스파이크 시에도 캐시가 버퍼 역할.
    - **어떻게:** 페이지 단위 캐시 키를 정해 저장. 파라미터 조합이 곧 캐시 키가 됨.
        - 키 예시: `products:list:{brandId}:{sort}:{page}:{size}`
            - [ ]  (todo) 키가 너무 복잡한데, 키 생성을 어떻게 하면 좋을까?
              size: 사용자가 변경할 수 없이 50으로 고정 (모바일은 다를 수 있음)
              status: 사용자가 변경하는 filter 가 아니므로 key에서 제외
              brandId: brandId 도 인기 브랜드만? 또는 brandId 없는 목록 조회만
        - **TTL**: 10~60초(트래픽/변동성에 따라 조절). 초단위 TTL도 효과 큼.
- **DB 부담 완화**
    - **무엇:** 정렬·조인·네트워크 왕복을 캐시가 흡수.
    - **왜:** 목록 트래픽은 고빈도·저가치 쿼리가 많아 DB를 쉽게 포화시킴.
    - **어떻게:** **캐시-어사이드(cache-aside)** 패턴 권장.
        1. 조회 시 캐시에 있으면 반환
        2. 없으면 DB 조회 후 캐시에 저장(+TTL)
        3. 쓰기 이벤트는 **소프트 무효화**(키 삭제/버전 증가)로 반영

---

## 8) [참고] 드라이빙 테이블

- 본 쿼리의 **드라이빙 테이블은 product**인 것이 유리합니다.
- 이유: 정렬·LIMIT이 모두 **product 컬럼 기준**이므로 product에서 **정렬 커버 인덱스 순서**로 걷다가 **LIMIT에서 멈추는** 것이 최적입니다.
- brand를 드라이빙으로 바꾸면 어차피 product를 합친 뒤 **글로벌 Top-N** 정렬을 다시 해야 하므로 이점이 거의 없습니다.

    ```sql
    CREATE INDEX idx_brand_status_id ON brand (status, id);
    ```

  *(brand가 커질 미래에 대비한 선필터/조인 안정성 목적; 현재 워크로드에선 효과 미미)*


## **9) 운영 팁**

인덱스 추가는 쓰기/디스크 오버헤드를 약간 늘립니다.  하지만 **읽기 중심의 목록 조회** 트래픽에서는 이득이 압도적입니다.

### 1.  트래픽 저부하 시간에 온라인 DDL

### 2. 히스토그램 통계 최신화 `ANALYZE TABLE product, brand`

- 히스토그램 도입 전에는 데이터가 균등하게 분포되었을 것이라고 가정하고 실행계획 수립했었음.
- 이 부분 극복하고자 히스토그램 도입
- 모든 값에 대한 분포가 아닐지라도, 각 범위별로 레코드 건수, 유니크한 값의 개수 갖음으로써 더 정확한 예측 가능
- 데이터 균등 분포 예측 → 특정 범위 데이터의 많고 적음 식별 가능
    - 쿼리 성능에 영향 미침
    - 조인시 조인 횟수를 줄일 수 있게 드라이빙 테이블 선정
- 히스토그램과 인덱스
    - 인덱스를 생성한 컬럼도 히스토그램 정보를 수집해야할까?
        - v8.0 MySQL 서버는 인덱스된 컬럼을 검색 조건으로 사용하는 경우, 히스토그램은 사용하지 않는다.  → 해당 실험에서 anayze 전과 후의 explain 값이 동일했다.
        - 항상 인덱스 다이브를 통함
- 인덱스 다이브: 레코드 건수 예측하기 위해 인덱스의 B-Tree를 샘플링해서 살펴보는 것