SELECT O.ID, CL.FIRST_NAME, CL.LAST_NAME, P.NAME, P.PRICE
    FROM ORDERS O
    JOIN CLIENTS CL ON CL.ID = O.CLIENT_ID
    JOIN PRODUCTS P ON P.ID = O.PRODUCT_ID
    WHERE CL.LAST_NAME IN ('Cundiff', 'Mastroianni')
    	AND (P.CATEGORY='AGD' OR P.CATEGORY='RTV')
    	AND (P.PRICE BETWEEN 100 AND 600 OR P.PRICE is null);