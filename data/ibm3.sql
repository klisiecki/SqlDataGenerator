   select cod_unidade,
          nom_unidade,
          cod_empregado_origem,
          cod_empregado,
          nom_empregado,
          cod_estrutura_funcional_pai as cod_estrutura_funcional,
          cod_grupo_produto,
          nom_grupo_produto,
      count(distinct cod_cliente) as qtd_cobertura_possiveis
   from (
      select eve.cod_unidade,
                 uni.nom_unidade,
                         e.cod_empregado_origem,
                         e.cod_empregado,
                         e.nom_empregado,
                         eve.cod_ef_quebra as cod_estrutura_funcional_pai,
                         gp1.cod_grupo_produto,
                         gp1.nom_grupo_produto,
                         cef.cod_cliente
        from tmp_sp_se_iq_hierarquia_charindex as eve
          join OPAV..UNIDADE as uni on uni.cod_unidade = eve.cod_unidade
          join OPAV..EMPREGADO as e on e.cod_empregado = eve.cod_quebra
          join opav..grupo_produto as gp1 on instr('|'||'89|34|32', '|'|| gp1.cod_grupo_produto::varchar(15) || '|') > 0
          join OPAV..cliente_estrutura_funcional as cef on cef.cod_estrutura_funcional = eve.cod_ef_vendedor
           and cef.dat_inicio_vigencia <= '2012-06-11 12:33:19'
                   and cef.dat_fim_vigencia >= '2012-06-11 12:33:19'
                   and exists ( select 1 from opav..frequencia_visita as fv
								  left outer join ( select av.cod_cliente
								                         , av.cod_estrutura_funcional  
								                         , count(*) av_cnt1 
								                      from opav..adiantamento_visita as av
													  where av.dat_visita_adiantamento = '2012-06-11 12:33:19'
													  group by 1,2
												  )	 av1 on fv.cod_cliente             = av1.cod_cliente
                                                        and fv.cod_estrutura_funcional = av1.cod_estrutura_funcional
								  left outer join ( select av.cod_cliente
								                         , av.cod_estrutura_funcional  
								                         , count(*) av_cnt1 
								                      from opav..adiantamento_visita as av
													  where av.dat_visita = '2012-06-11 12:33:19'
													  group by 1,2
												  )	 av2 on fv.cod_cliente             = av2.cod_cliente
                                                        and fv.cod_estrutura_funcional = av2.cod_estrutura_funcional														
                                 where fv.cod_estrutura_funcional = cef.cod_estrutura_funcional
                                                   and fv.cod_cliente = cef.cod_cliente
                                                   and fv.dat_inicio_vigencia <= '2012-06-11 12:33:19'
                                                   and fv.dat_fim_vigencia >= '2012-06-11 12:33:19'
                                                   and(
                                                         ( fv.num_dia_semana =   to_char('2012-06-11 12:33:19'::timestamp ,'DD') :: int
                                                           and av1.av_cnt1 = 0 ) 
													   or  av1.av_cnt1 > 0                                                            
                                                       )
                                           )
          and not exists( select 1 from OPAV..NOTA_FISCAL as nf
                            join OPAV..ITEM_NOTA_FISCAL as inf on inf.cod_unidade = nf.cod_unidade
                                                         and inf.dat_emissao = nf.dat_emissao
                                                         and inf.num_nf = nf.num_nf
                                                         and inf.nse_nf = nf.nse_nf
                                                         and inf.cod_material is null
                                                        join OPAV..NATUREZA_OPERACAO as nop on nop.cod_natureza_operacao = inf.cod_natureza_operacao
                                                         and nop.ind_volume = 'S'
                                                        join opav..produto as p on p.cod_prod = inf.cod_prod
                                                         and p.qtd_venda_produto > 0
                                                         and p.qtd_avulsa_prod > 0
                                                        join opav..produto as pd on pd.cod_prod = p.cod_prod_gerencial_venda
                                                        join opav..grupo_produto_produto as gpp on gpp.cod_prod = pd.cod_prod
                                                        join opav..grupo_produto as gp on gp.cod_grupo_produto = gpp.cod_grupo_produto
                                                         and gp.ftr_conversao > 0
                                                         and gp.qtd_unidade_composicao > 0
                                                         and instr('|'||'89|34|32', '|'|| gp.cod_grupo_produto::varchar(15) || '|') > 0
                                                   where nf.cod_cliente = cef.cod_cliente
                                                     and nf.dat_emissao >= '2012-05-31 00:00:00'
                                                         and nf.dat_emissao <= '2012-06-11 12:33:19'
                                                         and nf.dat_cancelamento is null
                                                         and nf.dat_vencimento is null
                             and not exists( select 1 from opav..nota_fiscal as nfis
                                                      where nfis.cod_unidade_referencia = nf.cod_unidade
                                                                        and nfis.dat_emissao_referencia = nf.dat_emissao
                                                                            and nfis.num_nf_referencia = nf.num_nf
                                                                            and nfis.nse_nf_referencia = nf.nse_nf
                                                                )
                             and gp.cod_grupo_produto = gp1.cod_grupo_produto
                                    )
                ) as a
    group by cod_unidade,
      nom_unidade,
          cod_empregado_origem,
          cod_empregado,
          nom_empregado,
          cod_estrutura_funcional_pai,
          cod_grupo_produto,
          nom_grupo_produto
