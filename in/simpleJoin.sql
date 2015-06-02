SELECT A.x, A.y, B.x, B.y
    FROM TABLE_A A
    JOIN TABLE_A A2
    JOIN TABLE_B B ON A.x = B.y;