SET NAMES utf8mb4;
START TRANSACTION;

INSERT INTO system_dict_type (name,type,status,remark)
SELECT '资产分类','financial_asset_category',0,'家庭资产业务字典'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type='financial_asset_category' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 1,'流动资金','liquid','financial_asset_category',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_asset_category' AND value='liquid' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 2,'固定资产','fixed','financial_asset_category',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_asset_category' AND value='fixed' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 3,'投资理财','investment','financial_asset_category',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_asset_category' AND value='investment' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 4,'应收款','receivable','financial_asset_category',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_asset_category' AND value='receivable' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 5,'负债','liability','financial_asset_category',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_asset_category' AND value='liability' AND deleted=0);

INSERT INTO system_dict_type (name,type,status,remark)
SELECT '账户模板','financial_account_template',0,'家庭资产业务字典'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type='financial_account_template' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 1,'银行活期','bank','financial_account_template',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_account_template' AND value='bank' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 2,'银行卡','card','financial_account_template',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_account_template' AND value='card' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 3,'现金','cash','financial_account_template',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_account_template' AND value='cash' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 4,'支付宝','alipay','financial_account_template',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_account_template' AND value='alipay' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 5,'微信钱包','wechat','financial_account_template',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_account_template' AND value='wechat' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 6,'数字人民币','ecny','financial_account_template',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_account_template' AND value='ecny' AND deleted=0);

INSERT INTO system_dict_type (name,type,status,remark)
SELECT '资金用途','financial_fund_bucket',0,'家庭资产业务字典'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type='financial_fund_bucket' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 1,'活钱','liquid','financial_fund_bucket',0,'#3b82f6','1 年以内'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_fund_bucket' AND value='liquid' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 2,'稳健资金','stable','financial_fund_bucket',0,'#14b8a6','1～5 年'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_fund_bucket' AND value='stable' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 3,'长期资金','long_term','financial_fund_bucket',0,'#a78bfa','5 年以上'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_fund_bucket' AND value='long_term' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 4,'保险保障','insurance','financial_fund_bucket',0,'#f59e0b','保障期限'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_fund_bucket' AND value='insurance' AND deleted=0);

INSERT INTO system_dict_type (name,type,status,remark)
SELECT '保险类型','financial_insurance_type',0,'家庭资产业务字典'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type='financial_insurance_type' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 1,'意外','意外','financial_insurance_type',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_insurance_type' AND value='意外' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 2,'医疗','医疗','financial_insurance_type',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_insurance_type' AND value='医疗' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 3,'重疾','重疾','financial_insurance_type',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_insurance_type' AND value='重疾' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 4,'寿险','寿险','financial_insurance_type',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_insurance_type' AND value='寿险' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 5,'其他','其他','financial_insurance_type',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_insurance_type' AND value='其他' AND deleted=0);

INSERT INTO system_dict_type (name,type,status,remark)
SELECT '账户操作','financial_record_operation',0,'家庭资产业务字典'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type='financial_record_operation' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 1,'新增','create','financial_record_operation',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_record_operation' AND value='create' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 2,'修改','update','financial_record_operation',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_record_operation' AND value='update' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 3,'删除','delete','financial_record_operation',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_record_operation' AND value='delete' AND deleted=0);

INSERT INTO system_dict_type (name,type,status,remark)
SELECT '记录来源','financial_record_source',0,'家庭资产业务字典'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type='financial_record_source' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 1,'手动更新','manual','financial_record_source',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_record_source' AND value='manual' AND deleted=0);

INSERT INTO system_dict_data (sort,label,value,dict_type,status,css_class,remark)
SELECT 2,'Excel 导入','import','financial_record_source',0,'',''
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='financial_record_source' AND value='import' AND deleted=0);

COMMIT;
