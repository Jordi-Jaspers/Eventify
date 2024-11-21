insert into event (check_id, created, "timestamp", check_result, message)
SELECT ((random() * (select max(id) from shx_check))+1)::int,
        stamp,
       stamp,
       (ARRAY ['HARD_OK','SOFT_OK','HARD_UNKNOWN', 'SOFT_UNKNOWN', 'HARD_WARNING', 'SOFT_WARNING', 'HARD_CRITICAL', 'SOFT_CRITICAL' ])[round(random()*7)+1] as "check_result",
       'Test Event ' || stamp                                                                                                                                   as "message"
FROM generate_series('2023-08-01 01:00:00.000'::timestamp,
    NOW()::timestamp,
    '5 SECOND'::interval) as stamp
ON CONFLICT DO NOTHING;

INSERT INTO bia_group_check (bia_group_id, check_id, sort_id)
SELECT bg.id, checks.id, row_number() over (partition by bg.id)
FROM bia_group bg
         CROSS JOIN LATERAL (SELECT id FROM shx_check ORDER BY random() LIMIT 2000) checks
WHERE bg.id IN (57,58,59);

