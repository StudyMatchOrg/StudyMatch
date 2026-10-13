# Especificação de Casos de Uso

Este documento descreve o modelo inicial de casos de uso do StudyMatch. Os casos de uso
cobrem as áreas pedidas no enunciado (identidade do estudante, percurso académico,
competências, perfil do estudante, cold start e contexto de agrupamento) e incluem
propostas da equipa (secção "Casos de uso propostos pela equipa"). 
---

| Ator | Descrição |
|---|---|
| **Utilizador** | Qualquer pessoa com conta no sistema. Generaliza os restantes atores. |
| **Admin** | Gere contas e a estrutura académica (cursos e cadeiras). |
| **Professor** | Gere a informação académica dos estudantes, consulta perfis e forma grupos. |
---

## Resumo

| # | Caso de uso | Ator principal |
|---|---|---|---|
| 1 | Registar Contas | Admin |
| 2 | Efetuar Login | Utilizador |
| 3 | Registar Cursos | Admin |
| 4 | Ver Cursos | Utilizador |
| 5 | Registar Cadeiras | Admin |
| 6 | Ver Cadeiras | Utilizador |
| 7 | Exportar Grupos | Professor |
| 8 | Formar Grupos | Professor |
| 9 | Gerir Estudantes | Professor |
| 10 | Registar Resultado Académico | Professor |
| 11 | Gerir Competências das Cadeiras | Admin |
| 12 | Consultar Perfil do Estudante | Professor|
| 13 | Registar Avaliação Inicial | Professor |
| 14 | Definir Contexto de Agrupamento | Professor |
| 15 | Importar Histórico Académico | Professor |
---

## 1. Registar Contas

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Admin |
| **Descrição** | 1. O caso de uso inicia quando o Admin seleciona a opção *Registar Conta*.<br>2. O Sistema lê os tipos de conta.<br>3. O Sistema apresenta os tipos de conta.<br>4. O Admin seleciona o tipo de conta.<br>5. O Sistema pede as informações do Utilizador da nova conta.<br>6. O Admin insere as informações do Utilizador.<br>7. O Sistema regista o Utilizador. |
| **Cenário Alternativo** | **As credenciais do novo utilizador já estão em uso:**<br>1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Só o Admin pode registar contas.<br>• O email (ou username) é único.<br>• Os campos obrigatórios têm de estar preenchidos.<br>• O tipo de conta tem de ser um dos existentes.<br>• A palavra-passe nunca é guardada em texto simples. |
| **Conceitos do Domínio** | Tipo de Conta, Admin, Professor, Credenciais. |

---

## 2. Efetuar Login

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Utilizador |
| **Descrição** | 1. O caso de uso inicia quando o Utilizador seleciona a opção *Login*.<br>2. O Sistema pede as credenciais do Utilizador.<br>3. O Utilizador insere as credenciais.<br>4. O Sistema autentica o Utilizador. |
| **Cenário Alternativo** | **O Utilizador não se encontra registado no Sistema ou a palavra-passe está errada:**<br>1. O sistema apresenta um aviso|
| **Regras de Negócio / Restrições** | • O acesso às funcionalidades depende do tipo de conta. |
| **Conceitos do Domínio** | Credenciais, Tipo de Conta. |

---

## 3. Registar Cursos

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Admin |
| **Descrição** | 1. O caso de uso inicia quando o Utilizador seleciona a opção *Registar Curso*.<br>2. O Sistema pede os detalhes do Curso.<br>3. O Utilizador insere os detalhes do Curso.<br>4. O Sistema regista o Curso. |
| **Cenário Alternativo** | **As credenciais do curso já estão em uso:**<br>1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Só o Admin pode registar cursos.<br>• O código/nome do curso é único.<br>• Nome e código são obrigatórios. |
| **Conceitos do Domínio** | Curso |

---

## 4. Ver Cursos

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Utilizador |
| **Descrição** | 1. O caso de uso inicia quando o Utilizador seleciona a opção *Ver Cursos*.<br>2. O Sistema lê os Cursos registados.<br>3. O Sistema apresenta os cursos. |
| **Cenário Alternativo** | **Não existem cursos registados:**<br>1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Qualquer utilizador autenticado pode consultar.<br>• A operação é só de leitura. |
| **Conceitos do Domínio** | Curso |

---

## 5. Registar Cadeiras

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Admin |
| **Descrição** | 1. O caso de uso inicia quando o Admin seleciona a opção *Registar Cadeira*.<br>2. O Sistema pede os detalhes da Cadeira.<br>3. O Admin insere os detalhes da Cadeira (código, nome, ECTS, ano e semestre curricular, cursos).<br>4. O Sistema regista a Cadeira. |
| **Cenário Alternativo** | **O código da cadeira já está em uso:**<br>1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Só o Admin pode registar cadeiras.<br>• O código da cadeira é único.<br>• Cada cadeira pertence a pelo menos um curso. |
| **Conceitos do Domínio** | Cadeira, Curso |

---

## 6. Ver Cadeiras

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Utilizador |
| **Descrição** | 1. O caso de uso inicia quando o Utilizador seleciona a opção *Ver Cadeiras*.<br>2. O Sistema lê as Cadeiras registadas.<br>3. O Sistema apresenta as Cadeiras. |
| **Cenário Alternativo** | **Não existem cadeiras registadas:**<br>1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Qualquer utilizador autenticado pode consultar.<br>• A listagem pode ser filtrada por curso.<br>• A operação é só de leitura. |
| **Conceitos do Domínio** | Cadeira, Curso |

---

## 7. Exportar Grupos

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o Utilizador seleciona a opção *Exportar Grupos*.<br>2. O Sistema lê os conjuntos de grupos já realizados.<br>3. O Sistema apresenta os conjuntos.<br>4. O Professor seleciona o conjunto.<br>5. O sistema exporta o ficheiro. |
| **Cenário Alternativo** | **Não existem conjuntos registados:**<br>1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Só o Professor pode exportar, e apenas os conjuntos que criou.<br>• O formato é fixo (por exemplo, CSV).<br>• O ficheiro inclui cadeira, nº do grupo e alunos. |
| **Conceitos do Domínio** | *Nenhum especificado* |

---

## 8. Formar Grupos

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o professor seleciona a opção *Formar Grupos*.<br>2. O sistema pede as informações dos grupos.<br>3. O professor insere as informações.<br>4. O sistema lê as informações.<br>5. O Sistema forma os grupos.<br>6. O Sistema apresenta os grupos.<br>7. O Sistema guarda os grupos. |
| **Cenário Alternativo** | **1. Não existem alunos registados:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso.<br>**2. As informações são inválidas:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Só o Professor pode formar grupos.<br>• É obrigatório indicar a cadeira (contexto) e o tamanho do grupo.<br>• O tamanho do grupo tem de ser $\ge 2$ e $\le$ nº de alunos.<br>• Cada aluno fica em exatamente um grupo por conjunto.<br>• Só entram alunos da cadeira escolhida.<br>• O critério de formação não está definido neste sprint (o enunciado deixa-o em aberto).<br>• Os grupos ficam guardados como um conjunto. |
| **Conceitos do Domínio** | Grupo, Conjunto de Grupos |

---

## 9. Gerir Estudantes

| Campo | Detalhes |
| :--- | :--- |
| **Objetivo** | Manter a informação que identifica cada estudante e o seu contexto académico. |
| **Ator** | Admin |
| **Descrição** | 1. O caso de uso inicia quando o Admin seleciona a opção *Estudantes*.<br>2. O Sistema apresenta a lista de estudantes.<br>3. O Admin escolhe criar um estudante ou editar um existente.<br>4. O Sistema pede os dados do estudante (número, nome, email, curso, ano de entrada).<br>5. O Admin insere ou altera os dados.<br>6. O Sistema valida e guarda o estudante. |
| **Cenário Alternativo** | **1. O número de estudante ou o email já existem:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso e não guarda.<br>**2. Faltam campos obrigatórios ou o email é inválido:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema indica os campos com erro. |
| **Regras de Negócio / Restrições** | • O número de estudante e o email são únicos.<br>• Cada estudante pertence a um curso existente.<br>• Os dados ficam guardados entre execuções. |
| **Conceitos do Domínio** | Estudante, Curso |

---

## 10. Registar Resultado Académico

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o Professor abre o histórico académico de um estudante.<br>2. O Sistema apresenta os registos existentes, por ano letivo e semestre.<br>3. O Professor escolhe adicionar um registo.<br>4. O Sistema pede a cadeira, o ano letivo, o semestre, o estado e a nota.<br>5. O Professor insere os dados.<br>6. O Sistema guarda o registo e atualiza o perfil do estudante. |
| **Cenário Alternativo** | **1. A nota está fora do intervalo 0–20:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso.<br>**|
| **Regras de Negócio / Restrições** | • Um estudante pode ter vários registos da mesma cadeira (repetição ou melhoria de nota).<br>• Aprovado ⇒ nota ≥ 10; reprovado ⇒ nota < 10; inscrito ⇒ sem nota.<br>• A cadeira tem de pertencer ao curso do estudante. |
| **Conceitos do Domínio** | Registo Académico, Cadeira, Estudante |

---

## 11. Gerir Competências das Cadeiras

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o Professor seleciona a opção *Competências*.<br>2. O Sistema apresenta as competências existentes (ex.: Programação, Bases de Dados, Resolução de Problemas).<br>3. O Professor cria uma competência ou seleciona uma cadeira.<br>4. Para a cadeira, o Professor associa competências e indica o peso de cada uma (0 a 1).<br>5. O Sistema guarda as associações. |
| **Cenário Alternativo** | **1. O peso está fora do intervalo 0–1:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso.<br>**2. A competência já está associada à cadeira:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • As competências são configuráveis, não fixas no código.<br>• Uma cadeira pode desenvolver várias competências, e uma competência pode vir de várias cadeiras.<br>• Alterar os pesos atualiza os perfis calculados. |
| **Conceitos do Domínio** | Competência, Cadeira, Peso da Competência |

---

## 12. Consultar Perfil do Estudante

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o Professor seleciona um estudante.<br>2. O Sistema apresenta o perfil do estudante.|
| **Cenário Alternativo** ||
| **Regras de Negócio / Restrições** ||
| **Conceitos do Domínio** | Perfil do Estudante,Registo Académico|

---

### 13. Pesquisar e Filtrar Estudantes

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Utilizador |
| **Descrição** | 1. O caso de uso inicia quando o Utilizador abre a lista de estudantes.<br>2. O Sistema apresenta os campos de pesquisa e os filtros.<br>3. O Utilizador escreve um nome ou número, e/ou escolhe filtros: curso, ano de entrada, cadeira em que está inscrito, nível mínimo numa competência, ou "perfil com pouca evidência".<br>4. O Sistema apresenta os estudantes que cumprem todos os critérios, com o número de resultados.<br>5. O Utilizador pode ordenar os resultados (por nome, número ou nível numa competência). |
| **Cenário Alternativo** | **Nenhum estudante cumpre os critérios:**<br>1. O sistema indica que não há resultados e sugere remover filtros. |
| **Regras de Negócio / Restrições** | • A pesquisa por nome ignora maiúsculas e acentos ("joao" encontra "João").<br>• Os filtros combinam-se (todos têm de ser cumpridos).<br>• A operação é só de leitura. |
| **Conceitos do Domínio** | Estudante, Curso, Cadeira, Perfil do Estudante, Pontuação de Competência |

## Casos de uso propostos pela equipa

## 1. Exportar Grupos

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o Professor seleciona a opção *Exportar Grupos*.<br>2. O Sistema lê os conjuntos de grupos já realizados.<br>3. O Sistema apresenta os conjuntos.<br>4. O Professor seleciona o conjunto.<br>5. O sistema exporta o ficheiro. |
| **Cenário Alternativo** | **Não existem conjuntos registados:**<br>1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Só o Professor pode exportar, e apenas os conjuntos que criou.<br>• O formato é fixo (por exemplo, CSV).<br>• O ficheiro inclui cadeira, nº do grupo e alunos. |
| **Conceitos do Domínio** | Conjunto de Grupos, Grupo |

---

### 2. Pesquisar e Filtrar Estudantes

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Utilizador |
| **Descrição** | 1. O caso de uso inicia quando o Utilizador abre a lista de estudantes.<br>2. O Sistema apresenta os campos de pesquisa e os filtros.<br>3. O Utilizador escreve um nome ou número, e/ou escolhe filtros: curso, ano de entrada, cadeira em que está inscrito, nível mínimo numa competência, ou "perfil com pouca evidência".<br>4. O Sistema apresenta os estudantes que cumprem todos os critérios, com o número de resultados.<br>5. O Utilizador pode ordenar os resultados (por nome, número ou nível numa competência). |
| **Cenário Alternativo** | **Nenhum estudante cumpre os critérios:**<br>1. O sistema indica que não há resultados e sugere remover filtros. |
| **Regras de Negócio / Restrições** | • A pesquisa por nome ignora maiúsculas e acentos ("joao" encontra "João").<br>• Os filtros combinam-se (todos têm de ser cumpridos).<br>• A operação é só de leitura. |
| **Conceitos do Domínio** | Estudante, Curso, Cadeira, Perfil do Estudante, Pontuação de Competência |