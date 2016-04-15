
SELECT
    TRIM(TO_CHAR(CBH.FSCL_YR, '9999')) AS FSCL_YR,
    -- changing to_char for the CR parameterCBH.CLNT_CDE AS BOARDCODE, BRD.CLNT_NME AS BOARDNAME, CBH.BTCH_NBR AS BTCH_NBR, CSH.CSH_CDE_NME AS REMIT_TYPE, CBH.BTCH_RCPT_DTE AS RCPT_DT, CR.CSH_RCPT_ID AS CSH_RCPT_ID, CBH.INIT_RCPT_NBR AS iNIT_RCPT_NBR, CBH.END_RCPT_NBR AS END_RCPT_NBR, CAL.ALLC_AMT,CBH.RCPT_CNT AS RCPT_CNT, CBH.RCPT_CNT_CTRL AS RCPT_CNT_CTRL, CBH.REMT_AMT_TOT AS REMT_AMT_TOT, CBH.REMT_AMT_CTRL AS REMT_AMT_CTRL, CR.REMT_AMT AS RCPT_REMT_AMT, CR.DLN AS DLN,CR.CMNT AS PAYOR_CMTS,CR.VALD_NBR AS RCPT_NO,CRP.REC_TYP AS REC_TYP, CBH.CHG_OPER_ID AS CHNG_USER_ID,Netezza Page 1
    CBH.CHG_OPER_ID AS CHNG_USER_ID,
    CBH.CHG_TME_STMP AS CHNG_USER_DT,
    CAL.ALLC_TYP AS ALL_TYP,
    CASE WHEN CAL.ALLC_TYP = 'A'
            THEN (
            SELECT
                XACT_DESC
            FROM
                XACT_DEFN XDF
            WHERE
                CAL.XACT_DEFN_ID = XDF.XACT_DEFN_ID AND CAL.XACT_CLS_CDE = XDF.XACT_CLS_CDE
        )
        WHEN CAL.ALLC_TYP = 'M'
            THEN (
            SELECT
                CHRG_DESC
            FROM
                MISC_CHRG_TYP MCT
            WHERE
                CAL.MISC_CHRG_TYP_ID = MCT.MISC_CHRG_TYP_ID
        )
        WHEN CAL.ALLC_TYP IS NULL
            THEN 'AMOUNT NOT ALLOCATED'
        END AS XDESC,
    CASE WHEN CAL.ALLC_TYP = 'A'
            THEN (
            SELECT
                XACT_CDE
            FROM
                XACT_DEFN XDF
            WHERE
                CAL.XACT_DEFN_ID = XDF.XACT_DEFN_ID AND CAL.XACT_CLS_CDE = XDF.XACT_CLS_CDE
        )
        WHEN CAL.ALLC_TYP = 'M'
            THEN (
            SELECT
                CHRG_CDE
            FROM
                MISC_CHRG_TYP MCT
            WHERE
                CAL.MISC_CHRG_TYP_ID = MCT.MISC_CHRG_TYP_ID
        )
        WHEN CAL.ALLC_TYP IS NULL
            THEN 'XX'
        END AS XCODE,
    CASE WHEN CAL.ALLC_TYP = 'P'
            THEN (
            SELECT
                CSH_CDE
            FROM
                CSH_CDE CSH
            WHERE
                CAL.CSH_CDE_ID_ALLC_PURP = CSH.CSH_CDE_ID
        )
        END AS RFND_CDE,
    CASE WHEN CAL.ALLC_TYP = 'P'
            THEN (
            SELECT
                CSH_CDE_NME
            FROM
                CSH_CDE CSH
            WHERE
                CAL.CSH_CDE_ID_ALLC_PURP = CSH.CSH_CDE_ID
        )
        END AS RFND_DESC,
    REMIT_INFO.REMIT_NAME AS REMIT_NAME,
    REMIT_INFO.REMIT_ENTITY_ID AS REMIT_ENTITY_ID,
    BENE_INFO.BENE_NAME AS BENEF_NAME,
    BENE_INFO.BENE_ENTITY_ID
FROM
    CSH_BTCH_HDR CBH,
    CLNT BRD,
    CSH_RCPT CR,
    CSH_CDE CSH,
    CSH_RCPT_PARTY CRP,
    CSH_ALLC CAL,
    (
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
            AND n.xent_id = p.xent_id Netezza Page 2
            AND n.xent_id = p.xent_id
            AND n.cur_nme_ind = 'Y'
            AND n.ent_nme_typ = 'P'
            AND remt.csh_cde_id = r.csh_cde_id_remt
    ) REMIT_INFO,
    (
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
    ) BENE_INFO CBH.FSCL_YR = CR.FSCL_YR
WHERE
    AND CR.CLNT_CDE = BRD.CLNT_CDE
    AND CBH.BTCH_ID = CR.BTCH_ID
    AND CRP.CSH_RCPT_ID = CR.CSH_RCPT_ID
    AND CRP.CSH_RCPT_PARTY_ID = CAL.CSH_RCPT_PARTY_ID(+)
    AND CSH.CSH_CDE_ID = CR.CSH_CDE_ID_REMT
    AND CR.CSH_RCPT_ID = REMIT_INFO.CSH_RCPT_ID
    AND CR.CSH_RCPT_ID = BENE_INFO.CSH_RCPT_ID(+)
    AND CR.BAD_CK_IND = 'N'
    AND CSH.ACCT_FUNC = 'R'
    AND CSH.csh_cde IN ('CHK', 'CSH', 'CC', 'AMEX', 'FTB')
    AND (CRP.REC_TYP = 'B'
        OR NOT EXISTS (
            SELECT
                1
            FROM
                CSH_RCPT_PARTY CRP2
            WHERE
                CRP.CSH_RCPT_ID = CRP2.CSH_RCPT_ID
                AND CRP2.REC_TYP = 'B'
        )
        OR EXISTS (
            SELECT
                1
            FROM
                CSH_ALLC CAL1
            WHERE
                CAL.CSH_ALLC_ID = CAL1.CSH_ALLC_ID
                AND CAL1.ALLC_TYP = 'P'
        ))
    AND TRIM(TO_CHAR(CBH.FSCL_YR, '9999')) = '{?FSCL_YR}'
    AND CBH.CLNT_CDE = '{?BRD_CDE}'
    AND CBH.BTCH_NBR ={?BTCH_NBR}

