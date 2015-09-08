SELECT 
   p.Program_ID, m.Minute AS MOP, 
CASE  WHEN  COALESCE( sub01.expr01 ,0 ) > 2959
      THEN  COALESCE( sub01.expr02, 0 ) 
      ELSE  COALESCE( sub01.expr03 ,0 )
END AS ClockTime,  
CASE WHEN COALESCE( sub01.expr04 ,0)  > 2959  THEN 1 ELSE 0 END AS Nielson_Next_Day_Ind,
COALESCE(  sub01.expr05,0)  AS Gap_Num,
p.Broadcast_Date
FROM 
Ref_D30_Enhanced_Program_Descriptive p
JOIN (select idx as Minute from _v_vector_idx) m ON p.Reported_Duration >= m.Minute
left outer join (
   select sub02.Program_Code, sub02.Telecast_Number , sub02.Segment_ID, sub02.Minute , sub02.Reported_Start_Time,
        ( ( (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 + 
		   (((sub02.Reported_Start_Time + (sub02.Minute -1)) - (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 * 100) + coalesce(sum(gap_reported_duration),0))/60
		   ) * 100 + 
		(((sub02.Reported_Start_Time + (sub02.Minute -1)) - (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 * 100) + coalesce(sum(gap_reported_duration),0))%60
	 ) as expr01,
	 (6 + ((((sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 + 
	 (((sub02.Reported_Start_Time + (sub02.Minute -1)) - (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 * 100) + coalesce(sum(gap_reported_duration),0))/60) * 100 + 
	 (((sub02.Reported_Start_Time + (sub02.Minute -1)) - (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 * 100) + coalesce(sum(gap_reported_duration),0))%60) / 100 - 29) - 1 ) * 100 + 
	 ((((sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 + 
	 (((sub02.Reported_Start_Time + (sub02.Minute -1)) - (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 * 100) + coalesce(sum(gap_reported_duration),0))/60) * 100 + 
	 (((sub02.Reported_Start_Time + (sub02.Minute -1)) - (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 * 100) + coalesce(sum(gap_reported_duration),0))%60) - 
	 (((sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 + (((sub02.Reported_Start_Time + (sub02.Minute -1)) - (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 * 100) + 
	    coalesce(sum(gap_reported_duration),0))/60) * 100 + (((sub02.Reported_Start_Time + (sub02.Minute -1)) - (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 * 100) + 
		coalesce(sum(gap_reported_duration),0))%60) / 100* 100
	 ) as expr02,
     (((sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 + 
               (((sub02.Reported_Start_Time + (sub02.Minute -1)) - (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 * 100) + 
               coalesce(sum(gap_reported_duration),0))/60) * 100 + 
	           (((sub02.Reported_Start_Time + (sub02.Minute -1)) - 
	           (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 * 100) + 
	            coalesce(sum(gap_reported_duration),0))%60
     ) as expr03,
	 (
			   ((sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 + 
	           (((sub02.Reported_Start_Time + (sub02.Minute -1)) - (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 * 100) + 
	           coalesce(sum(gap_reported_duration),0))/60) * 100 + (((sub02.Reported_Start_Time + (sub02.Minute -1)) - 
	           (sub02.Reported_Start_Time + (sub02.Minute -1)) / 100 * 100) + 
	           coalesce(sum(gap_reported_duration),0))%60
	 ) as expr04,
	 count (*) as expr05
   from 
   ( select distinct p.Program_Code, p.Telecast_Number , p.Segment_ID, m.Minute , p.Reported_Start_Time 
       from Ref_D30_Enhanced_Program_Descriptive p
       inner join (select idx as Minute from _v_vector_idx) m ON p.Reported_Duration >= m.Minute
   )  sub02, REF_D35_Program_Gap_Record cgap
   where cgap.Program_Code = sub02.Program_Code 
	    AND cgap.Telecast_Number = sub02.Telecast_Number 
		AND cgap.Segment_ID = sub02.Segment_ID
		AND sub02.Minute + sub02.Reported_Start_Time > cgap.Gap_Reported_Start_Time
	group by 1,2,3,4,5
) sub01 on  p.Program_Code = sub01.Program_Code
	    AND p.Telecast_Number = sub01.Telecast_Number
		AND p.Segment_ID = sub01.Segment_ID
		AND m.Minute = sub01.Minute
		AND p.Reported_Start_Time = sub01.Reported_Start_Time	
WHERE p.ETL_Load_Number = isnull(1, p.ETL_Load_Number);



