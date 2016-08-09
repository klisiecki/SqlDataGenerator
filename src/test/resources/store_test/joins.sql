SELECT O.ID, CL.FIRST_NAME, CL.LAST_NAME, P.NAME, P.PRICE
    FROM ORDERS O
    JOIN CLIENTS CL ON CL.ID = O.CLIENT_ID
    JOIN PRODUCTS P ON P.ID = O.PRODUCT_ID
  WHERE CL.FIRST_NAME = 'John' AND P.PRICE > 100