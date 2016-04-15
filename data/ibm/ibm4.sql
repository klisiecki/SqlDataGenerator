WITH sawith0 AS
    (SELECT DISTINCT t62723.perioada AS c1,
                     t62723.an_raportare AS c2,
                     t62723.data_inr_partener AS c3,
                     CASE t62723.inr_partener
                         WHEN 1 THEN 'DA'
                         ELSE 'NU'
                     END AS c4,
                     t62723.data_inr_cui AS c5,
                     CASE t62723.inr_cui
                         WHEN 1 THEN 'DA'
                         ELSE 'NU'
                     END AS c6,
                     CASE t62723.platitor_tva_partener
                         WHEN 1 THEN 'DA'
                         ELSE 'NU'
                     END AS c7,
                     CASE t62723.platitor_tva_cui
                         WHEN 1 THEN 'DA'
                         ELSE 'NU'
                     END AS c8,
                     CASE t62723.invalid_partener
                         WHEN 1 THEN 'DA'
                         ELSE 'NU'
                     END AS c9,
                     CASE t62723.invalid_cui
                         WHEN 1 THEN 'DA'
                         ELSE 'NU'
                     END AS c10,
                     t62723.dif_tva / NULLIF (t62723.tva_cui,
                                              0) * 100 AS c11,
                                                   t62723.dif_tva AS c12,
                                                   t62723.tva_partener AS c13,
                                                   t62723.tva_cui AS c14,
                                                   t62723.dif_bi / NULLIF (t62723.baza_impozabila_cui,
                                                                           0) * 100 AS c15,
                                                                                t62723.dif_bi AS c16,
                                                                                t62723.baza_impozabila_partener AS c17,
                                                                                t62723.baza_impozabila_cui AS c18,
                                                                                t62723.data_rad_partener AS c19,
                                                                                CASE t62723.radiat_partener
                                                                                    WHEN 1 THEN 'DA'
                                                                                    ELSE 'NU'
                                                                                END AS c20,
                                                                                t62723.data_rad_cui AS c21,
                                                                                CASE t62723.radiat_cui
                                                                                    WHEN 1 THEN 'DA'
                                                                                    ELSE 'NU'
                                                                                END AS c22,
                                                                                t62723.cod_tva_partener AS c24,
                                                                                t62723.tip_operatiune AS c25,
                                                                                t62723.cui AS c27,
                                                                                CASE
                                                                                    WHEN CONCAT ('', 'X') <> 'X'
                                                                                         AND t62723.an_raportare >= 2012
                                                                                         AND (t62723.perioada IN ('L11',
                                                                                                                  'L10',
                                                                                                                  'L8',
                                                                                                                  'L7',
                                                                                                                  'L5',
                                                                                                                  'L4',
                                                                                                                  'L1',
                                                                                                                  'L2' ) ) THEN 'ATENTIE! Pentru perioada de raportare selectata s-au calculat neconcordante numai fata de platitorii lunari de TVA. '
                                                                                    ELSE CASE
                                                                                             WHEN CONCAT ('', 'X') <> 'X'
                                                                                                  AND t62723.an_raportare >= 2012
                                                                                                  AND (t62723.perioada IN ('L12',
                                                                                                                           'L9',
                                                                                                                           'L3',
                                                                                                                           'L6')) THEN 'ATENTIE! Pentru perioada de raportare selectata s-au calculat neconcordante fata de platitorii lunari si trimestriali de TVA. '
                                                                                         END
                                                                                END AS c28,
                                                                                t63058.af_denumire AS c29,
                                                                                t63058.denumire_judet AS c30,
                                                                                t56489.ca_la_data AS c31
     FROM crc_caladata t56489,
         (SELECT *
          FROM d_structuri) t63058, (((contribuabil t12967 /* CONTRIBUABIL_DIM */
                                       LEFT OUTER JOIN forma_juridica t7052 ON t7052.ID = t12967.jur_jur_id)
                                      LEFT OUTER JOIN cod_grupe_juridice t20232 ON t12967.grup_juridic = t20232.cod)
                                     LEFT OUTER JOIN cod_tipcontrib t21022 ON t12967.contrib_type = t21022.cod)
     LEFT OUTER JOIN cod_forme_proprietate t21034 ON t12967.forma_id = t21034.cod ,
                                                     crc_declaratii_394 t62723
     WHERE (t12967.cui = t62723.cui
            AND t56489.ca_la_data = t62723.ca_la_data
            AND t62723.jud_cod_adm = t63058.af_jud_cod
            AND t62723.tsm_tip_adm = t63058.af_tsm_tip
            AND t62723.smf_id_adm = t63058.af_smf_id
            AND (CASE t62723.radiat_cui WHEN 1 THEN 'DA' ELSE 'NU' END IN ('DA')
                 OR CASE t62723.radiat_partener WHEN 1 THEN 'DA' ELSE 'NU' END IN ('DA') ) ))
SELECT DISTINCT sawith0.c28 AS c1,
                sawith0.c30 AS c3,
                sawith0.c29 AS c4,
                sawith0.c27 AS c5,
                sawith0.c25 AS c7,
                sawith0.c24 AS c8,
                sawith0.c22 AS c10,
                sawith0.c21 AS c11,
                sawith0.c20 AS c12,
                sawith0.c19 AS c13,
                sawith0.c18 AS c14,
                sawith0.c17 AS c15,
                sawith0.c16 AS c16,
                sawith0.c15 AS c17,
                sawith0.c14 AS c18,
                sawith0.c13 AS c19,
                sawith0.c12 AS c20,
                sawith0.c11 AS c21,
                sawith0.c10 AS c22,
                sawith0.c9 AS c23,
                sawith0.c8 AS c24,
                sawith0.c7 AS c25,
                sawith0.c6 AS c26,
                sawith0.c5 AS c27,
                sawith0.c4 AS c28,
                sawith0.c3 AS c29,
                sawith0.c2 AS c30,
                sawith0.c1 AS c31,
                sawith0.c31 AS c32
FROM sawith0