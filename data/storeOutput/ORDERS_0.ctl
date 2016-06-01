load data 
infile 'ORDERS_0.csv' "str '\n'"
truncate
into table ORDERS
fields terminated by ';'
OPTIONALLY ENCLOSED BY '"' AND '"'
trailing nullcols
( 
	ID CHAR(4000),
	PRODUCT_ID CHAR(4000),
	CLIENT_ID CHAR(4000),
	DATETIME CHAR(4000)
)
