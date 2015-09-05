SELECT A.id
    FROM TABLE_A A
    JOIN TABLE_B B on A.id = B.a_id
    WHERE A.n > 10 and A.n < 20 and A.n <>15 and A.n <> 17