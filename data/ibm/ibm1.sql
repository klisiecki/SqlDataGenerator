SELECT 
           PESSOA.IDME || PESSOA.IDPN IDPNME,
           PESSOA.IDPN IDPN,
           NVL(PESSOA.CDRES, 'X') CDRES,
           NVL(PESSOA.CDSECT, 'XXXXXX') CDSECT,
           NVL(PESSOA.CDTYCL, 'XX') CDTYCL,
           SUBSTR(TO_CHAR(PESSOA.IDAE, '09999'), 2, 5) IDAE,
           PESSOA.LNDPN LNDPN,
           NVL(TO_CHAR(PESSOA.DINS, 'YYYYMMDD'), DEFAULT_DATINI_V) DINS,
           DECODE(LENGTH(TRUNC(MONTHS_BETWEEN(TO_DATE(DT_INCREMENTO_P, 'YYYYMMDD'), PESSOA.DINS) / 12)),  4, 999,
                  NVL(TRUNC(MONTHS_BETWEEN(TO_DATE(DT_INCREMENTO_P, 'YYYYMMDD'), PESSOA.DINS) / 12), 0)) NUIDADE,
           NVL(TO_CHAR(PESSOA.DIPN, 'YYYYMMDD'), DEFAULT_DATINI_V) DIPN,
           NVL(PESSOA.CDFPRF, 'X' || NVL(PESSOA.IDPK, 'XXX')) CDFPRF,
           SUBSTR(TO_CHAR(PESSOA.IDAE14, '09999'), 2, 5) IDAE14,
           NVL(SUBSTR(TO_CHAR(PESSOA.IDFJ, '99999'), 2, 5), '    0') IDFJ,
           NVL(PESSOA.IDSGM, 'XXXXXXXX') IDSGM,
           NVL(PESSOA.MTXBPP, 0) MTXBPP,
           NVL(PESSOA.QTPEAC, 0) QTPEAC,
           --
           CASE WHEN PESSOA.CDTYCL = '10' AND  o.counter > 0 THEN '1' ELSE '0' END AS INIMIGRANTE
    FROM
		TPESSOA_VIEW PESSOA
				LEFT JOIN TPESEND_VIEW PESEND ON 
					PESSOA.IDPN = PESEND.IDPN
					AND    PESSOA.IDENT = PESEND.IDENT
					AND    PESSOA.IDSUBENT = PESEND.IDSUBENT
					AND    decode(PESEND.CTUPDATE, 'D', 0, 1) = 1
                    AND    PESEND.IDTYAD    = '1'
		LEFT JOIN TENDER_VIEW ENDER ON
					PESEND.IDAD = ENDER.IDAD
					AND    PESEND.IDENT = ENDER.IDENT
					AND    PESEND.IDSUBENT = ENDER.IDSUBENT	
					AND    decode(ENDER.CTUPDATE , 'D', 0, 1) = 1
		LEFT JOIN D_COR_ESTAB_AUX ESTAB ON
					PESSOA.IDENT = ESTAB.IDENT
					AND    PESSOA.IDSUBENT = ESTAB.IDSUBENT
					AND    PESSOA.IDPN = ESTAB.IDET
		LEFT JOIN D_COR_CONTACT_AUX2 CONTACT ON
					PESSOA.IDENT = CONTACT.IDENT
					AND    PESSOA.IDSUBENT = CONTACT.IDSUBENT
					AND    PESSOA.IDPN = CONTACT.IDPN	
        LEFT JOIN D_COR_DOCID_AUX DOCID ON
					PESSOA.IDENT = DOCID.IDENT
					AND    PESSOA.IDSUBENT = DOCID.IDSUBENT
					AND    PESSOA.IDPN = DOCID.IDPN
        LEFT JOIN D_COR_SEGRISCO_AUX3 SEGRISCO ON
					PESSOA.IDENT = SEGRISCO.IDENT
					AND    PESSOA.IDSUBENT = SEGRISCO.IDSUBENT
					AND    PESSOA.IDPN = SEGRISCO.IDPN
        LEFT JOIN (
		     select PESSOA.IDENT,  PESSOA.IDSUBENT , PESSOA.IDWANAC , count(A.IDWA) counter
			   from TPESSOA_VIEW PESSOA 
			         left outer join ( select A.IDENT, A.IDSUBENT , TO_NUMBER(A.IDWA, '999999') IDWA 
					                    from D_AUX_PAIS_INIMIGRANTE A 
										group by 1,2,3
					 ) A on PESSOA.IDENT = A.IDENT and  PESSOA.IDSUBENT = A.IDSUBENT and PESSOA.IDWANAC = A.IDWA
			  group by 1,2,3
		) O on PESSOA.IDENT = o.IDENT and  PESSOA.IDSUBENT = o.IDSUBENT and PESSOA.IDWANAC = o.IDWANAC
    ;