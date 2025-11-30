# Otimização de Consultas Espaciais: Uma Análise da Eficiência de Árvores R*

Este projeto apresenta uma análise comparativa de diferentes estruturas de dados espaciais para otimização de consultas de range query em dados bidimensionais. O trabalho foi desenvolvido para um estudo acadêmico sobre a eficiência de árvores espaciais, com foco especial na R*-Tree.

## 📋 Descrição

O projeto implementa e compara três abordagens diferentes para consultas espaciais:

1. **Busca Linear** (`Linear.java`) - Implementação baseline com complexidade O(n)
2. **QuadTree** (`QuadTree.java`) - Árvore quaternária para particionamento espacial
3. **R*-Tree** (`RStarTree.java`) - Árvore R* otimizada com algoritmos de split e reinsert

## 🎯 Objetivos

- Avaliar o desempenho de diferentes estruturas de dados espaciais
- Comparar eficiência em cenários de distribuição uniforme e clusterizada
- Analisar o impacto de diferentes tamanhos de consultas (range queries)
- Fornecer dados empíricos sobre a escalabilidade das estruturas

## 🏗️ Estrutura do Projeto

```
Otimizacao-de-Consultas-Espaciais/
├── Linear.java           # Implementação de busca linear (baseline)
├── QuadTree.java         # Implementação de QuadTree
├── RStarTree.java        # Implementação de R*-Tree otimizada
├── Point.java            # Classe para representar pontos 2D
├── Rectangle.java        # Classe para representar retângulos e MBRs
├── Node.java             # Classe nó para R*-Tree
├── Entry.java            # Classe entrada para R*-Tree
└── TestComparativo.java  # Teste comparativo e geração de resultados
```

## 🔬 Metodologia

### Cenários de Teste

O teste comparativo avalia as estruturas em diferentes configurações:

- **Tamanhos de Dataset**: 10.000, 50.000, 100.000, 250.000, 500.000, 1.000.000, 2.000.000, 5.000.000 e 10.000.000 pontos
- **Distribuições de Dados**:
  - **Uniforme**: Pontos distribuídos aleatoriamente no espaço
  - **Clusterizada**: Pontos agrupados em 5 clusters principais
- **Frações de Consulta**: 1%, 5% e 20% do espaço total (1000x1000)
- **Repetições**: 100 execuções por configuração para garantir estatística confiável

### Métricas Coletadas

Para cada configuração, o sistema coleta:
- Tempo médio de execução (ms)
- Desvio padrão do tempo (ms)

Os resultados são exportados em formato CSV para análise posterior.

## 🚀 Como Executar

### Pré-requisitos

- Java JDK 8 ou superior
- Compilador Java

### Compilação

Compile todos os arquivos Java:

```bash
javac *.java
```

### Execução

Execute o teste comparativo:

```bash
java TestComparativo
```

### Saída

O programa gera resultados no formato CSV, separado por ponto e vírgula (`;`), com a seguinte estrutura:

```
Cenario;N;QueryFrac;Linear_Avg(ms);Linear_Std(ms);Quad_Avg(ms);Quad_Std(ms);RTree_Avg(ms);RTree_Std(ms)
```

Você pode redirecionar a saída para um arquivo:

```bash
java TestComparativo > resultados.csv
```

## 📊 Principais Componentes

### R*-Tree Otimizada

A implementação de R*-Tree inclui várias otimizações:

- **Forced Reinsert**: Redistribui entradas antes de fazer split para melhorar a qualidade da árvore
- **Split Otimizado**: Escolha inteligente do eixo de split minimizando overlap e área total
- **Atualização Incremental de MBR**: Evita recálculos desnecessários
- **Otimização de Consultas**: Detecta quando um nó está completamente contido na query para evitar verificações desnecessárias

### QuadTree

Implementação de QuadTree com:
- Capacidade configurável por nó (padrão: 16 pontos)
- Limite de profundidade máxima (padrão: 12 níveis)
- Otimização para consultas que contêm completamente um nó

## 📈 Análise de Resultados

Os resultados podem ser analisados para identificar:

- **Escalabilidade**: Como cada estrutura se comporta com o aumento do número de pontos
- **Impacto da Distribuição**: Diferenças entre dados uniformes e clusterizados
- **Impacto do Tamanho da Query**: Eficiência relativa para consultas pequenas vs grandes
- **Trade-offs**: Quando cada estrutura é mais apropriada

## 🔧 Parâmetros Configuráveis

No arquivo `TestComparativo.java` você pode ajustar:

- `REPS`: Número de repetições (padrão: 100)
- `SIZES`: Array com tamanhos de dataset a serem testados
- `SPACE`: Tamanho do espaço bidimensional (padrão: 1000.0)
- `fracs`: Frações de consulta (padrão: 0.01, 0.05, 0.2)

Na `RStarTree.java`:
- `maxEntries`: Máximo de entradas por nó (padrão: 16)
- `REINSERT_PCT`: Percentual de entradas para reinsert (padrão: 0.3)

Na `QuadTree.java`:
- `CAPACITY`: Capacidade máxima por nó (padrão: 16)
- `MAX_DEPTH`: Profundidade máxima (padrão: 12)

## 📚 Referências

Este projeto foi desenvolvido para o artigo científico:
**"Otimização de Consultas Espaciais: Uma Análise da Eficiência de Árvores R*"**

## 📝 Notas Técnicas

- A R*-Tree implementa o algoritmo de split otimizado que minimiza overlap entre nós filhos
- O sistema de coordenadas utiliza valores do tipo `double` para precisão
- Todos os testes utilizam uma semente fixa (`Random(12345)`) para garantir reprodutibilidade
- A implementação prioriza correção dos resultados em relação à otimização extrema de performance

## 🤝 Contribuições

Este é um projeto acadêmico. Para sugestões ou correções, sinta-se à vontade para abrir uma issue ou pull request.

## 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos e de pesquisa.

---

**Projeto**: Otimização de Consultas Espaciais  
**Data**: 2025

