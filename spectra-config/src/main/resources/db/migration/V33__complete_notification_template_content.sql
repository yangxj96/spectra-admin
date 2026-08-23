-- 修正内置 OA 模板正文，禁止正文仅透传一个业务参数。

UPDATE spectra_notification.ntf_template
SET content_template = CASE template_group_code
                           WHEN 'oa.meeting.invitation' THEN '您收到会议邀请：「{{meeting_title}}」。{{content}}'
                           WHEN 'oa.notice.published' THEN '公告《{{title}}》已发布，请及时查看。{{content}}'
                           WHEN 'oa.document.published' THEN '文档《{{document_title}}》已发布。摘要：{{summary}}'
                           WHEN 'oa.application.status' THEN '您的申请「{{title}}」状态已更新：{{content}}'
                       END
WHERE template_group_code IN (
          'oa.meeting.invitation',
          'oa.notice.published',
          'oa.document.published',
          'oa.application.status')
  AND version_no = 1
  AND state = 'PUBLISHED'
  AND created_by IS NULL
  AND content_template ~ '^\s*\{\{\s*[A-Za-z0-9_.-]+\s*}}\s*$';

UPDATE spectra_notification.ntf_template
SET version_digest = encode(
        digest(
            concat_ws(chr(31),
                coalesce(template_group_code, '<NULL>'),
                coalesce(channel, '<NULL>'),
                coalesce(purpose, '<NULL>'),
                coalesce(version_no::text, '<NULL>'),
                coalesce(title_template, '<NULL>'),
                coalesce(content_template, '<NULL>'),
                coalesce(html_template, '<NULL>'),
                coalesce(parameter_schema::text, '<NULL>'),
                coalesce(provider_template_code, '<NULL>')),
            'sha256'),
        'hex')
WHERE template_group_code IN (
          'oa.meeting.invitation',
          'oa.notice.published',
          'oa.document.published',
          'oa.application.status')
  AND version_no = 1
  AND state = 'PUBLISHED'
  AND created_by IS NULL;
