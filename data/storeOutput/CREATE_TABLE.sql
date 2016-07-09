DROP TABLE PRODUCTS;
DROP TABLE CLIENTS;
DROP TABLE ORDERS;

CREATE TABLE PRODUCTS (
	ID NUMBER(4),
	PRICE NUMBER(6,2),
	CATEGORY VARCHAR2(50),
	DESCRIPTION VARCHAR2(50),
	NAME VARCHAR2(50)
);

CREATE TABLE CLIENTS (
	ID NUMBER(4),
	FIRST_NAME VARCHAR2(50),
	LAST_NAME VARCHAR2(50)
);

CREATE TABLE ORDERS (
	ID NUMBER(6),
	PRODUCT_ID NUMBER(4),
	CLIENT_ID NUMBER(4),
  DATETIME TIMESTAMP(6)
);


--INSERT INTO ORDERS VALUES(1,2,3,TO_DATE('2012-03-28 11:10:00','yyyy/mm/dd hh24:mi:ss'));
--
--INSERT INTO ORDERS VALUES(1,2,3,'13-01-03 10.13.18');