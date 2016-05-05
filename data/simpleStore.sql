SELECT O.ID, P.PRICE
    FROM ORDERS O
    JOIN CLIENTS CL ON CL.ID = O.CLIENT_ID
    JOIN PRODUCTS P ON P.ID = O.PRODUCT_ID
    WHERE P.PRICE > 100 AND P.ID < 20 AND CL.ID >=0;