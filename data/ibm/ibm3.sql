
SELECT cod_unidade,
       nom_unidade,
       cod_empregado_origem,
       cod_empregado,
       nom_empregado,
       cod_estrutura_funcional_pai AS cod_estrutura_funcional,
       cod_grupo_produto,
       nom_grupo_produto,
       count(DISTINCT cod_cliente) AS qtd_cobertura_possiveis
FROM
    (SELECT eve.cod_unidade,
            uni.nom_unidade,
            e.cod_empregado_origem,
            e.cod_empregado,
            e.nom_empregado,
            eve.cod_ef_quebra AS cod_estrutura_funcional_pai,
            gp1.cod_grupo_produto,
            gp1.nom_grupo_produto,
            cef.cod_cliente
     FROM tmp_sp_se_iq_hierarquia_charindex AS eve
     JOIN OPAV..UNIDADE AS uni ON uni.cod_unidade = eve.cod_unidade
     JOIN OPAV..EMPREGADO AS e ON e.cod_empregado = eve.cod_quebra
     JOIN opav..grupo_produto AS gp1 ON instr('|'||'89|34|32', '|'|| gp1.cod_grupo_produto::varchar(15) || '|') > 0
     JOIN OPAV..cliente_estrutura_funcional AS cef ON cef.cod_estrutura_funcional = eve.cod_ef_vendedor
     AND cef.dat_inicio_vigencia <= '2012-06-11 12:33:19'
     AND cef.dat_fim_vigencia >= '2012-06-11 12:33:19'
     AND EXISTS
         (SELECT 1
          FROM opav..frequencia_visita AS fv
          LEFT OUTER JOIN
              (SELECT av.cod_cliente,
                      av.cod_estrutura_funcional,
                      count(*) av_cnt1
               FROM opav..adiantamento_visita AS av
               WHERE av.dat_visita_adiantamento = '2012-06-11 12:33:19'
               GROUP BY 1,
                        2) av1 ON fv.cod_cliente = av1.cod_cliente
          AND fv.cod_estrutura_funcional = av1.cod_estrutura_funcional
          LEFT OUTER JOIN
              (SELECT av.cod_cliente,
                      av.cod_estrutura_funcional,
                      count(*) av_cnt1
               FROM opav..adiantamento_visita AS av
               WHERE av.dat_visita = '2012-06-11 12:33:19'
               GROUP BY 1,
                        2) av2 ON fv.cod_cliente = av2.cod_cliente
          AND fv.cod_estrutura_funcional = av2.cod_estrutura_funcional
          WHERE fv.cod_estrutura_funcional = cef.cod_estrutura_funcional
              AND fv.cod_cliente = cef.cod_cliente
              AND fv.dat_inicio_vigencia <= '2012-06-11 12:33:19'
              AND fv.dat_fim_vigencia >= '2012-06-11 12:33:19'
              AND((fv.num_dia_semana = to_char('2012-06-11 12:33:19'::TIMESTAMP,'DD') :: int
                   AND av1.av_cnt1 = 0)
                  OR av1.av_cnt1 > 0))
     AND NOT EXISTS
         (SELECT 1
          FROM OPAV..NOTA_FISCAL AS nf
          JOIN OPAV..ITEM_NOTA_FISCAL AS inf ON inf.cod_unidade = nf.cod_unidade
          AND inf.dat_emissao = nf.dat_emissao
          AND inf.num_nf = nf.num_nf
          AND inf.nse_nf = nf.nse_nf
          AND inf.cod_material IS NULL
          JOIN OPAV..NATUREZA_OPERACAO AS nop ON nop.cod_natureza_operacao = inf.cod_natureza_operacao
          AND nop.ind_volume = 'S'
          JOIN opav..produto AS p ON p.cod_prod = inf.cod_prod
          AND p.qtd_venda_produto > 0
          AND p.qtd_avulsa_prod > 0
          JOIN opav..produto AS pd ON pd.cod_prod = p.cod_prod_gerencial_venda
          JOIN opav..grupo_produto_produto AS gpp ON gpp.cod_prod = pd.cod_prod
          JOIN opav..grupo_produto AS gp ON gp.cod_grupo_produto = gpp.cod_grupo_produto
          AND gp.ftr_conversao > 0
          AND gp.qtd_unidade_composicao > 0
          AND instr('|'||'89|34|32', '|'|| gp.cod_grupo_produto::varchar(15) || '|') > 0
          WHERE nf.cod_cliente = cef.cod_cliente
              AND nf.dat_emissao >= '2012-05-31 00:00:00'
              AND nf.dat_emissao <= '2012-06-11 12:33:19'
              AND nf.dat_cancelamento IS NULL
              AND nf.dat_vencimento IS NULL
              AND NOT EXISTS
                  (SELECT 1
                   FROM opav..nota_fiscal AS nfis
                   WHERE nfis.cod_unidade_referencia = nf.cod_unidade
                       AND nfis.dat_emissao_referencia = nf.dat_emissao
                       AND nfis.num_nf_referencia = nf.num_nf
                       AND nfis.nse_nf_referencia = nf.nse_nf)
              AND gp.cod_grupo_produto = gp1.cod_grupo_produto)) AS a
GROUP BY cod_unidade,
         nom_unidade,
         cod_empregado_origem,
         cod_empregado,
         nom_empregado,
         cod_estrutura_funcional_pai,
         cod_grupo_produto,
         nom_grupo_produto