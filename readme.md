# Test data generator for SQL queries

Java console application that generates data for specific SQL query. 
May be useful when there is a need to test a query and no real data is available.
Can generate data matching and not matching given query - 
the ratio of these numbers can be specified and we call it selectivity.

Besides the query itself, an XML description of database schema is necessary for data generation.

At current version, the generator supports limited set of SQL constructs. 



## Usage
This application uses Gradle build system. You can run this app from an IDE like IntelliJ IDEA or Eclipse - 
just start the Main class. You can also build the distribution with scripts to run the generator outside the IDE.
To build the distribution use following command (on Unix system):
````
./gradlew distZip
````
It will create ZIP achieve with compiled code and application start scripts - bash script in */bin/SqlDataGenerator* 
and bat one in */bin/SqlDataGenerator.bat* 

List of application parameters:

* –xmlFile: A path to XML file with database description. The structure of this file will be described below.
 This parameter is required.
* –sqlFile: A path to text file with the SQL query. This parameter is required.
* –output: A path to directory where the output files will be created. This parameter is required.
* –selectivity: The desired selectivity of output data. This parameter is optional, if not specified the value
                0.5 will be used.
* –properties: A path to properties file with additional configuration. If not specified the default configuration values will
               be used.
   
A sample command to run the generator on Linux system:
````            
./bin/SqlDataGenerator --xmlFile store/store.xml --sqlFile store/store.sql --output store/output --selectivity 0.67 --properties generator.properties
````

## Database description
This file describes database schema, must be provided in –xmlFile parameter. Must be valid against *src/main/resources/schema.xsd*.
It must contain a description of all the tables and attributes which are used in the SQL query. For every table, 
this file should contain its name, desired number of rows to be generated and a list of all assigned attributes. 
A description of every attribute should contain its name, type and some other restrictions, 
i.e. if this column is a primary key or should have values from a specific range.


##Example
In the */data/store* directory there is a sample files to present application capabilities. 
*store.xml* contains description of example database, *schema.sql* contains DDL commands to create this database. 
(it was tested on PostgreSQL 9.6). *store.sql* contains sample SQL query which can be used to generate test data.

Below there are more sample queries that can be used with this example.
```sql
SELECT *
FROM CLIENTS C
WHERE C.BIRTH_DATE BETWEEN '1960-01-01 00:00:00' AND '1969-12-31 23:59:59';
```

```sql
SELECT *
FROM PRODUCTS P
WHERE P.CATEGORY = 'AGD'
AND P.PACKAGE_WIDTH BETWEEN 50 AND 100
AND P.PACKAGE_DEPTH BETWEEN 50 AND 100
AND P.PACKAGE_HEIGHT BETWEEN 50 AND 100;
```

```sql
SELECT *
FROM ORDERS O
JOIN CLIENTS C ON C.ID = O.CLIENT_ID
WHERE O.DATETIME BETWEEN '2015-01-01 00:00:00' AND '2015-12-31 23:59:59'
AND O.STATE = 0
AND C.FIRST_NAME = 'John'
AND C.LAST_NAME = 'Doe';
```

```sql
SELECT *
FROM ORDERS O
JOIN CLIENTS C ON C.ID = O.CLIENT_ID
JOIN PRODUCTS P ON O.PRODUCT_ID = P.ID
WHERE NOT (C.FIRST_NAME = 'John'
AND (P.PRICE < 300 OR P.OLD_PRICE < 300)
AND P.CATEGORY <> 'RTV'
AND O.DATETIME BETWEEN '2013-04-01 00:00:00' AND '2013-05-01 00:00:00');
```

```sql
SELECT count(*)
FROM ORDERS O
JOIN PRODUCTS P ON O.PRODUCT_ID = P.ID
WHERE
(
(P.PRICE BETWEEN 100 AND 500 AND P.CATEGORY = 'RTV')
OR (P.PRICE BETWEEN 900 AND 2000 AND P.CATEGORY = 'AGD')
OR (P.PRICE BETWEEN 4000 AND 6000 AND P.CATEGORY = 'ELECTRONICS')
)
AND P.PACKAGE_WIDTH < 120
AND
(
O.DATETIME BETWEEN '2011-07-01 8:00:00' AND '2011-07-01 16:00:00'
OR O.DATETIME BETWEEN '2011-07-08 8:00:00' AND '2011-07-08 16:00:00'
);
```

```sql
SELECT *
FROM PRODUCTS P
WHERE P.PACKAGE_HEIGHT > 100
AND P.PACKAGE_WIDTH < 200
AND P.PACKAGE_HEIGHT < P.PACKAGE_WIDTH
AND P.PRICE > 999
AND P.OLD_PRICE > P.PRICE
```