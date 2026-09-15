-- 删除已废弃的自定义表单权限及其关联数据。
DELETE FROM spectra_security.sec_role_permission rp
USING spectra_security.sec_permission p
WHERE rp.permission_id = p.id
  AND p.code IN (
      'workflow:form:create',
      'workflow:form:read',
      'workflow:form:update',
      'workflow:form:disable'
  );

DELETE FROM spectra_security.sec_role_grantable_permission rgp
USING spectra_security.sec_permission p
WHERE rgp.permission_id = p.id
  AND p.code IN (
      'workflow:form:create',
      'workflow:form:read',
      'workflow:form:update',
      'workflow:form:disable'
  );

DELETE FROM spectra_security.sec_assignment_grant_boundary agb
USING spectra_security.sec_permission p
WHERE agb.permission_id = p.id
  AND p.code IN (
      'workflow:form:create',
      'workflow:form:read',
      'workflow:form:update',
      'workflow:form:disable'
  );

DELETE FROM spectra_security.sec_assignment_permission_boundary apb
USING spectra_security.sec_permission p
WHERE apb.permission_id = p.id
  AND p.code IN (
      'workflow:form:create',
      'workflow:form:read',
      'workflow:form:update',
      'workflow:form:disable'
  );

DELETE FROM spectra_security.sec_permission
WHERE code IN (
    'workflow:form:create',
    'workflow:form:read',
    'workflow:form:update',
    'workflow:form:disable'
);

DROP TABLE spectra_workflow.wf_form_version;
DROP TABLE spectra_workflow.wf_form_definition;

ALTER TABLE spectra_oa.oa_application_type
    DROP COLUMN form_definition_id;
