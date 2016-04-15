SELECT   univBase.cust_id
FROM     campaign_tb.MA_CUSTKEY20121212 univBase
WHERE    EXISTS
         ( SELECT    ''
         FROM       EDW_MCF.JPBATES.SALES_TRANSACTION AS st
                    INNER JOIN EDW_MCF.JPBATES.location loc ON st.TRANS_LOCATION_ID = loc.LOCATION_ID
                    INNER JOIN EDW_MCF.JPBATES.HH_CUSTOMER_TXN_XREF ctx ON st.trans_id = ctx.trans_id
                    INNER JOIN EDW_MCF.JPBATES.HH_customer AS c ON ctx.hh_customer_id = c.hh_customer_id
         WHERE      st.TRAN_STATUS_CD             ='C'
         AND        void_ind                      ='N'
         AND        ctx.HH_CUSTOMER_ID            > 0
         AND        st.trans_booked_dt           >= '2006-01-01'
         AND        st.trans_booked_dt  IS NOT NULL
         AND        c.hh_customer_id              > 0
         AND        loc.concept_format_id IN (1)
         AND        c.hh_customer_id=univBase.cust_id   -- <<-- THIS is where it joins back to the outer query
         )