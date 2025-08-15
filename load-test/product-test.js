import http from 'k6/http';
import { baseline } from './scenarios/baseline.js';
import { rampup } from './scenarios/rampup.js';
import { spike } from './scenarios/spike.js';
import { stress } from './scenarios/stress.js';
import { check } from 'k6';


const BASE = 'http://localhost:8080';

const SCENARIO = __ENV.SCENARIO || 'baseline';

const scenarios = {
  baseline: baseline,
  rampup: rampup,
  spike: spike,
  stress: stress,
};

export const options = {
  scenarios: scenarios[SCENARIO],
};

export default function () {
  const page = 0;
  const size = 50;
  const sort = 'LATEST';

  const url = `${BASE}/api/v1/products?pagingCondition.page=${page}&pagingCondition.size=${size}&sortCondition=${sort}`;
  const res = http.get(url);

  check(res, {
      'status is 200': (r) => r.status === 200,
    });
}
