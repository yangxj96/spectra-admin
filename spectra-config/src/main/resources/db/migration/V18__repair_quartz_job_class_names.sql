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

-- Quartz 包结构扁平化后，修复已持久化的内置执行历史清理 Job 类名。
UPDATE spectra_quartz.QRTZ_JOB_DETAILS
SET JOB_CLASS_NAME = 'com.devops00.spectra.core.quartz.job.QuartzExecutionHistoryCleanupJob'
WHERE JOB_NAME = 'system.scheduler.execution-history-cleanup'
  AND JOB_GROUP = 'SPECTRA_BUILTIN'
  AND JOB_CLASS_NAME = 'com.devops00.spectra.core.scheduler.quartz.job.QuartzExecutionHistoryCleanupJob';
