SELECT P.ID
    FROM PRODUCTS P
    WHERE P.PACKAGE_WIDTH < P.PACKAGE_HEIGHT
      AND P.PACKAGE_HEIGHT < P.PACKAGE_DEPTH
      AND P.PACKAGE_DEPTH < 3