SELECT A.dupa, A.y, B.z, B.w
    FROM TABLE_A A
    JOIN TABLE_B B ON A.dupa = B.y
    WHERE A.k = B.l AND A.i < 100;