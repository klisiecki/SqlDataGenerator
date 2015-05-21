  
 SELECT count(*) counter_col FROM 
 (
  select  r.DATABASE as database_name , a.relname as table_name , b.attname as column_name , r.OBJID as OBJID, r."OWNER" as owner 
  from _t_class        a, 
       _t_attribute    b, 
	    (SELECT "o".OBJID, "o".OBJCLASS,  "u".USENAME AS "OWNER",   "o".OBJDB,   "d".OBJNAME AS "DATABASE"  
           FROM "_T_OBJECT" "o" 
		   JOIN "_T_OBJECT" "d" ON "o".OBJDB = "d".OBJID 
		   JOIN "_T_USER" "u"   ON "o".OBJOWNER = "u".USESYSID  
		   JOIN "_T_OBJECT_CLASSES" "c" ON "o".OBJCLASS = "c".OBJCLASS WHERE ((("o".OBJCLASS >= 4905) AND ("o".OBJCLASS <= 4940)) OR (("o".OBJCLASS = 4949) 
		        OR ((("o".OBJCLASS >= 4951) AND ("o".OBJCLASS <= 4955)) OR (("o".OBJCLASS = 4959) 
			    OR ((("o".OBJCLASS >= 4961) AND ("o".OBJCLASS <= 4963)) OR ("o".OBJCLASS = 4993)))))) 
		) r 
  where a.oid = b.attrelid and lower(a.relname) not like '\_t\_%' and r.OBJID = a.OID 
    and upper (b.attname ) not in ('_EXTENTID','_PAGEID', 'DELETEXID' , 'CREATEXID' , 'DATASLICEID' , 'ROWID' ) 
    and  (((((r.OBJCLASS = 4905) OR (r.OBJCLASS = 4911)) OR ((r.OBJCLASS = 4920) OR (r.OBJCLASS = 4926))) OR (((r.OBJCLASS = 4930) OR (r.OBJCLASS = 4940)) 
     OR ((r.OBJCLASS = 4951) OR (r.OBJCLASS = 4953)))) OR (((r.OBJCLASS = 4959) OR (r.OBJCLASS = 4961)) OR ((r.OBJCLASS = 4962) OR (r.OBJCLASS = 4963)))) 
  order by r.DATABASE , 2^32*a.relrefs + case when a.reltuples < 0 then 2^32 + a.reltuples else a.reltuples end desc, a.relname , b.attnum
 ) all_tab_cols	
 WHERE upper(owner)      = upper('dbowner') 
   AND upper(table_name) = upper('table_name');
