load data 
infile 'CLIENTS_0.csv' "str '\n'"
truncate
into table CLIENTS
fields terminated by ';'
OPTIONALLY ENCLOSED BY '"' AND '"'
trailing nullcols
( 	
	ID CHAR(4000),
	FIRST_NAME CHAR(4000),
	LAST_NAME CHAR(4000)
)
