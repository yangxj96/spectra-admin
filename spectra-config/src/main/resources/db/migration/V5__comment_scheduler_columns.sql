/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

-- 为调度表补充完整的数据字典注释，不改变已执行的 V4 表结构。

COMMENT ON TABLE spectra_core.scheduler_job IS '统一调度任务定义表；deleted 仅用于 BaseEntity 兼容，归档使用 definition_status';
COMMENT ON COLUMN spectra_core.scheduler_job.id IS '任务定义主键';
COMMENT ON COLUMN spectra_core.scheduler_job.job_key IS '任务稳定键；用于代码注册处理器和管理操作定位';
COMMENT ON COLUMN spectra_core.scheduler_job.name IS '任务显示名称';
COMMENT ON COLUMN spectra_core.scheduler_job.module IS '任务所属业务模块标识';
COMMENT ON COLUMN spectra_core.scheduler_job.description IS '任务用途和运维说明';
COMMENT ON COLUMN spectra_core.scheduler_job.handler_key IS '代码注册处理器键；不接受数据库动态类名或方法名';
COMMENT ON COLUMN spectra_core.scheduler_job.job_type IS '任务类型：OPS 运维、SYSTEM 系统、LOOP 高频循环';
COMMENT ON COLUMN spectra_core.scheduler_job.run_scope IS '运行范围：PER_INSTANCE 每实例运行、SINGLETON 单实例运行';
COMMENT ON COLUMN spectra_core.scheduler_job.definition_status IS '定义状态：REGISTERED 已注册、UNAVAILABLE 不可用、ARCHIVED 已归档';
COMMENT ON COLUMN spectra_core.scheduler_job.desired_state IS '期望状态；离散任务为 ENABLED/DISABLED，循环任务为 RUNNING/DRAINING/STOPPED';
COMMENT ON COLUMN spectra_core.scheduler_job.schedule_kind IS '调度方式：CRON、FIXED_DELAY 或 MANUAL';
COMMENT ON COLUMN spectra_core.scheduler_job.cron_expression IS 'Cron 表达式；仅 CRON 调度方式使用';
COMMENT ON COLUMN spectra_core.scheduler_job.fixed_delay_ms IS '固定延迟毫秒数；仅 FIXED_DELAY 调度方式使用';
COMMENT ON COLUMN spectra_core.scheduler_job.initial_delay_ms IS '首次触发前的初始延迟毫秒数';
COMMENT ON COLUMN spectra_core.scheduler_job.next_fire_at IS '按系统时区计算并保存的下一次计划触发时间';
COMMENT ON COLUMN spectra_core.scheduler_job.misfire_policy IS '错过计划的处理策略：SKIP、FIRE_ONCE 或 CATCH_UP_LIMITED';
COMMENT ON COLUMN spectra_core.scheduler_job.concurrency_policy IS '并发策略：FORBID、ALLOW 或 REPLACE';
COMMENT ON COLUMN spectra_core.scheduler_job.execution_policy IS '超时、租约、重试、心跳和排空策略；只允许非敏感配置或密钥引用';
COMMENT ON COLUMN spectra_core.scheduler_job.parameters IS '任务参数；只允许参数值或密钥引用';
COMMENT ON COLUMN spectra_core.scheduler_job.revision IS '任务定义修订号；执行记录保存创建时的修订快照';
COMMENT ON COLUMN spectra_core.scheduler_job.created_by IS '创建人主键；系统初始化任务可为空';
COMMENT ON COLUMN spectra_core.scheduler_job.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.scheduler_job.updated_by IS '最后修改人主键；系统内部变更可为空';
COMMENT ON COLUMN spectra_core.scheduler_job.updated_at IS '最后修改时间';
COMMENT ON COLUMN spectra_core.scheduler_job.deleted IS 'BaseEntity 兼容字段；调度业务不读取、不写入，归档使用 definition_status';
COMMENT ON COLUMN spectra_core.scheduler_job.version IS '乐观锁版本号';

COMMENT ON TABLE spectra_core.scheduler_execution IS '统一调度离散执行记录表；UNKNOWN 原状态不可覆盖';
COMMENT ON COLUMN spectra_core.scheduler_execution.id IS '离散执行记录主键';
COMMENT ON COLUMN spectra_core.scheduler_execution.job_id IS '关联的任务定义主键';
COMMENT ON COLUMN spectra_core.scheduler_execution.fire_key IS '本次计划触发的全局幂等键';
COMMENT ON COLUMN spectra_core.scheduler_execution.trigger_type IS '触发来源：SCHEDULE 计划、MANUAL 手工、RETRY 重试';
COMMENT ON COLUMN spectra_core.scheduler_execution.status IS '执行状态：QUEUED、RUNNING、RETRY_WAIT、SUCCEEDED、FAILED、UNKNOWN、SKIPPED 或 CANCELLED';
COMMENT ON COLUMN spectra_core.scheduler_execution.job_revision IS '创建执行时的任务定义修订号快照';
COMMENT ON COLUMN spectra_core.scheduler_execution.handler_version IS '创建执行时的处理器版本快照';
COMMENT ON COLUMN spectra_core.scheduler_execution.schedule_kind_snapshot IS '创建执行时的调度方式快照';
COMMENT ON COLUMN spectra_core.scheduler_execution.schedule_expression_snapshot IS '创建执行时的 Cron 或固定延迟表达式快照';
COMMENT ON COLUMN spectra_core.scheduler_execution.parameters_snapshot IS '创建执行时的任务参数快照；不保存当前用户、租户或隐式请求上下文';
COMMENT ON COLUMN spectra_core.scheduler_execution.effect_type IS '外部副作用确认类型：DB_ONLY、OUTBOX、EXTERNAL_IDEMPOTENT 或 EXTERNAL_UNKNOWN';
COMMENT ON COLUMN spectra_core.scheduler_execution.scheduled_at IS '计划触发时间';
COMMENT ON COLUMN spectra_core.scheduler_execution.queued_at IS '进入执行队列时间';
COMMENT ON COLUMN spectra_core.scheduler_execution.started_at IS '处理器开始执行时间';
COMMENT ON COLUMN spectra_core.scheduler_execution.finished_at IS '执行结束时间';
COMMENT ON COLUMN spectra_core.scheduler_execution.next_retry_at IS '允许重试时的下一次重试时间';
COMMENT ON COLUMN spectra_core.scheduler_execution.deadline_at IS '本次执行的处理截止时间';
COMMENT ON COLUMN spectra_core.scheduler_execution.attempt_no IS '当前执行尝试序号，从 1 开始';
COMMENT ON COLUMN spectra_core.scheduler_execution.max_attempts IS '本次执行允许的最大尝试次数';
COMMENT ON COLUMN spectra_core.scheduler_execution.locked_by IS '当前租约持有实例标识';
COMMENT ON COLUMN spectra_core.scheduler_execution.locked_at IS '当前租约加锁时间';
COMMENT ON COLUMN spectra_core.scheduler_execution.lease_expires_at IS '当前执行租约过期时间';
COMMENT ON COLUMN spectra_core.scheduler_execution.last_heartbeat_at IS '处理器最近一次心跳时间';
COMMENT ON COLUMN spectra_core.scheduler_execution.last_error_code IS '最近一次错误编码';
COMMENT ON COLUMN spectra_core.scheduler_execution.last_error_message IS '最近一次脱敏错误信息';
COMMENT ON COLUMN spectra_core.scheduler_execution.result_summary IS '执行结果摘要；不保存敏感载荷';
COMMENT ON COLUMN spectra_core.scheduler_execution.original_execution_id IS '重试或派生执行关联的原始执行记录主键';
COMMENT ON COLUMN spectra_core.scheduler_execution.resolution_status IS 'UNKNOWN 结果解决状态：UNRESOLVED、CONFIRMED_SUCCESS、CONFIRMED_FAILED 或 RETRIED';
COMMENT ON COLUMN spectra_core.scheduler_execution.resolution_reason IS '解决 UNKNOWN 结果或执行状态的操作原因';
COMMENT ON COLUMN spectra_core.scheduler_execution.resolved_by IS '解决人主键';
COMMENT ON COLUMN spectra_core.scheduler_execution.resolved_at IS '解决时间';
COMMENT ON COLUMN spectra_core.scheduler_execution.created_by IS '创建人主键；系统调度执行可为空';
COMMENT ON COLUMN spectra_core.scheduler_execution.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.scheduler_execution.updated_by IS '最后修改人主键；系统内部变更可为空';
COMMENT ON COLUMN spectra_core.scheduler_execution.updated_at IS '最后修改时间';
COMMENT ON COLUMN spectra_core.scheduler_execution.deleted IS 'BaseEntity 兼容字段；执行记录不通过该字段归档或过滤';
COMMENT ON COLUMN spectra_core.scheduler_execution.version IS '乐观锁版本号；租约和结果回写使用版本 CAS';

COMMENT ON TABLE spectra_core.scheduler_loop_runtime IS '高频循环运行会话表；每个会话跨多个周期，不为每周期创建执行记录';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.id IS '循环运行会话主键';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.job_id IS '关联的 LOOP 任务定义主键';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.session_key IS '循环会话幂等键';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.instance_id IS '运行该会话的应用实例标识';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.status IS '会话状态：STARTING、RUNNING、DEGRADED、DRAINING、STOPPED、CRASHED 或 UNKNOWN';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.started_at IS '会话启动时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.stopped_at IS '会话停止时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.last_heartbeat_at IS '会话最近一次心跳时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.lease_expires_at IS '会话租约过期时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.last_cycle_at IS '最近一次循环周期完成或开始时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.last_progress_at IS '最近一次取得有效业务进展的时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.drain_deadline_at IS '排空停止的最晚完成时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.total_cycles IS '累计循环周期数';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.total_processed IS '累计处理成功数量';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.total_failed IS '累计处理失败数量';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.consecutive_error_count IS '连续错误周期数';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.last_error_code IS '最近一次循环错误编码';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.last_error_message IS '最近一次脱敏循环错误信息';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.state_reason IS '最近一次会话状态变更原因';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.created_by IS '创建人主键；系统运行会话可为空';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.updated_by IS '最后修改人主键；系统内部变更可为空';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.updated_at IS '最后修改时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.deleted IS 'BaseEntity 兼容字段；循环会话生命周期不使用该字段';
COMMENT ON COLUMN spectra_core.scheduler_loop_runtime.version IS '乐观锁版本号；循环控制使用版本 CAS';

COMMENT ON TABLE spectra_core.scheduler_control_command IS '高频循环控制命令表；命令先持久化再应用';
COMMENT ON COLUMN spectra_core.scheduler_control_command.id IS '控制命令主键';
COMMENT ON COLUMN spectra_core.scheduler_control_command.job_id IS '关联的 LOOP 任务定义主键';
COMMENT ON COLUMN spectra_core.scheduler_control_command.target_runtime_id IS '目标循环会话主键；按指定会话控制时使用';
COMMENT ON COLUMN spectra_core.scheduler_control_command.target_session_key IS '目标循环会话幂等键；按会话键控制时使用';
COMMENT ON COLUMN spectra_core.scheduler_control_command.expected_runtime_version IS '提交命令时预期的循环会话版本号，用于防止过期控制';
COMMENT ON COLUMN spectra_core.scheduler_control_command.command_type IS '控制动作：START、DRAIN_STOP、RESTART、FORCE_STOP 或 FORCE_RECLAIM';
COMMENT ON COLUMN spectra_core.scheduler_control_command.status IS '命令状态：REQUESTED、APPLYING、APPLIED、FAILED 或 TIMEOUT';
COMMENT ON COLUMN spectra_core.scheduler_control_command.idempotency_key IS '控制命令幂等键';
COMMENT ON COLUMN spectra_core.scheduler_control_command.reason IS '提交控制命令的操作原因';
COMMENT ON COLUMN spectra_core.scheduler_control_command.requested_by IS '命令申请人主键';
COMMENT ON COLUMN spectra_core.scheduler_control_command.requested_at IS '命令申请时间';
COMMENT ON COLUMN spectra_core.scheduler_control_command.deadline_at IS '命令处理截止时间';
COMMENT ON COLUMN spectra_core.scheduler_control_command.applied_at IS '命令开始应用时间';
COMMENT ON COLUMN spectra_core.scheduler_control_command.finished_at IS '命令处理结束时间';
COMMENT ON COLUMN spectra_core.scheduler_control_command.result_code IS '命令处理结果编码';
COMMENT ON COLUMN spectra_core.scheduler_control_command.result_message IS '命令处理结果说明';
COMMENT ON COLUMN spectra_core.scheduler_control_command.created_by IS '创建人主键；系统内部命令可为空';
COMMENT ON COLUMN spectra_core.scheduler_control_command.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.scheduler_control_command.updated_by IS '最后修改人主键；系统内部变更可为空';
COMMENT ON COLUMN spectra_core.scheduler_control_command.updated_at IS '最后修改时间';
COMMENT ON COLUMN spectra_core.scheduler_control_command.deleted IS 'BaseEntity 兼容字段；控制命令不使用该字段归档';
COMMENT ON COLUMN spectra_core.scheduler_control_command.version IS '乐观锁版本号；命令消费使用版本 CAS';

COMMENT ON TABLE spectra_core.scheduler_loop_error IS '高频循环错误聚合表；重复错误通过计数和限流日志收敛';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.id IS '循环错误聚合记录主键';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.job_id IS '关联的 LOOP 任务定义主键';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.instance_id IS '产生错误的应用实例标识';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.runtime_id IS '关联的循环运行会话主键';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.error_fingerprint IS '脱敏错误指纹；用于聚合重复错误';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.error_code IS '错误编码';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.error_message IS '脱敏错误信息';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.status IS '错误状态：OPEN 或 RESOLVED';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.first_seen_at IS '首次发现该错误的时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.last_seen_at IS '最近一次发现该错误的时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.last_logged_at IS '最近一次按限流规则输出该错误日志的时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.occurrence_count IS '错误累计出现次数';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.suppressed_count IS '因日志限流而抑制的错误次数';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.last_context IS '最近一次脱敏错误上下文摘要';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.resolved_by IS '错误解决人主键';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.resolved_at IS '错误解决时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.resolution_reason IS '错误解决原因';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.created_by IS '创建人主键；系统聚合记录可为空';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.updated_by IS '最后修改人主键；系统内部变更可为空';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.updated_at IS '最后修改时间';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.deleted IS 'BaseEntity 兼容字段；错误聚合生命周期不使用该字段';
COMMENT ON COLUMN spectra_core.scheduler_loop_error.version IS '乐观锁版本号';
