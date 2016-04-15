with cte_Gebruiker as (
select distinct  USR.ID ,USR.RECID
  from dbo.USERINFO USR
 union select '' as ID ,0 as RECID
),
cte_PercentageOpen as (
select CTO.REFRECID as TransactieID
	 , ISNULL((CTR.AMOUNTMST - CTR.SETTLEAMOUNTMST)/NULLIF(CTR.AMOUNTMST,0),0) as PercentageOpen
  from dbo.CUSTTRANSOPEN CTO
  join dbo.CUSTTRANS CTR on CTR.DATAAREAID = CTO.DATAAREAID and CTR.RECID = CTO.REFRECID
 where CTO.DATAAREAID = 'kmps' and CTO.AMOUNTMST != 0
),
cte_MapTrans as (
select FT.RECID, FTR.CUSTTRANSREFRECID
     , ROW_NUMBER() over (partition by FTR.CUSTTRANSREFRECID order by FT.STARTDATE desc) as RowNum
  from dbo.ICM_FIL_FILETABLE FT
  join dbo.ICM_FIL_FILETRANS FTR on FTR.FILEID = FT.FILEID
 where FT.DATAAREAID = 'kmps' and FT.STATUS not in (2,3,7)
),
cte_VerrekendeVoorschotten as (
select ILD.RECID as VVRecID
     , CIT.DOCUMENTDATE::DATE as DocumentDatum
     , CIT.INVOICEDATE::DATE as FactuurDatum
  from dbo.IUS_INV_INVOICELINEDETAIL ILD
  join dbo.IUS_INV_INVOICECALCSETTLEMENT ICS on ILD.INVOICEDLINETYPE = 1 and ILD.STATUS = 0
   and ICS.DATAAREAID = ILD.DATAAREAID 
   and ICS.SALESID = ILD.SALESID 
   and ICS.AGREEMENTID = ILD.AGREEMENTID
   and ICS.PRODUCTTERMSID = ILD.PRODUCTTERMSID 
   and ICS.PRICECOMPONENTID = ILD.PRICECOMPONENTID
   and ILD.LINESTARTDATE between ICS.FROMDATE and ICS.TODATE
   and ICS.STATUS = 0
   and ICS.TRANSTYPE = 1
  join dbo.CUSTINVOICETABLE CIT on CIT.DATAAREAID = ILD.DATAAREAID and CIT.IUS_INV_SALESID = ICS.OFFSETSALESID and CIT.IUS_INV_CUSTFREEINVOICESTATUS = 2
where ILD.DATAAREAID = 'kmps'
),
cte_KlantKoppeling as (
select 
	A.RECID as AgreementRecID
	,isnull(CT1.RECID,0) as DIM_FacturatieKlant_Key
	,isnull(CT2.RECID,0) as DIM_CommercieleKlant_Key
	,isnull(CT3.RECID,0) as DIM_DealKlant_Key
from dbo.IUS_AGR_AGREEMENT A
left join	dbo.IUS_AGR_AGREEMENTRELATION ARL on A.ISCOMMERCIALAGREEMENT = 0 and ARL.DATAAREAID = A.DATAAREAID and ARL.AGREEMENTID = A.AGREEMENTID and ARL.PARENTAGREEMENTID != ''
left join   dbo.IUS_AGR_AGREEMENTRELATION ART on ART.DATAAREAID = A.DATAAREAID and ART.AGREEMENTID = A.AGREEMENTID
left join   dbo.IUS_AGR_AGREEMENTRELATION ARD on ARD.DATAAREAID = A.DATAAREAID and ARD.TREEID = COALESCE(ARL.TREEID,ART.TREEID) and ARD.PARENTAGREEMENTID = ''
left join	dbo.IUS_AGR_AGREEMENT AGC on AGC.DATAAREAID = A.DATAAREAID and AGC.AGREEMENTID = ARL.PARENTAGREEMENTID and AGC.ISCOMMERCIALAGREEMENT = 1
left join	dbo.IUS_AGR_AGREEMENT AGD on AGD.DATAAREAID = A.DATAAREAID and AGD.AGREEMENTID = ARD.AGREEMENTID
left join	dbo.IUS_AGR_GROUPEDINVSTRUCTLINE GIS on GIS.DATAAREAID = A.DATAAREAID and GIS.AGREEMENTID = A.AGREEMENTID
left join	dbo.IUS_AGR_GROUPEDINVSTRUCTINFO GII on GII.DATAAREAID = A.DATAAREAID and GII.GROUPEDINVSTRUCTID = GIS.GROUPEDINVSTRUCTID and GII.CUSTACCOUNT != ''
left join	dbo.CUSTTABLE CT1 on CT1.DATAAREAID = A.DATAAREAID and CT1.AccountNum = COALESCE(GII.CUSTACCOUNT, A.CUSTACCOUNT)
left join	dbo.CUSTTABLE CT2 on CT2.DATAAREAID = A.DATAAREAID and CT2.AccountNum = COALESCE(AGC.CUSTACCOUNT,CT1.ACCOUNTNUM)
left join 	dbo.CUSTTABLE CT3 on CT3.DATAAREAID = A.DATAAREAID and CT3.AccountNum = COALESCE(AGD.CUSTACCOUNT,CT1.ACCOUNTNUM)   
where 
	A.DATAAREAID = 'kmps' and A.ISTEMPLATE = 0 and A.ISINVOICEAGREEMENT = 1 and A.STARTDATE != A.ENDDATE
),
cte_EenheidsOmrekening as (
select UMF.SYMBOL as Eenheid
,1 * UMC.FACTOR * UMC.NUMERATOR / UMC.DENOMINATOR as Factor
from ITP_FIXEDVALUE FIV
join UNITOFMEASURE UOM on UOM.SYMBOL = FIV.TEXTVALUE
left join UNITOFMEASURECONVERSION UMC on UMC.TOUNITOFMEASURE = UOM.RECID
left join UNITOFMEASURE UMF on UMF.RECID = UMC.FROMUNITOFMEASURE
where FIV.DATAAREAID = 'kmps' and FIV.FIXEDVALUEID = 'QCU'
),
cte_geboekteFacturen as (
select 
	'0' as PrognoseLijn
	,cast( to_char(T1.INVOICEDATE, 'YYYYMMDD') as integer) as FactuurDatum
	,cast( to_char(T3.LINESTARTDATE, 'YYYYMMDD') as integer) as DIM_LeveringsDatum_Key
	,coalesce(T9.RecID,0) as DIM_Aansluiting_Key
	,T1.INVOICEID as FactuurID
	,case when T1.invoiceID  = ''  then 0 else T1.RECID end  as DIM_Factuur_Key
	,coalesce(T2.DefaultDimension,T1.DefaultDimension, 0) as DIM_FinancieleDimensie_Key 
	,coalesce(MA.RECID,0) as DIM_GrootboekRekening_Key
	,T1.INVOICEACCOUNT as CustomerID
	,CUS.RECID as DIM_FacturatieKlant_Key
	,ISNULL(T4.DIM_CommercieleKlant_Key,0) as DIM_CommercieleKlant_Key
	,ISNULL(T4.DIM_DealKlant_Key,0) as DIM_DealKlant_Key
	,coalesce(T3.PriceComponentID,T2.IUS_INV_ITEMID) as PrijsComponentID
	,coalesce(T7.Recid,0) as DIM_PrijsComponent_Key
	,T3.PRODUCTID as Product
	,coalesce(T6.Recid,0) as DIM_Product_Key
	,T3.AGREEMENTID as ContractNummer
	,coalesce(T5.Recid,0) as DIM_Contract_Key	
	,cast(coalesce(T3.QTY,T2.QUANTITY) as decimal(18,2)) as Hoeveelheid	
	,cast(coalesce(T3.Price,T2.UNITPRICE)as decimal(18,2)) as Prijs
    ,cast(coalesce(T3.LineAmount,T2.AMOUNTCUR)as decimal(18,2)) as Bedrag
    ,cast((coalesce(T3.LineAmount,T2.AMOUNTCUR) * coalesce(T1b.PercentageOpen,0)) as decimal(18,2)) as Saldo
	,0 as PrognoseHoeveelheid
	,0 as PrognosePrijs
	,0 as PrognoseBedrag
	,cast( to_char(T3.CONSUMPTIONSTARTDATE, 'YYYYMMDD') as integer) as StartDatumVerbruik
	,cast( to_char(T3.CONSUMPTIONENDDATE, 'YYYYMMDD') as integer) as EindDatumVerbruik
	,cast( to_char(T3.PRICESTARTDATE, 'YYYYMMDD') as integer) as StartDatumPrijs
	,cast( to_char(T3.PRICEENDDATE, 'YYYYMMDD') as integer) as EindDatumPrijs
	,cast( to_char(T3.LINESTARTDATE, 'YYYYMMDD') as integer) as BeginDatumLijn
	,cast( to_char(T3.LINEENDDATE, 'YYYYMMDD') as integer) as EindDatumLijn
	,cast( to_char(T3.CALENDARSTARTDATE, 'YYYYMMDD') as integer) as KalenderStartDatum
	,cast( to_char(T3.CALENDARENDDATE, 'YYYYMMDD') as integer) as KalenderEindDatum  	
	, (T2.TaxGroup 
		|| '#' || T2.TaxItemGroup 
		|| '#' || coalesce(T3.INVOICEDLINETYPE,-1)::varchar(64) 
		|| '#' || coalesce(T3.INVOICEDIRECTION,-1)::varchar(64)
		|| '#' || coalesce(T3.Status,-1)::varchar(64)
		|| '#' || coalesce(nullif(T3.TARIFFCODE,''),'-1') 
		|| '#' || coalesce(nullif(T2.IUS_INV_SALESUNIT,''),'-1')
		|| '#' || 
			case when T3.INVOICEDLINETYPE = 1 and T3.STATUS = 0 and T3.REVERSEDSALESID || T3.CORRECTEDSALESID = '' 
			     then 
			case when VVO.VVRecID is not null then '1' else '0' end
				else '-1' end
	   ) as DIM_Factuurlijn_Key		

	,ISNULL(USR.RECID,0) as DIM_Gebruiker_Key
	,coalesce(T10.recid,0) as DIM_StamData_Key
	,(case 
		when T12.RECID is null then '-1'
		else T12.staffleset::varchar(64) || '#' || T12.applcodeid::varchar(64) || '#' || T12."YEAR"::varchar(64) || '#' || T12."ZONE"::varchar(64) end) as DIM_EnergieStaffel_Key
	,case 
		when (coalesce(T3.PriceComponentID,T2.IUS_INV_ITEMID) = 'E_REB_WVF') then 1
		when (coalesce(T3.PriceComponentID,T2.IUS_INV_ITEMID) = 'E_REB') then 2
		else 0 end as DIM_Heffingskorting_Key	
	,ISNULL(MDM.DISTRMETHODID, '#NVT') as WerkelijkProfiel
	,ISNULL( CR.RECID ,0) as DIM_BillingRunInfo_Key
	,ISNULL(GIS.RECID ,0) as DIM_FactuurStructuur_Key
	,ISNULL(MAP.RECID ,0) as DIM_Map_Key
	,case when T3.INVOICEDLINETYPE = 1 and T3.STATUS = 0 and T3.REVERSEDSALESID || T3.CORRECTEDSALESID = '' 
	      then abs(T1.DOCUMENTDATE::date - ISNULL(VVO.DocumentDatum::date,current_date))
	      else 0 
	      end as AantalDagenUitstaandVoorschot
	,case when T1.IUS_INV_BILLINGCATEGORY = 0 and T2.DEFAULTDIMENSION > 0 then T2.DEFAULTDIMENSION - 5637143000 else 0 end as DIM_GrootboekDimensie_Key
	,cast( to_char( 
	       COALESCE( IUI.STATEMENTSELECTIONDATE
	               , VVO.FactuurDatum
	               , INI.STATEMENTSELECTIONDATE 
	               , '19000101'::date
	               ), 'YYYYMMDD'
	               ) as integer 
	     ) as VerwachteAfrekenDatum	               	     
	,cast((coalesce(T3.QTY,T2.QUANTITY) * ISNULL(EHO.Factor,0))as decimal(18,2)) as OmgerekendeHoeveelheid
	,0 as OmgerekendePrognoseHoeveelheid
from 
	dbo.CUSTINVOICETABLE T1
left join 	dbo.Custtrans T1a on T1.INVOICEID = T1a.VOUCHER and T1.DATAAREAID = T1a.DATAAREAID 
left join	cte_PercentageOpen T1b on T1a.recid = T1b.TransactieID 
left join 	dbo.CUSTTABLE CUS on CUS.DATAAREAID = T1.DATAAREAID and CUS.ACCOUNTNUM = T1.INVOICEACCOUNT
left join 	cte_Gebruiker USR on USR.ID = COALESCE(NULLIF(T1.IUS_INV_CALCAPPROVEDBY,''),CASE T1.IUS_INV_BILLINGCATEGORY WHEN 0 THEN T1.CREATEDBY ELSE '' END)
left join   IUS_INV_CALCLOG CR on CR.DATAAREAID = T1.DATAAREAID and CR.CALCRUNID = T1.IUS_INV_CALCRUNID
left join   IUS_AGR_GROUPEDINVSTRUCTINFO GIS on GIS.DATAAREAID = T1.DATAAREAID and GIS.GROUPEDINVSTRUCTID = T1.IUS_AGR_INVSTRUCTID
join     	dbo.CUSTINVOICELINE T2 on T2.DATAAREAID = T1.DATAAREAID and T2.PARENTRECID = T1.RECID and T1.DataAreaID = T2.DataAreaID
left join 	cte_EenheidsOmrekening EHO on EHO.Eenheid = T2.IUS_INV_SALESUNIT
left join 	dbo.DIMENSIONATTRIBUTEVALUECOMBINATION AVC on AVC.RECID = T2.LEDGERDIMENSION
left join  	dbo.IUS_INV_INVOICELINEDETAIL T3 on T3.DATAAREAID = T2.DataAreaID and T3.SALESID = T1.IUS_INV_SALESID and T3.LINENUM = T2.LINENUM and T2.DataareaID = T3.DataAreaID
left join  	dbo.IUS_AGR_AGREEMENT T5 on T3.AGREEMENTID = T5.AgreementID and T3.DAtaareaid = T3.Dataareaid
left join  	cte_KlantKoppeling T4 on T4.AgreementRecID = T5.RECID
left join 	dbo.IUS_PRD_Product T6 on T6.ProductID = T3.PRODUCTID and T6.DataAreaID = T3.DataAreaid
left join 	dbo.IUS_PRD_PriceComponent T7 on T7.ComponentID = coalesce(T3.PriceComponentID,T2.IUS_INV_ITEMID) and T7.DataAreaID = coalesce(T3.DataAreaID,T2.DataAreaID)
left join 	dbo.IUS_AGR_DeliveryTerms T8 on T8.AgreementID = T3.AGREEMENTID  and (T3.LINESTARTDATE between T8.StartDate and T8.Enddate) and T8.DataAreaID = T3.DataAreaID
left join 	dbo.IUS_MPO_MeteringPointTable T9 on T8.DeliveryPoint = T9.MeteringPointID and T8.DataAreaID = T9.DataAreaID
left join 	dbo.IUS_MPO_MasterData T10 on T9.MeteringPointID=T10.MeteringPointID and T9.DataAreaID=T10.DataAreaID  and T3.LINESTARTDATE between T10.FromDate and coalesce(nullif(T10.todate,'1900-01-01'),'2100-01-01')
left join 	dbo.IUS_INV_ENERGYTAXCUMINVLINE T12 on T3.RECID = T12.INVDETAILRECID and T3.LINENUM = T12.LINENUM and T12.SALESID = T3.SALESID
left join 	dbo.MAINACCOUNT MA on MA.RECID = AVC.MAINACCOUNT 
left join 	dbo.IUS_MPO_METERINGPOINTDISTRMETHODS MDM on MDM.DATAAREAID = T1.DATAAREAID and MDM.METERINGPOINTID = T9.METERINGPOINTID and T1.INVOICEDATE between MDM.FROMDATE and ISNULL(NULLIF(MDM.TODATE,'1900-01-01'),'2154-12-31')
left join 	cte_MapTrans MAP on MAP.RowNum = 1 and MAP.CUSTTRANSREFRECID = T1A.RECID
left join 	cte_VerrekendeVoorschotten VVO on VVO.VVRecID = T3.RECID
left join 	dbo.IUS_INV_INVOICEDUNITINFO IUI on T3.INVOICEDLINETYPE in (2,3) and IUI.DATAAREAID = T1.DATAAREAID and IUI.AGREEMENTID = T3.AGREEMENTID  and IUI.SALESID = T1.IUS_INV_SALESID and IUI.TRANSTYPE = 2
left join 	dbo.IUS_AGR_INVOICEINFO INI on T3.INVOICEDLINETYPE = 1 and VVO.VVRecID is null and INI.DATAAREAID = T1.DATAAREAID and INI.AGREEMENTID = T3.AGREEMENTID
where 
	T1.DataAreaID = 'kmps' and T1.INVOICEID != ''
)
select * from cte_geboekteFacturen
distribute on random ;
