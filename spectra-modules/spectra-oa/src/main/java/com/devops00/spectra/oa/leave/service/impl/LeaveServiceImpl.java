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

package com.devops00.spectra.oa.leave.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.application.javabean.constant.ApplicationStatus;
import com.devops00.spectra.oa.application.javabean.entity.Application;
import com.devops00.spectra.oa.application.javabean.entity.ApplicationType;
import com.devops00.spectra.oa.application.mapper.ApplicationMapper;
import com.devops00.spectra.oa.application.mapper.ApplicationTypeMapper;
import com.devops00.spectra.oa.application.service.ApplicationService;
import com.devops00.spectra.oa.leave.javabean.converter.LeaveConverter;
import com.devops00.spectra.oa.leave.javabean.entity.AttendanceRecord;
import com.devops00.spectra.oa.leave.javabean.entity.LeaveApplication;
import com.devops00.spectra.oa.leave.javabean.entity.LeaveBalance;
import com.devops00.spectra.oa.leave.javabean.entity.LeaveType;
import com.devops00.spectra.oa.leave.javabean.from.LeaveCreateFrom;
import com.devops00.spectra.oa.leave.javabean.from.LeavePageFrom;
import com.devops00.spectra.oa.leave.javabean.from.LeaveSubmitFrom;
import com.devops00.spectra.oa.leave.javabean.vo.LeaveVO;
import com.devops00.spectra.oa.leave.mapper.AttendanceRecordMapper;
import com.devops00.spectra.oa.leave.mapper.LeaveApplicationMapper;
import com.devops00.spectra.oa.leave.mapper.LeaveBalanceMapper;
import com.devops00.spectra.oa.leave.mapper.LeaveTypeMapper;
import com.devops00.spectra.oa.leave.service.LeaveService;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.workflow.service.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 请假业务闭环服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl extends BaseServiceImpl<LeaveApplicationMapper, LeaveApplication> implements LeaveService {

    private static final String LEAVE_TYPE_CODE = "leave";
    private static final String ATTENDANCE_STATUS = "LEAVE";
    private static final String ATTENDANCE_SOURCE = "OA_LEAVE";

    private final LeaveTypeMapper leaveTypeMapper;
    private final LeaveBalanceMapper leaveBalanceMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final ApplicationTypeMapper applicationTypeMapper;
    private final ApplicationMapper applicationMapper;
    private final ApplicationService applicationService;
    private final ProcessInstanceService processInstanceService;
    private final NotificationGateway notificationGateway;
    private final LeaveConverter leaveConverter;
    private final TimeMapper timeMapper;

    @Override
    @Transactional
    public UUID create(LeaveCreateFrom from) {
        var parsed = parse(from);
        var type = requireLeaveType(parsed.leaveTypeCode());
        var application = applicationService.createDraft(LEAVE_TYPE_CODE, null, "请假申请 - " + type.getName());
        var detail = new LeaveApplication();
        detail.setApplicationId(application.getId());
        detail.setDepartmentId(application.getDepartmentId());
        detail.setLeaveTypeCode(parsed.leaveTypeCode());
        detail.setStartTime(parsed.startTime());
        detail.setEndTime(parsed.endTime());
        detail.setDurationHours(parsed.durationHours());
        detail.setReason(from.getReason());
        detail.setContactAddress(from.getContactAddress());
        if (!this.save(detail)) {
            throw new DataSaveException("保存请假明细失败");
        }
        applicationService.bindBizId(application.getId(), detail.getId());
        return detail.getId();
    }

    @Override
    @Transactional
    public void update(UUID id, LeaveCreateFrom from) {
        var detail = requireDetail(id);
        var application = requireApplicantApplication(detail);
        if (!(ApplicationStatus.DRAFT.name().equals(application.getStatus()) || ApplicationStatus.REJECTED.name().equals(application.getStatus()))) {
            throw new DataSaveException("当前状态不允许修改请假申请");
        }
        var parsed = parse(from);
        requireLeaveType(parsed.leaveTypeCode());
        detail.setLeaveTypeCode(parsed.leaveTypeCode());
        detail.setStartTime(parsed.startTime());
        detail.setEndTime(parsed.endTime());
        detail.setDurationHours(parsed.durationHours());
        detail.setReason(from.getReason());
        detail.setContactAddress(from.getContactAddress());
        if (!this.updateById(detail)) {
            throw new DataSaveException("更新请假明细失败");
        }
    }

    @Override
    public IPage<LeaveVO> page(PageFrom page, LeavePageFrom params) {
        var wrapper = new LambdaQueryWrapper<LeaveApplication>();
        var user = SecUtil.getCurrentUser();
        if (user == null || user.getId() == null || user.getDepartmentId() == null) {
            return new Page<>(page.getPageNum(), page.getPageSize(), 0);
        }
        var applicationWrapper = new LambdaQueryWrapper<Application>().eq(Application::getTypeCode, LEAVE_TYPE_CODE)
                .and(query -> query.eq(Application::getApplicantId, user.getId()).or().eq(Application::getDepartmentId, user.getDepartmentId()));
        if (params != null && StringUtils.hasText(params.getStatus())) {
            applicationWrapper.eq(Application::getStatus, params.getStatus());
        }
        var applicationIds = applicationMapper.selectList(applicationWrapper).stream().map(Application::getId).toList();
        if (applicationIds.isEmpty()) {
            return new Page<>(page.getPageNum(), page.getPageSize(), 0);
        }
        wrapper.in(LeaveApplication::getApplicationId, applicationIds);
        if (params != null && StringUtils.hasText(params.getLeaveTypeCode())) {
            wrapper.eq(LeaveApplication::getLeaveTypeCode, params.getLeaveTypeCode());
        }
        wrapper.orderByDesc(LeaveApplication::getCreatedAt);
        var result = this.page(page.toPage(), wrapper);
        var voPage = new Page<LeaveVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(
                result.getRecords().stream().map(detail -> assembleView(detail, applicationService.require(detail.getApplicationId()))).toList());
        return voPage;
    }

    @Override
    public LeaveVO get(UUID id) {
        var detail = requireDetail(id);
        return assembleView(detail, applicationService.requireVisible(detail.getApplicationId()));
    }

    @Override
    @Transactional
    public void submit(UUID id, LeaveSubmitFrom from) {
        var detail = requireDetail(id);
        var application = requireApplicantApplication(detail);
        applicationService.submit(application.getId());
        reserveBalance(application.getApplicantId(), detail);
        var type = applicationTypeMapper.selectOne(
                new LambdaQueryWrapper<ApplicationType>().eq(ApplicationType::getCode, LEAVE_TYPE_CODE).eq(ApplicationType::getEnabled, true));
        if (type == null || !StringUtils.hasText(type.getProcessDefinitionKey())) {
            throw new DataSaveException("请假流程尚未配置");
        }
        var currentUser = SecUtil.getCurrentUser();
        var applicant = currentUser == null ? null : currentUser.getUsername();
        if (!StringUtils.hasText(applicant)) {
            throw new DataSaveException("当前用户缺少流程用户名");
        }
        var approver = from == null ? null : from.getApproverUsername();
        var variables = new java.util.LinkedHashMap<String, Object>();
        variables.put("applicant", applicant);
        variables.put("approver", StringUtils.hasText(approver) ? approver : applicant);
        variables.put("applicantId", application.getApplicantId().toString());
        variables.put("leaveTypeCode", detail.getLeaveTypeCode());
        variables.put("durationHours", detail.getDurationHours().doubleValue());
        var processId = processInstanceService.start(type.getProcessDefinitionKey(), application.getId().toString(), variables);
        applicationService.bindProcessInstance(application.getId(), processId);
    }

    @Override
    @Transactional
    public void withdraw(UUID id) {
        var detail = requireDetail(id);
        var application = requireApplicantApplication(detail);
        releaseReservedBalance(application.getApplicantId(), detail);
        applicationService.withdraw(application.getId());
        terminateProcess(application);
    }

    @Override
    @Transactional
    public void cancel(UUID id) {
        var detail = requireDetail(id);
        var application = requireApplicantApplication(detail);
        if (ApplicationStatus.IN_REVIEW.name().equals(application.getStatus())) {
            releaseReservedBalance(application.getApplicantId(), detail);
        }
        applicationService.cancel(application.getId());
        terminateProcess(application);
    }

    @Override
    @Transactional
    public void onApproved(String businessKey, Map<String, Object> variables) {
        var application = requireApplication(businessKey);
        if (!ApplicationStatus.IN_REVIEW.name().equals(application.getStatus())) {
            return;
        }
        var detail = requireDetail(application.getBizId());
        applicationService.updateStatus(application.getId(), ApplicationStatus.APPROVED.name(), null);
        approveBalance(application.getApplicantId(), detail);
        createAttendanceRecords(application, detail);
        sendNotification(application, "请假申请已通过", "您的请假申请已审批通过");
    }

    @Override
    @Transactional
    public void onRejected(String businessKey, String reason) {
        var application = requireApplication(businessKey);
        if (!ApplicationStatus.IN_REVIEW.name().equals(application.getStatus())) {
            return;
        }
        var detail = requireDetail(application.getBizId());
        applicationService.updateStatus(application.getId(), ApplicationStatus.REJECTED.name(), reason);
        releaseReservedBalance(application.getApplicantId(), detail);
        sendNotification(application, "请假申请已驳回", StringUtils.hasText(reason) ? reason : "请假申请未通过审批");
    }

    @Override
    @Transactional
    public void onTerminated(String businessKey, String reason) {
        var application = requireApplication(businessKey);
        if (!ApplicationStatus.IN_REVIEW.name().equals(application.getStatus())) {
            return;
        }
        var detail = requireDetail(application.getBizId());
        applicationService.updateStatus(application.getId(), ApplicationStatus.CANCELLED.name(), reason);
        releaseReservedBalance(application.getApplicantId(), detail);
    }

    private LeaveApplication requireDetail(UUID id) {
        var detail = this.getById(id);
        if (detail == null) {
            throw new DataNotExistException("请假申请不存在: " + id);
        }
        return detail;
    }

    private Application requireApplicantApplication(LeaveApplication detail) {
        var application = applicationService.require(detail.getApplicationId());
        if (!Objects.equals(application.getApplicantId(), SecUtil.getCurrentUserId())) {
            throw new DataNotExistException("请假申请不存在或无权操作");
        }
        return application;
    }

    private Application requireApplication(String businessKey) {
        try {
            return applicationService.require(UUID.fromString(businessKey));
        } catch (IllegalArgumentException exception) {
            throw new DataNotExistException("审批业务KEY无效: " + businessKey);
        }
    }

    private LeaveType requireLeaveType(String code) {
        var type = leaveTypeMapper.selectOne(new LambdaQueryWrapper<LeaveType>().eq(LeaveType::getCode, code).eq(LeaveType::getEnabled, true));
        if (type == null) {
            throw new DataNotExistException("请假类型不存在或已停用: " + code);
        }
        return type;
    }

    private ParsedLeave parse(LeaveCreateFrom from) {
        try {
            var start = timeMapper.toInstant(from.getStartTime());
            var end = timeMapper.toInstant(from.getEndTime());
            if (!start.isBefore(end)) {
                throw new DataSaveException("请假开始时间必须早于结束时间");
            }
            var hours = calculateBusinessHours(start, end);
            if (hours.signum() <= 0) {
                throw new DataSaveException("请假时长必须大于 0 小时");
            }
            return new ParsedLeave(from.getLeaveTypeCode(), start, end, hours);
        } catch (java.time.format.DateTimeParseException exception) {
            throw new DataSaveException("请使用 ISO-8601 时间格式");
        }
    }

    private BigDecimal calculateBusinessHours(Instant start, Instant end) {
        var zoneId = timeMapper.getUserZoneId();
        var cursor = start.atZone(zoneId).toLocalDate();
        var last = end.atZone(zoneId).toLocalDate();
        long minutes = 0;
        while (!cursor.isAfter(last)) {
            var day = cursor.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                minutes += overlapMinutes(start, end, cursor, LocalTime.of(9, 0), LocalTime.NOON);
                minutes += overlapMinutes(start, end, cursor, LocalTime.of(13, 0), LocalTime.of(18, 0));
            }
            cursor = cursor.plusDays(1);
        }
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private long overlapMinutes(Instant start, Instant end, LocalDate date, LocalTime from, LocalTime to) {
        var zoneId = timeMapper.getUserZoneId();
        var windowStart = ZonedDateTime.of(date, from, zoneId).toInstant();
        var windowEnd = ZonedDateTime.of(date, to, zoneId).toInstant();
        var left = start.isAfter(windowStart) ? start : windowStart;
        var right = end.isBefore(windowEnd) ? end : windowEnd;
        return right.isAfter(left) ? Duration.between(left, right).toMinutes() : 0;
    }

    private LeaveVO assembleView(LeaveApplication detail, Application application) {
        var vo = leaveConverter.toVO(detail);
        vo.setApplicationNo(application.getApplicationNo());
        vo.setTitle(application.getTitle());
        vo.setStatus(application.getStatus());
        vo.setApplicantId(application.getApplicantId());
        vo.setProcessInstanceId(application.getProcessInstanceId());
        vo.setRejectReason(application.getRejectReason());
        return vo;
    }

    private void reserveBalance(UUID userId, LeaveApplication detail) {
        var balance = findBalance(userId, detail);
        if (balance == null) {
            return;
        }
        var available = balance.getTotalHours().subtract(balance.getUsedHours()).subtract(balance.getReservedHours());
        if (available.compareTo(detail.getDurationHours()) < 0) {
            throw new DataSaveException("请假额度不足");
        }
        balance.setReservedHours(balance.getReservedHours().add(detail.getDurationHours()));
        if (leaveBalanceMapper.updateById(balance) != 1) {
            throw new DataSaveException("预占请假额度失败");
        }
    }

    private void releaseReservedBalance(UUID userId, LeaveApplication detail) {
        var balance = findBalance(userId, detail);
        if (balance == null) {
            return;
        }
        balance.setReservedHours(balance.getReservedHours().subtract(detail.getDurationHours()).max(BigDecimal.ZERO));
        leaveBalanceMapper.updateById(balance);
    }

    private void approveBalance(UUID userId, LeaveApplication detail) {
        var balance = findBalance(userId, detail);
        if (balance == null) {
            return;
        }
        balance.setReservedHours(balance.getReservedHours().subtract(detail.getDurationHours()).max(BigDecimal.ZERO));
        balance.setUsedHours(balance.getUsedHours().add(detail.getDurationHours()));
        leaveBalanceMapper.updateById(balance);
    }

    private LeaveBalance findBalance(UUID userId, LeaveApplication detail) {
        return leaveBalanceMapper.selectOne(new LambdaQueryWrapper<LeaveBalance>().eq(LeaveBalance::getUserId, userId)
                .eq(LeaveBalance::getLeaveTypeCode, detail.getLeaveTypeCode())
                .eq(LeaveBalance::getYear, detail.getStartTime().atZone(timeMapper.getUserZoneId()).getYear()));
    }

    private void createAttendanceRecords(Application application, LeaveApplication detail) {
        var zoneId = timeMapper.getUserZoneId();
        var date = detail.getStartTime().atZone(zoneId).toLocalDate();
        var last = detail.getEndTime().atZone(zoneId).toLocalDate();
        while (!date.isAfter(last)) {
            var attendanceDate = date.atStartOfDay(zoneId).toInstant();
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY
                    && date.getDayOfWeek() != DayOfWeek.SUNDAY
                    && attendanceRecordMapper
                            .selectCount(new LambdaQueryWrapper<AttendanceRecord>().eq(AttendanceRecord::getApplicationId, application.getId())
                                    .eq(AttendanceRecord::getAttendanceDate, attendanceDate)) == 0) {
                var record = new AttendanceRecord();
                record.setApplicationId(application.getId());
                record.setUserId(application.getApplicantId());
                record.setAttendanceDate(attendanceDate);
                record.setStatus(ATTENDANCE_STATUS);
                record.setSource(ATTENDANCE_SOURCE);
                record.setDepartmentId(application.getDepartmentId());
                attendanceRecordMapper.insert(record);
            }
            date = date.plusDays(1);
        }
    }

    private void terminateProcess(Application application) {
        if (StringUtils.hasText(application.getProcessInstanceId())) {
            processInstanceService.terminate(application.getProcessInstanceId(), "OA 申请已撤回或取消");
        }
    }

    private void sendNotification(Application application, String title, String content) {
        notificationGateway.enqueue(NotificationRequest.inApp("oa:leave:" + application.getBizId() + ":" + title,
                NotificationPurpose.OA_NOTICE, List.of(application.getApplicantId()), "oa.application.status", title, content,
                "OA_LEAVE", application.getBizId().toString(), "OA", "/oa/leave/" + application.getBizId()));
    }

    /**
     * 解析后的请假时段和值班时长。
     */
    private record ParsedLeave(String leaveTypeCode, Instant startTime, Instant endTime, BigDecimal durationHours) {
    }
}
