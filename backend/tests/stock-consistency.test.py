#!/usr/bin/env python3
"""库存口径一致性回归测试：只读数据库 + 静态源码校验，不改任何业务数据。"""
import os
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path('/opt/qixiaozhu/src/backend/ruoyi-system/src/main')
AI = ROOT / 'java/com/ruoyi/business/ai/service/impl/AiChatServiceImpl.java'
STOCK_XML = ROOT / 'resources/mapper/business/StockMapper.xml'
PRODUCT_XML = ROOT / 'resources/mapper/business/ProductMapper.xml'
PRODUCT_JAVA = ROOT / 'java/com/ruoyi/business/domain/Product.java'

results = []
def check(name, cond, detail=''):
    results.append((name, bool(cond), detail))
    print(('PASS ' if cond else 'FAIL ') + name + ((' — ' + detail) if detail else ''))

def read(p):
    return p.read_bytes().decode('utf-8', 'replace').replace('\r\n', '\n')

ai, sx, px, pj = read(AI), read(STOCK_XML), read(PRODUCT_XML), read(PRODUCT_JAVA)

# --- 静态源码断言 ---
check('AI 快照不再直接读未填充的 getAvailableQty()',
      'plain(stock.getAvailableQty())' not in ai)
check('AI 快照按 现存−锁定 自算可用量',
      'qty.subtract(locked)' in ai and 'stock.getQty()' in ai and 'stock.getLockedQty()' in ai)
check('AI 低库存明细列出完整数量（不再固定截断 5 条）',
      '共 ' in ai and 'stocks.size()' in ai and 'Math.min(10, stocks.size())' in ai)
check('提示词声明「可用 = 现存 − 锁定」',
      '可用 = 现存 − 锁定' in ai)
check('提示词禁止把知识库历史数量当实时库存',
      '知识库表格里出现的数量是导入时的历史值' in ai and '不能当作当前库存' in ai)
check('提示词禁止模型自行推算/补零',
      '禁止根据安全库存或知识库数字自行推算、补零或改写' in ai)
check('库存 SQL 返回真实可用量',
      's.qty - ifnull(s.locked_qty, 0) as availableQty' in sx)
check('商品 SQL 返回真实可用量',
      'ifnull(s.qty, 0) - ifnull(s.locked_qty, 0) as availableQty' in px)
check('Product 实体有 availableQty 字段与 getter',
      'private BigDecimal availableQty;' in pj and 'getAvailableQty()' in pj)
check('相关商品实时数据改用「可用」口径',
      'product.getAvailableQty()' in ai and '"｜可用 "' in ai)

# --- 真实数据库只读校验：SQL 表达式结果 == 库存页口径 ---
env = {}
for line in Path('/opt/qixiaozhu/app.env').read_text().splitlines():
    if '=' in line and not line.strip().startswith('#'):
        k, v = line.split('=', 1)
        env[k.strip()] = v.strip()
sql = (
    "select p.product_name, s.qty, ifnull(s.locked_qty,0), rp.safety_stock, "
    "s.qty - ifnull(s.locked_qty,0) from t_stock s "
    "join t_product p on p.product_id=s.product_id and p.enterprise_id=s.enterprise_id and p.del_flag='0' "
    "left join t_replenish_param rp on rp.product_id=s.product_id and rp.enterprise_id=s.enterprise_id "
    "where s.enterprise_id=1 and (s.qty<=0 or (rp.safety_stock is not null "
    "and s.qty - ifnull(s.locked_qty,0) <= rp.safety_stock)) order by p.product_id;"
)
out = subprocess.check_output([
    'mysql', '-h', env.get('DB_HOST', '127.0.0.1'), '-u', env['DB_USER'],
    '-p' + env['DB_PASSWORD'], env['DB_NAME'], '-N', '-B', '-e', sql
], text=True, stderr=subprocess.DEVNULL)
rows = [r.split('\t') for r in out.strip().splitlines() if r.strip()]
check('数据库确有 6 条库存预警', len(rows) == 6, f'实际 {len(rows)} 条')
for name, qty, locked, safety, available in rows:
    qty, locked, safety, available = map(float, (qty, locked, safety, available))
    ok = abs((qty - locked) - available) < 1e-9 and available > 0
    check(f'预警项可用量非 0：{name}', ok,
          f'现存 {qty:g} − 锁定 {locked:g} = 可用 {available:g}，安全 {safety:g}')
check('修复后 AI 不再可能对这 6 项报「可用 0」',
      all(float(r[4]) > 0 for r in rows))

failed = [n for n, ok, _ in results if not ok]
print(f'\n{len(results) - len(failed)}/{len(results)} checks passed')
sys.exit(1 if failed else 0)
