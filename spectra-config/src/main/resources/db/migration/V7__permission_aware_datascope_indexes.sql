-- Phase 6: indexes used by Permission-aware Access Boundary predicates.
-- All predicates are server-generated; these indexes only support filtering and
-- do not grant access by themselves.
CREATE INDEX IF NOT EXISTS idx_oa_asset_scope_department
    ON spectra_oa.oa_asset (department_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_calendar_scope_owner_department
    ON spectra_oa.oa_calendar (owner_id, department_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_contract_scope_department_owner
    ON spectra_oa.oa_contract (department_id, owner_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_document_scope_department_owner
    ON spectra_oa.oa_document (department_id, owner_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_document_folder_scope_department
    ON spectra_oa.oa_document_folder (department_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_meeting_scope_department
    ON spectra_oa.oa_meeting (department_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_meeting_participant_scope
    ON spectra_oa.oa_meeting_participant (department_id, user_id, meeting_id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_meeting_record_scope_department
    ON spectra_oa.oa_meeting_record (department_id, meeting_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_notice_scope_department
    ON spectra_oa.oa_notice (department_id, target_department_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_application_scope_department
    ON spectra_oa.oa_application (department_id, applicant_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_leave_application_scope_department
    ON spectra_oa.oa_leave_application (department_id, created_by, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_leave_balance_scope
    ON spectra_oa.oa_leave_balance (department_id, user_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_attendance_record_scope
    ON spectra_oa.oa_attendance_record (department_id, user_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_supply_item_scope_department
    ON spectra_oa.oa_supply_item (department_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_purchase_scope_department
    ON spectra_oa.oa_purchase (department_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_purchase_item_scope_department
    ON spectra_oa.oa_purchase_item (department_id, purchase_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_purchase_receipt_scope_purchase
    ON spectra_oa.oa_purchase_receipt (purchase_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_purchase_receipt_item_scope_purchase_item
    ON spectra_oa.oa_purchase_receipt_item (purchase_item_id, receipt_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_reimbursement_scope_department
    ON spectra_oa.oa_reimbursement (department_id, id) WHERE deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_oa_reimbursement_item_scope_department
    ON spectra_oa.oa_reimbursement_item (department_id, reimbursement_id, id) WHERE deleted IS NULL;
