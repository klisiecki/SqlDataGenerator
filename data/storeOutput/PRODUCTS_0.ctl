load data 
infile 'PRODUCTS_0.csv' "str '\n'"
truncate
into table PRODUCTS
fields terminated by ';'
OPTIONALLY ENCLOSED BY '"' AND '"'
trailing nullcols
( 	
	ID CHAR(4000),
	NAME CHAR(4000),
	DESCRIPTION CHAR(4000),
	PRICE CHAR(4000),
	CATEGORY CHAR(4000)
)
