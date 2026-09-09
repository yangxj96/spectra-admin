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

-- 旧自研调度数据不迁移；按外键依赖顺序删除旧表。
DROP TABLE IF EXISTS spectra_core.scheduler_operation_audit;
DROP TABLE IF EXISTS spectra_core.scheduler_loop_error;
DROP TABLE IF EXISTS spectra_core.scheduler_control_command;
DROP TABLE IF EXISTS spectra_core.scheduler_loop_runtime;
DROP TABLE IF EXISTS spectra_core.scheduler_execution;
DROP TABLE IF EXISTS spectra_core.scheduler_job;
