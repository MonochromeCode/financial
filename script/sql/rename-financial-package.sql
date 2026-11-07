UPDATE QRTZ_JOB_DETAILS
SET JOB_CLASS_NAME = REPLACE(JOB_CLASS_NAME, 'cn.iocoder.yudao.', 'cn.iocoder.financial.')
WHERE JOB_CLASS_NAME LIKE 'cn.iocoder.yudao.%';
