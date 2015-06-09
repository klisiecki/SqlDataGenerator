SELECT A.id, A.x, B.z
    FROM TABLE_A A
    JOIN TABLE_B B ON A.id = B.a_id
    JOIN TABLE_C C ON A.id = C.a_id
    WHERE A.m = B.i and A.n > -100 AND A.n < 100;