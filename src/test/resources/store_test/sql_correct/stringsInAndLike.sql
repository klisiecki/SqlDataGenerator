SELECT CL.FIRST_NAME, CL.LAST_NAME
    FROM CLIENTS CL
    WHERE CL.FIRST_NAME LIKE '_A__' AND CL.FIRST_NAME IN ('AAA', 'AAAA', 'BBBB', 'XAXX', 'AZAA', 'AABB');
