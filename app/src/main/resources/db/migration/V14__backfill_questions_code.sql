UPDATE questions
SET code = 'AWSCertPract_' || UPPER(SUBSTR(md5(
                                                   exam_id::text || '|' ||
                                                   lower(trim(text)) || '|' ||
                                                   lower(trim(domain)) || '|' ||
                                                   lower(trim(difficulty))
                                           ), 1, 6))
WHERE code IS NULL;