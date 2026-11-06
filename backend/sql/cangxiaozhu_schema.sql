-- ----------------------------------------------------------------------------
-- 物小助智能补货采购平台 - 业务表结构
--
-- 执行顺序：
--   1) sql/ry_20260319.sql        若依基础表（首次部署）
--   2) sql/quartz.sql             定时任务表（首次部署）
--   3) sql/cangxiaozhu_schema.sql 本脚本（可重复执行）
--
-- 约定：
--   * 所有业务表以 enterprise_id 作为单位隔离字段，服务端强制注入；
--   * 金额 decimal(18,2)，数量 decimal(18,3)；
--   * del_flag: 0 存在 / 2 删除；status: 0 正常 / 1 停用（业务状态各表单独说明）。
-- ----------------------------------------------------------------------------

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- 1. 单位
-- ----------------------------------------------------------------------------
create table if not exists t_enterprise (
  id                bigint(20)      not null auto_increment    comment '单位ID',
  name              varchar(100)    not null                   comment '单位名称',
  short_name        varchar(50)     default ''                 comment '单位简称',
  logo              varchar(255)    default ''                 comment 'Logo地址',
  contact           varchar(50)     default ''                 comment '联系人',
  phone             varchar(20)     default ''                 comment '联系电话',
  pay_term_days     int(4)          default 30                 comment '默认账期（天）',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (id)
) engine=innodb auto_increment=100 default charset=utf8mb4 comment='单位信息';

-- 微信绑定：账号密码登录为主，绑定表用于后续订阅消息与小程序身份
create table if not exists t_app_user_bind (
  bind_id           bigint(20)      not null auto_increment    comment '主键',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  user_id           bigint(20)      not null                   comment '系统用户ID',
  openid            varchar(64)     not null                   comment '微信 openid',
  unionid           varchar(64)     default null               comment '微信 unionid',
  bind_time         datetime                                   comment '绑定时间',
  primary key (bind_id),
  unique key uk_bind_openid (enterprise_id, openid),
  key idx_bind_user (user_id)
) engine=innodb default charset=utf8mb4 comment='微信账号绑定';

-- ----------------------------------------------------------------------------
-- 2. 商品与供应商档案
-- ----------------------------------------------------------------------------
create table if not exists t_product_category (
  category_id                bigint(20)      not null auto_increment    comment '分类ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  parent_id         bigint(20)      default 0                  comment '父分类ID',
  name              varchar(50)     not null                   comment '分类名称',
  sort              int(4)          default 0                  comment '显示顺序',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (category_id),
  key idx_category_ent (enterprise_id, parent_id)
) engine=innodb default charset=utf8mb4 comment='商品分类';

create table if not exists t_product (
  product_id                bigint(20)      not null auto_increment    comment '商品ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  category_id       bigint(20)      default null               comment '分类ID',
  product_code      varchar(64)     not null                   comment '商品编码',
  product_name      varchar(100)    not null                   comment '商品名称',
  spec              varchar(100)    default ''                 comment '规格型号',
  unit              varchar(20)     default ''                 comment '计量单位',
  ref_price         decimal(18,2)   default 0.00               comment '参考进价',
  supplier_id       bigint(20)      default null               comment '默认供应商ID',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  remark            varchar(500)    default null               comment '备注',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (product_id),
  unique key uk_product_code (enterprise_id, product_code),
  key idx_product_name (enterprise_id, product_name),
  key idx_product_category (category_id)
) engine=innodb default charset=utf8mb4 comment='商品档案';

create table if not exists t_supplier (
  supplier_id                bigint(20)      not null auto_increment    comment '供应商ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  supplier_code     varchar(64)     not null                   comment '供应商编码',
  supplier_name     varchar(100)    not null                   comment '供应商名称',
  contact           varchar(50)     default ''                 comment '联系人',
  phone             varchar(20)     default ''                 comment '联系电话',
  email             varchar(100)    default ''                 comment '邮箱',
  address           varchar(255)    default ''                 comment '地址',
  pay_term_days     int(4)          default 30                 comment '账期（天）',
  lead_days         int(4)          default 3                  comment '交期（天）',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  remark            varchar(500)    default null               comment '备注',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (supplier_id),
  unique key uk_supplier_code (enterprise_id, supplier_code),
  key idx_supplier_name (enterprise_id, supplier_name)
) engine=innodb default charset=utf8mb4 comment='供应商档案';

-- ----------------------------------------------------------------------------
-- 3. 库存与自动补货
-- ----------------------------------------------------------------------------
create table if not exists t_stock (
  stock_id                bigint(20)      not null auto_increment    comment '库存ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  product_id        bigint(20)      not null                   comment '商品ID',
  qty               decimal(18,3)   not null default 0         comment '现存量',
  locked_qty        decimal(18,3)   not null default 0         comment '锁定出库量',
  in_transit_qty    decimal(18,3)   not null default 0         comment '在途采购量',
  update_time       datetime                                   comment '更新时间',
  primary key (stock_id),
  unique key uk_stock_product (enterprise_id, product_id),
  constraint ck_stock_qty check (qty >= 0),
  constraint ck_stock_locked check (locked_qty >= 0),
  constraint ck_stock_transit check (in_transit_qty >= 0)
) engine=innodb default charset=utf8mb4 comment='库存';

create table if not exists t_stock_record (
  record_id                bigint(20)      not null auto_increment    comment '流水ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  product_id        bigint(20)      not null                   comment '商品ID',
  record_type       char(1)         not null                   comment '类型（1入库 2出库）',
  biz_type          varchar(20)     default ''                 comment '业务类型（采购入库/其他入库/领用出库）',
  qty               decimal(18,3)   not null                   comment '变动数量（正数）',
  before_qty        decimal(18,3)   not null                   comment '变动前库存',
  after_qty         decimal(18,3)   not null                   comment '变动后库存',
  source_no         varchar(64)     default null               comment '来源单号',
  batch_no          varchar(64)     default null               comment '批次号',
  operator_id       bigint(20)      default null               comment '操作人ID',
  operator_name     varchar(64)     default ''                 comment '操作人',
  remark            varchar(500)    default null               comment '备注',
  reverse_flag      char(1)         default '0'                comment '冲销标记（0未冲销 1已冲销）',
  reverse_record_id bigint(20)      default null               comment '冲销生成的流水ID',
  create_time       datetime                                   comment '创建时间',
  primary key (record_id),
  key idx_record_product (enterprise_id, product_id, create_time),
  key idx_record_source (source_no)
) engine=innodb default charset=utf8mb4 comment='出入库流水';

create table if not exists t_replenish_param (
  param_id          bigint(20)      not null auto_increment    comment '主键',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  product_id        bigint(20)      not null                   comment '商品ID',
  safety_stock      decimal(18,3)   not null default 0         comment '安全库存',
  max_stock         decimal(18,3)   not null default 0         comment '最大库存上限（0不限制）',
  cycle_days        int(4)          default 7                  comment '补货周期（天）',
  lead_days         int(4)          default 3                  comment '供应商交期（天）',
  season_coef       decimal(5,2)    default 1.00               comment '季节系数（0.80-1.50）',
  enable_flag       char(1)         default '1'                comment '是否参与计算（0否 1是）',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (param_id),
  unique key uk_param_product (enterprise_id, product_id)
) engine=innodb default charset=utf8mb4 comment='补货参数';

create table if not exists t_replenish_suggestion (
  suggestion_id                bigint(20)      not null auto_increment    comment '建议ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  product_id        bigint(20)      not null                   comment '商品ID',
  calc_date         date            not null                   comment '计算日期',
  suggest_qty       decimal(18,3)   not null default 0         comment '建议补货量',
  target_qty        decimal(18,3)   default 0                  comment '目标库存',
  daily_out_qty     decimal(18,3)   default 0                  comment '日均出库量',
  available_qty     decimal(18,3)   default 0                  comment '可用库存',
  in_transit_qty    decimal(18,3)   default 0                  comment '在途采购量',
  safety_stock      decimal(18,3)   default 0                  comment '安全库存快照',
  max_stock         decimal(18,3)   default 0                  comment '最大库存快照',
  season_coef       decimal(5,2)    default 1.00               comment '季节系数快照',
  status            char(1)         default '0'                comment '状态（0待处理 1已建单 2已忽略）',
  order_id          bigint(20)      default null               comment '生成的采购单ID',
  create_time       datetime                                   comment '创建时间',
  primary key (suggestion_id),
  unique key uk_suggest_day (enterprise_id, product_id, calc_date),
  key idx_suggest_status (enterprise_id, status, calc_date)
) engine=innodb default charset=utf8mb4 comment='补货建议';

-- ----------------------------------------------------------------------------
-- 4. 采购、审批与收货
-- ----------------------------------------------------------------------------
create table if not exists t_purchase_order (
  order_id          bigint(20)      not null auto_increment    comment '采购单ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  order_no          varchar(32)     not null                   comment '采购单号',
  supplier_id       bigint(20)      not null                   comment '供应商ID',
  supplier_name     varchar(100)    default ''                 comment '供应商名称快照',
  total_amount      decimal(18,2)   not null default 0.00      comment '采购总金额',
  status            char(1)         not null default '0'       comment '状态（0草稿 1待审批 2已通过 3已驳回 4已完成 5已取消）',
  version           int(4)          not null default 0         comment '版本号（驳回重提自增）',
  submit_by         varchar(64)     default ''                 comment '提交人',
  submit_time       datetime                                   comment '提交时间',
  approve_by        varchar(64)     default ''                 comment '审批人',
  approve_time      datetime                                   comment '审批时间',
  receive_by        varchar(64)     default ''                 comment '收货人',
  receive_time      datetime                                   comment '收货时间',
  cancel_reason     varchar(500)    default null               comment '取消原因',
  remark            varchar(500)    default null               comment '备注',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (order_id),
  unique key uk_order_no (enterprise_id, order_no),
  key idx_order_status (enterprise_id, status, create_time),
  key idx_order_supplier (supplier_id)
) engine=innodb default charset=utf8mb4 comment='采购单';

create table if not exists t_purchase_order_item (
  item_id                bigint(20)      not null auto_increment    comment '明细ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  order_id          bigint(20)      not null                   comment '采购单ID',
  product_id        bigint(20)      not null                   comment '商品ID',
  product_name      varchar(100)    default ''                 comment '商品名称快照',
  spec              varchar(100)    default ''                 comment '规格快照',
  unit              varchar(20)     default ''                 comment '单位快照',
  qty               decimal(18,3)   not null                   comment '采购数量',
  received_qty      decimal(18,3)   not null default 0         comment '已收货数量',
  price             decimal(18,2)   not null default 0.00      comment '单价',
  amount            decimal(18,2)   not null default 0.00      comment '金额',
  remark            varchar(255)    default null               comment '备注',
  primary key (item_id),
  key idx_item_order (order_id),
  constraint ck_item_qty check (qty > 0),
  constraint ck_item_received check (received_qty >= 0)
) engine=innodb default charset=utf8mb4 comment='采购明细';

create table if not exists t_purchase_approval (
  approval_id                bigint(20)      not null auto_increment    comment '审批ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  order_id          bigint(20)      not null                   comment '采购单ID',
  approver_id       bigint(20)      default null               comment '审批人ID',
  approver_name     varchar(64)     default ''                 comment '审批人',
  result            char(1)         not null                   comment '结果（1通过 2驳回）',
  opinion           varchar(500)    default null               comment '审批意见',
  approve_time      datetime                                   comment '审批时间',
  create_time       datetime                                   comment '创建时间',
  primary key (approval_id),
  key idx_approval_order (order_id)
) engine=innodb default charset=utf8mb4 comment='采购审批记录';

create table if not exists t_receipt (
  receipt_id                bigint(20)      not null auto_increment    comment '收货单ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  receipt_no        varchar(32)     not null                   comment '收货单号',
  order_id          bigint(20)      not null                   comment '采购单ID',
  receipt_type      char(1)         default '1'                comment '类型（1采购收货）',
  total_qty         decimal(18,3)   default 0                  comment '收货总数量',
  receiver_id       bigint(20)      default null               comment '收货人ID',
  receiver_name     varchar(64)     default ''                 comment '收货人',
  receipt_time      datetime                                   comment '收货时间',
  remark            varchar(500)    default null               comment '备注',
  create_time       datetime                                   comment '创建时间',
  primary key (receipt_id),
  unique key uk_receipt_no (enterprise_id, receipt_no),
  key idx_receipt_order (order_id)
) engine=innodb default charset=utf8mb4 comment='收货单';

create table if not exists t_receipt_item (
  item_id           bigint(20)      not null auto_increment    comment '主键',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  receipt_id        bigint(20)      not null                   comment '收货单ID',
  order_item_id     bigint(20)      not null                   comment '采购明细ID',
  product_id        bigint(20)      not null                   comment '商品ID',
  qty               decimal(18,3)   not null                   comment '收货数量',
  price             decimal(18,2)   default 0.00               comment '单价',
  amount            decimal(18,2)   default 0.00               comment '金额',
  primary key (item_id),
  key idx_receipt_item (receipt_id)
) engine=innodb default charset=utf8mb4 comment='收货明细';

-- ----------------------------------------------------------------------------
-- 5. 应付账款
-- ----------------------------------------------------------------------------
create table if not exists t_payable (
  payable_id                bigint(20)      not null auto_increment    comment '应付ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  payable_no        varchar(32)     not null                   comment '应付单号',
  order_id          bigint(20)      not null                   comment '采购单ID',
  supplier_id       bigint(20)      not null                   comment '供应商ID',
  total_amount      decimal(18,2)   not null default 0.00      comment '应付金额',
  paid_amount       decimal(18,2)   not null default 0.00      comment '已付金额',
  due_date          date                                       comment '到期日',
  status            char(1)         default '0'                comment '状态（0未付款 1部分付款 2已结清）',
  remark            varchar(500)    default null               comment '备注',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (payable_id),
  unique key uk_payable_no (enterprise_id, payable_no),
  unique key uk_payable_order (enterprise_id, order_id),
  key idx_payable_due (enterprise_id, status, due_date),
  constraint ck_payable_amount check (paid_amount >= 0 and paid_amount <= total_amount)
) engine=innodb default charset=utf8mb4 comment='应付账款';

create table if not exists t_payment_record (
  payment_id                bigint(20)      not null auto_increment    comment '付款ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  payable_id        bigint(20)      not null                   comment '应付ID',
  amount            decimal(18,2)   not null                   comment '付款金额',
  pay_method        varchar(20)     default ''                 comment '付款方式',
  pay_time          datetime                                   comment '付款时间',
  voucher_url       varchar(255)    default null               comment '凭证地址',
  operator_id       bigint(20)      default null               comment '经办人ID',
  operator_name     varchar(64)     default ''                 comment '经办人',
  remark            varchar(500)    default null               comment '备注',
  create_time       datetime                                   comment '创建时间',
  primary key (payment_id),
  key idx_payment_payable (payable_id),
  constraint ck_payment_amount check (amount > 0)
) engine=innodb default charset=utf8mb4 comment='付款记录';

-- ----------------------------------------------------------------------------
-- 6. 知识库与 AI
-- ----------------------------------------------------------------------------
create table if not exists t_kb_document (
  document_id                bigint(20)      not null auto_increment    comment '文档ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  title             varchar(200)    not null                   comment '文档标题',
  file_name         varchar(255)    default ''                 comment '原始文件名',
  file_path         varchar(500)    default ''                 comment '存储路径',
  file_type         varchar(20)     default ''                 comment '文件类型',
  file_size         bigint(20)      default 0                  comment '文件大小（字节）',
  doc_type          varchar(20)     default ''                 comment '文档分类（制度/供应商资料）',
  status            char(1)         default '0'                comment '状态（0处理中 1已完成 2失败）',
  chunk_count       int(4)          default 0                  comment '切片数量',
  error_msg         varchar(500)    default null               comment '失败原因',
  upload_by         varchar(64)     default ''                 comment '上传人',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (document_id),
  key idx_kb_ent (enterprise_id, status)
) engine=innodb default charset=utf8mb4 comment='知识库文档';

create table if not exists t_kb_chunk (
  chunk_id                bigint(20)      not null auto_increment    comment '片段ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  document_id       bigint(20)      not null                   comment '文档ID',
  chunk_index       int(4)          not null                   comment '片段序号',
  page_no           int(4)          default null               comment '页码',
  content           text                                       comment '片段内容',
  token_count       int(4)          default 0                  comment 'token 数',
  vector_ref        varchar(100)    default null               comment '向量库中的标识',
  create_time       datetime                                   comment '创建时间',
  primary key (chunk_id),
  key idx_chunk_doc (document_id)
) engine=innodb default charset=utf8mb4 comment='知识库片段';

create table if not exists t_ai_chat_session (
  session_id                bigint(20)      not null auto_increment    comment '会话ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  user_id           bigint(20)      not null                   comment '用户ID',
  title             varchar(100)    default ''                 comment '会话标题',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (session_id),
  key idx_session_user (enterprise_id, user_id, update_time)
) engine=innodb default charset=utf8mb4 comment='AI 会话';

create table if not exists t_ai_chat_message (
  message_id                bigint(20)      not null auto_increment    comment '消息ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  session_id        bigint(20)      not null                   comment '会话ID',
  role              varchar(16)     not null                   comment '角色（user/assistant/tool）',
  content           mediumtext                                 comment '消息内容',
  sources           json                                       comment '引用来源（文档、页码、片段）',
  model             varchar(64)     default ''                 comment '模型名称',
  token_usage       int(4)          default 0                  comment 'token 用量',
  cost_ms           int(8)          default 0                  comment '耗时（毫秒）',
  create_time       datetime                                   comment '创建时间',
  primary key (message_id),
  key idx_message_session (session_id, create_time)
) engine=innodb default charset=utf8mb4 comment='AI 消息';

create table if not exists t_ai_draft (
  draft_id                bigint(20)      not null auto_increment    comment '草稿ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  session_id        bigint(20)      default null               comment '来源会话',
  draft_no          varchar(32)     default ''                 comment '草稿编号',
  content           json                                       comment '草稿内容（明细、供应商、依据）',
  status            char(1)         default '0'                comment '状态（0待确认 1已确认 2已放弃 3已过期）',
  expire_time       datetime                                   comment '过期时间',
  order_id          bigint(20)      default null               comment '确认后生成的采购单ID',
  create_by         varchar(64)     default ''                 comment '创建人',
  create_time       datetime                                   comment '创建时间',
  primary key (draft_id),
  unique key uk_draft_no (enterprise_id, draft_no),
  key idx_draft_status (enterprise_id, status, expire_time)
) engine=innodb default charset=utf8mb4 comment='AI 采购草稿建议';

create table if not exists t_ai_tool_call_log (
  log_id                bigint(20)      not null auto_increment    comment '日志ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  user_id           bigint(20)      default null               comment '用户ID',
  session_id        bigint(20)      default null               comment '会话ID',
  trace_id          varchar(64)     default ''                 comment '链路ID',
  tool_name         varchar(64)     not null                   comment '工具名称',
  tool_args         text                                       comment '参数（脱敏）',
  result_summary    varchar(1000)   default null               comment '结果摘要',
  status            char(1)         default '0'                comment '状态（0成功 1失败）',
  error_code        varchar(64)     default null               comment '错误码',
  cost_ms           int(8)          default 0                  comment '耗时（毫秒）',
  token_usage       int(4)          default 0                  comment 'token 用量',
  create_time       datetime                                   comment '创建时间',
  primary key (log_id),
  key idx_tool_time (enterprise_id, create_time),
  key idx_tool_trace (trace_id)
) engine=innodb default charset=utf8mb4 comment='AI 工具调用审计';

-- ----------------------------------------------------------------------------
-- 7. 公共表
-- ----------------------------------------------------------------------------
create table if not exists t_idem_record (
  idem_id           bigint(20)      not null auto_increment    comment '主键',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  user_id           bigint(20)      default null               comment '用户ID',
  request_id        varchar(64)     not null                   comment '幂等键',
  api               varchar(100)    default ''                 comment '接口',
  status            char(1)         default '0'                comment '状态（0处理中 1成功 2失败）',
  result_summary    varchar(500)    default null               comment '结果摘要',
  create_time       datetime                                   comment '创建时间',
  primary key (idem_id),
  unique key uk_idem (enterprise_id, request_id),
  key idx_idem_time (create_time)
) engine=innodb default charset=utf8mb4 comment='幂等记录';

create table if not exists t_attachment (
  attachment_id                bigint(20)      not null auto_increment    comment '附件ID',
  enterprise_id     bigint(20)      not null                   comment '单位ID',
  biz_type          varchar(32)     default ''                 comment '业务类型',
  biz_id            bigint(20)      default null               comment '业务ID',
  file_name         varchar(255)    default ''                 comment '文件名',
  file_path         varchar(500)    default ''                 comment '存储路径',
  file_type         varchar(20)     default ''                 comment '文件类型',
  file_size         bigint(20)      default 0                  comment '文件大小',
  upload_by         varchar(64)     default ''                 comment '上传人',
  create_time       datetime                                   comment '创建时间',
  primary key (attachment_id),
  key idx_att_biz (biz_type, biz_id)
) engine=innodb default charset=utf8mb4 comment='业务附件';

-- ----------------------------------------------------------------------------
-- 8. 系统表扩展：用户归属单位
-- ----------------------------------------------------------------------------
drop procedure if exists sp_add_user_enterprise;
delimiter $$
create procedure sp_add_user_enterprise()
begin
  if not exists (select 1 from information_schema.columns
                 where table_schema = database() and table_name = 'sys_user' and column_name = 'enterprise_id') then
    alter table sys_user add column enterprise_id bigint(20) default null comment '所属单位ID' after dept_id;
    alter table sys_user add index idx_user_enterprise (enterprise_id);
  end if;
end$$
delimiter ;
call sp_add_user_enterprise();
drop procedure if exists sp_add_user_enterprise;

-- ----------------------------------------------------------------------------
-- 8.1 系统表扩展：库存流水冲销字段
-- ----------------------------------------------------------------------------
drop procedure if exists sp_add_stock_reverse;
delimiter $$
create procedure sp_add_stock_reverse()
begin
  if not exists (select 1 from information_schema.columns
                 where table_schema = database() and table_name = 't_stock_record' and column_name = 'reverse_flag') then
    alter table t_stock_record add column reverse_flag char(1) default '0' comment '冲销标记（0未冲销 1已冲销）' after remark;
    alter table t_stock_record add column reverse_record_id bigint(20) default null comment '冲销生成的流水ID' after reverse_flag;
  end if;
end$$
delimiter ;
call sp_add_stock_reverse();
drop procedure if exists sp_add_stock_reverse;

-- ----------------------------------------------------------------------------
-- 9. 初始化数据（可重复执行）
-- ----------------------------------------------------------------------------
insert into t_enterprise (id, name, short_name, pay_term_days, status, create_time)
select 1, '演示物资中心', '演示中心', 30, '0', now()
where not exists (select 1 from t_enterprise where id = 1);

-- 已有管理员归入默认单位，便于联调
update sys_user set enterprise_id = 1 where enterprise_id is null;

-- ----------------------------------------------------------------------------
-- 8.2 知识库扩展：片段向量列（RAG）
--     向量以 float32 + Base64 存储，4096 维约 22KB，比 JSON 省 3/4 空间
-- ----------------------------------------------------------------------------
drop procedure if exists sp_add_kb_embedding;
delimiter $$
create procedure sp_add_kb_embedding()
begin
  if not exists (select 1 from information_schema.columns
                 where table_schema = database() and table_name = 't_kb_chunk' and column_name = 'embedding') then
    alter table t_kb_chunk add column embedding mediumtext comment '向量（float32 Base64）' after vector_ref;
    alter table t_kb_chunk add column embedding_model varchar(64) default null comment '向量模型' after embedding;
    alter table t_kb_chunk add column dim int(6) default 0 comment '向量维度' after embedding_model;
  end if;
end$$
delimiter ;
call sp_add_kb_embedding();
drop procedure if exists sp_add_kb_embedding;

-- ----------------------------------------------------------------------------
-- 8.3 商品扩展：图片地址（物料图）
-- ----------------------------------------------------------------------------
drop procedure if exists sp_add_product_image;
delimiter $$
create procedure sp_add_product_image()
begin
  if not exists (select 1 from information_schema.columns
                 where table_schema = database() and table_name = 't_product' and column_name = 'image_url') then
    alter table t_product add column image_url varchar(255) default null comment '商品图片地址' after remark;
  end if;
end$$
delimiter ;
call sp_add_product_image();
drop procedure if exists sp_add_product_image;
