SELECT
    TRIM(TO_CHAR(CBH.FSCL_YR, '9999')) AS FSCL_YR,
    CBH.CLNT_CDE AS BOARDCODE, BRD.CLNT_NME AS BOARDNAME, CBH.BTCH_NBR AS BTCH_NBR, CSH.CSH_CDE_NME AS REMIT_TYPE, CBH.BTCH_RCPT_DTE AS RCPT_DT, CR.CSH_RCPT_ID AS CSH_RCPT_ID, CBH.INIT_RCPT_NBR AS iNIT_RCPT_NBR, CBH.END_RCPT_NBR AS END_RCPT_NBR, CAL.ALLC_AMT,
    CBH.RCPT_CNT AS RCPT_CNT, CBH.RCPT_CNT_CTRL AS RCPT_CNT_CTRL, CBH.REMT_AMT_TOT AS REMT_AMT_TOT, CBH.REMT_AMT_CTRL AS REMT_AMT_CTRL, CR.REMT_AMT AS RCPT_REMT_AMT, CR.DLN AS DLN,
    CR.CMNT AS PAYOR_CMTS,
    CR.VALD_NBR AS RCPT_NO,
    CRP.REC_TYP AS REC_TYP, CBH.CHG_OPER_ID AS CHNG_USER_ID,
    CBH.CHG_OPER_ID AS CHNG_USER_ID,
    CBH.CHG_TME_STMP AS CHNG_USER_DT,
    CAL.ALLC_TYP AS ALL_TYP,
    CASE WHEN CAL.ALLC_TYP = 'A'        THEN  ALLC_TYP_A.XACT_DESC
         WHEN CAL.ALLC_TYP = 'M'        THEN ALLC_TYP_M.CHRG_DESC
         WHEN CAL.ALLC_TYP IS NULL      THEN 'AMOUNT NOT ALLOCATED'
    END AS XDESC,
    CASE WHEN CAL.ALLC_TYP = 'A'        THEN ALLC_TYP_A.XACT_CDE
         WHEN CAL.ALLC_TYP = 'M'        THEN ALLC_TYP_M.CHRG_CDE
         WHEN CAL.ALLC_TYP IS NULL      THEN 'XX'
        END AS XCODE,
    CASE WHEN CAL.ALLC_TYP = 'P' THEN ALLC_TYP_P.CSH_CDE     END AS RFND_CDE,
    CASE WHEN CAL.ALLC_TYP = 'P' THEN ALLC_TYP_P.CSH_CDE_NME END AS RFND_DESC,
    REMIT_INFO.REMIT_NAME AS REMIT_NAME,
    REMIT_INFO.REMIT_ENTITY_ID AS REMIT_ENTITY_ID,
    BENE_INFO.BENE_NAME AS BENEF_NAME,
    BENE_INFO.BENE_ENTITY_ID
FROM
    CSH_BTCH_HDR CBH
    inner join CSH_RCPT CR on CBH.FSCL_YR = CR.FSCL_YR AND CBH.BTCH_ID = CR.BTCH_ID
    inner join CLNT BRD on CR.CLNT_CDE = BRD.CLNT_CDE
    inner join CSH_RCPT_PARTY CRP on CRP.CSH_RCPT_ID = CR.CSH_RCPT_ID
    inner join CSH_CDE CSH on CSH.CSH_CDE_ID = CR.CSH_CDE_ID_REMT
    inner join (
        SELECT
            r.csh_rcpt_id CSH_RCPT_ID,
            n.xent_id REMIT_ENTITY_ID,
            CASE WHEN N.SURNME IS NOT NULL AND N.FRST_NME IS NOT NULL
                    THEN N.SURNME ||', '||N.FRST_NME ||' '||N.SEC_NME
                WHEN N.SURNME IS NULL AND N.FRST_NME IS NOT NULL
                    THEN N.FRST_NME ||' '||N.SURNME
                WHEN N.SURNME IS NULL AND N.FRST_NME IS NULL
                    THEN N.ORG_NME
                END AS REMIT_NAME
        FROM
            CSH_RCPT r,
            CSH_RCPT_PARTY p,
            CSH_BTCH_HDR b,
            NAME n,
            CSH_CDE REMT
        WHERE
            b.btch_id = r.btch_id
            AND p.csh_rcpt_id = r.csh_rcpt_id
            AND p.rec_typ = 'R'
            AND n.xent_id = p.xent_id  
            AND n.xent_id = p.xent_id
            AND n.cur_nme_ind = 'Y'
            AND n.ent_nme_typ = 'P'
            AND remt.csh_cde_id = r.csh_cde_id_remt
    ) REMIT_INFO on CR.CSH_RCPT_ID = REMIT_INFO.CSH_RCPT_ID
    left  join CSH_ALLC CAL on CRP.CSH_RCPT_PARTY_ID = CAL.CSH_RCPT_PARTY_ID 
    left  join (
        SELECT
            r.csh_rcpt_id CSH_RCPT_ID,
            n.xent_id BENE_ENTITY_ID,
            CASE WHEN N.SURNME IS NOT NULL AND N.FRST_NME IS NOT NULL
                    THEN N.SURNME ||', '||N.FRST_NME ||' '||N.SEC_NME
                WHEN N.SURNME IS NULL AND N.FRST_NME IS NOT NULL
                    THEN N.FRST_NME ||' '||N.SURNME
                WHEN N.SURNME IS NULL AND N.FRST_NME IS NULL
                    THEN N.ORG_NME
                END AS BENE_NAME
        FROM
            CSH_RCPT r,
            CSH_RCPT_PARTY p,
            CSH_BTCH_HDR b,
            NAME n,
            CSH_CDE REMT
        WHERE
            b.btch_id = r.btch_id
            AND p.csh_rcpt_id = r.csh_rcpt_id
            AND p.rec_typ = 'B'
            AND n.xent_id = p.xent_id
            AND n.cur_nme_ind = 'Y'
            AND n.ent_nme_typ = 'P'
            AND remt.csh_cde_id = r.csh_cde_id_remt
    ) BENE_INFO on CR.CSH_RCPT_ID = BENE_INFO.CSH_RCPT_ID
    left join ( SELECT  XACT_DESC, XACT_CDE, XACT_DEFN_ID, XACT_CLS_CDE FROM  XACT_DEFN XDF   ) ALLC_TYP_A  
              on CAL.XACT_DEFN_ID = ALLC_TYP_A.XACT_DEFN_ID AND CAL.XACT_CLS_CDE = ALLC_TYP_A.XACT_CLS_CDE and CAL.ALLC_TYP = 'A'     
    left join ( SELECT CHRG_DESC,CHRG_CDE ,MISC_CHRG_TYP_ID FROM MISC_CHRG_TYP MCT ) ALLC_TYP_M 
              on CAL.MISC_CHRG_TYP_ID = ALLC_TYP_M.MISC_CHRG_TYP_ID and CAL.ALLC_TYP = 'M'
    left join ( SELECT CSH_CDE_NME, CSH_CDE, CSH_CDE_ID FROM  CSH_CDE CSH ) ALLC_TYP_P 
              on CAL.CSH_CDE_ID_ALLC_PURP = ALLC_TYP_P.CSH_CDE_ID   and CAL.ALLC_TYP = 'P'  
    left join ( 
         select distinct a.CSH_RCPT_ID, count(b.CSH_RCPT_ID) cnt
           from  ( select distinct CRP.CSH_RCPT_ID 
                     from CSH_BTCH_HDR CBH
                     inner join CSH_RCPT CR on CBH.FSCL_YR = CR.FSCL_YR AND CBH.BTCH_ID = CR.BTCH_ID
                     inner join CLNT BRD on CR.CLNT_CDE = BRD.CLNT_CDE    
                     inner join CSH_CDE CSH on CSH.CSH_CDE_ID = CR.CSH_CDE_ID_REMT
                     inner join CSH_RCPT_PARTY CRP on CRP.CSH_RCPT_ID = CR.CSH_RCPT_ID 
                     inner join ( SELECT  r.csh_rcpt_id CSH_RCPT_ID,  n.xent_id REMIT_ENTITY_ID 
                                    FROM  CSH_RCPT r, CSH_RCPT_PARTY p, CSH_BTCH_HDR b,  NAME n, CSH_CDE REMT
                                   WHERE  b.btch_id = r.btch_id  AND p.csh_rcpt_id = r.csh_rcpt_id AND p.rec_typ = 'R' 
                                     AND n.xent_id = p.xent_id   AND n.xent_id = p.xent_id
                                     AND n.cur_nme_ind = 'Y'
                                     AND n.ent_nme_typ = 'P'
                                     AND remt.csh_cde_id = r.csh_cde_id_remt
                                ) REMIT_INFO on CR.CSH_RCPT_ID = REMIT_INFO.CSH_RCPT_ID
                     where  CR.BAD_CK_IND = 'N'
                       AND CSH.ACCT_FUNC = 'R'
                       AND CSH.csh_cde IN ('CHK', 'CSH', 'AMEX', 'CC', 'FTB') 
                       AND TRIM(TO_CHAR(CBH.FSCL_YR, '9999')) = '9999'
                       AND CBH.CLNT_CDE = 'BRD_CDE'
                       AND CBH.BTCH_NBR =12345
                  ) a
                 left join 
                 ( select distinct CRP.CSH_RCPT_ID from CSH_BTCH_HDR CBH
                     inner join CSH_RCPT CR on CBH.FSCL_YR = CR.FSCL_YR AND CBH.BTCH_ID = CR.BTCH_ID
                     inner join CLNT BRD on CR.CLNT_CDE = BRD.CLNT_CDE    
                     inner join CSH_CDE CSH on CSH.CSH_CDE_ID = CR.CSH_CDE_ID_REMT
                     inner join CSH_RCPT_PARTY CRP on CRP.CSH_RCPT_ID = CR.CSH_RCPT_ID
                     inner join ( SELECT  r.csh_rcpt_id CSH_RCPT_ID,  n.xent_id REMIT_ENTITY_ID 
                                    FROM  CSH_RCPT r, CSH_RCPT_PARTY p, CSH_BTCH_HDR b,  NAME n, CSH_CDE REMT
                                   WHERE  b.btch_id = r.btch_id  AND p.csh_rcpt_id = r.csh_rcpt_id AND p.rec_typ = 'R' 
                                     AND n.xent_id = p.xent_id   AND n.xent_id = p.xent_id
                                     AND n.cur_nme_ind = 'Y'
                                     AND n.ent_nme_typ = 'P'
                                     AND remt.csh_cde_id = r.csh_cde_id_remt
                                ) REMIT_INFO on CR.CSH_RCPT_ID = REMIT_INFO.CSH_RCPT_ID                     
                     where CRP.REC_TYP = 'B'
                       AND CR.BAD_CK_IND = 'N'
                       AND CSH.ACCT_FUNC = 'R'
                       AND CSH.csh_cde IN ('CHK', 'CSH', 'AMEX', 'CC', 'FTB') 
                       AND TRIM(TO_CHAR(CBH.FSCL_YR, '9999')) = '9999'
                       AND CBH.CLNT_CDE = 'BRD_CDE'
                       AND CBH.BTCH_NBR =12345
                       ) b 
                 on a.CSH_RCPT_ID = b.CSH_RCPT_ID
           group by a.CSH_RCPT_ID
               ) qn_exist on qn_exist.CSH_RCPT_ID = CRP.CSH_RCPT_ID 
     left join ( 
         select distinct a.CSH_ALLC_ID, count(b.CSH_ALLC_ID) cnt
           from  ( select distinct CAL.CSH_ALLC_ID 
                      from CSH_BTCH_HDR CBH
                     inner join CSH_RCPT CR on CBH.FSCL_YR = CR.FSCL_YR AND CBH.BTCH_ID = CR.BTCH_ID
                     inner join CLNT BRD on CR.CLNT_CDE = BRD.CLNT_CDE    
                     inner join CSH_CDE CSH on CSH.CSH_CDE_ID = CR.CSH_CDE_ID_REMT
                     inner join CSH_RCPT_PARTY CRP on CRP.CSH_RCPT_ID = CR.CSH_RCPT_ID 
                     inner join ( SELECT  r.csh_rcpt_id CSH_RCPT_ID,  n.xent_id REMIT_ENTITY_ID 
                                    FROM  CSH_RCPT r, CSH_RCPT_PARTY p, CSH_BTCH_HDR b,  NAME n, CSH_CDE REMT
                                   WHERE  b.btch_id = r.btch_id  AND p.csh_rcpt_id = r.csh_rcpt_id AND p.rec_typ = 'R' 
                                     AND n.xent_id = p.xent_id   AND n.xent_id = p.xent_id
                                     AND n.cur_nme_ind = 'Y'
                                     AND n.ent_nme_typ = 'P'
                                     AND remt.csh_cde_id = r.csh_cde_id_remt
                                ) REMIT_INFO on CR.CSH_RCPT_ID = REMIT_INFO.CSH_RCPT_ID 
                     left join CSH_ALLC CAL on CRP.CSH_RCPT_PARTY_ID = CAL.CSH_RCPT_PARTY_ID 
                     where  CR.BAD_CK_IND = 'N'
                       AND CSH.ACCT_FUNC = 'R'
                       AND CSH.csh_cde IN ('CHK', 'CSH', 'AMEX', 'CC', 'FTB') 
                       AND TRIM(TO_CHAR(CBH.FSCL_YR, '9999')) = '9999'
                       AND CBH.CLNT_CDE = 'BRD_CDE'
                       AND CBH.BTCH_NBR =12345 ) a
                 left join 
                 ( select distinct CAL.CSH_ALLC_ID
                     from CSH_BTCH_HDR CBH
                     inner join CSH_RCPT CR on CBH.FSCL_YR = CR.FSCL_YR AND CBH.BTCH_ID = CR.BTCH_ID
                     inner join CLNT BRD on CR.CLNT_CDE = BRD.CLNT_CDE    
                     inner join CSH_CDE CSH on CSH.CSH_CDE_ID = CR.CSH_CDE_ID_REMT
                     inner join CSH_RCPT_PARTY CRP on CRP.CSH_RCPT_ID = CR.CSH_RCPT_ID 
                     inner join ( SELECT  r.csh_rcpt_id CSH_RCPT_ID,  n.xent_id REMIT_ENTITY_ID 
                                    FROM  CSH_RCPT r, CSH_RCPT_PARTY p, CSH_BTCH_HDR b,  NAME n, CSH_CDE REMT
                                   WHERE  b.btch_id = r.btch_id  AND p.csh_rcpt_id = r.csh_rcpt_id AND p.rec_typ = 'R' 
                                     AND n.xent_id = p.xent_id   AND n.xent_id = p.xent_id
                                     AND n.cur_nme_ind = 'Y'
                                     AND n.ent_nme_typ = 'P'
                                     AND remt.csh_cde_id = r.csh_cde_id_remt
                                ) REMIT_INFO on CR.CSH_RCPT_ID = REMIT_INFO.CSH_RCPT_ID 
                     left join CSH_ALLC CAL on CRP.CSH_RCPT_PARTY_ID = CAL.CSH_RCPT_PARTY_ID 
                     where  CR.BAD_CK_IND = 'N'
                       AND CSH.ACCT_FUNC = 'R'
                       AND CSH.csh_cde IN ('CHK', 'CSH', 'AMEX', 'CC', 'FTB') 
                       AND TRIM(TO_CHAR(CBH.FSCL_YR, '9999')) = '9999'
                       AND CBH.CLNT_CDE = 'BRD_CDE'
                       AND CBH.BTCH_NBR =12345
                       AND CAL.ALLC_TYP = 'P'
                  ) b 
                 on a.CSH_ALLC_ID = b.CSH_ALLC_ID
           group by a.CSH_ALLC_ID
               ) q_exist on q_exist.CSH_ALLC_ID = CAL.CSH_ALLC_ID                                        
WHERE  CR.BAD_CK_IND = 'N'
    AND CSH.ACCT_FUNC = 'R'
      AND CSH.csh_cde IN ('CHK', 'CSH', 'CC', 'AMEX', 'FTB')
    AND (CRP.REC_TYP = 'B'
        OR qn_exist.cnt = 0 
        OR q_exist.cnt > 0 )
    AND TRIM(TO_CHAR(CBH.FSCL_YR, '9999')) = '9999'
    AND CBH.CLNT_CDE = 'BRD_CDE'
    AND CBH.BTCH_NBR =12345
;
