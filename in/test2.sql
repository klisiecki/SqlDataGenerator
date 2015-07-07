SELECT B.id, A.id, A.x, A.m, B.i, B.z, C.id
    FROM TABLE_A A
    JOIN TABLE_B B ON A.id = B.a_id
    JOIN TABLE_C C ON A.id = C.a_id
    WHERE A.n BETWEEN -50 AND 100
    AND A.m in (105,106,107)
    AND C.ci > 2000;