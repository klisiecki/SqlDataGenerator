SELECT A.id, A.x, A.m, B.i, B.z, C.id
    FROM TABLE_A A
    JOIN TABLE_B B ON A.id = B.a_id
    JOIN TABLE_C C ON A.id = C.a_id
    WHERE A.n > -100 AND A.n < 100;