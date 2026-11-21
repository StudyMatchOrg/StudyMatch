# Especificação de Casos de Uso

Este documento descreve o modelo de casos de uso do StudyMatch. Os casos de uso cobrem as
áreas pedidas no enunciado (identidade do estudante, percurso académico, competências,
perfil do estudante, cold start e contexto de agrupamento) e incluem propostas da equipa
(secção "Casos de uso propostos pela equipa").

---

| Ator | Descrição |
|---|---|
| **Utilizador** | Qualquer pessoa com conta no sistema. Generaliza os restantes atores. |
| **Admin** | Gere contas, a estrutura académica (cursos e cadeiras) e os estudantes. Pode também fazer tudo o que o Professor faz. |
| **Professor** | Gere a informação académica dos estudantes, consulta perfis, define projetos e forma grupos. |

---

## Resumo

| # | Caso de uso | Ator principal |
|---|---|---|
| 1 | Registar Contas | Admin |
| 2 | Efetuar Login | Utilizador |
| 3 | Registar Cursos | Admin |
| 4 | Ver Cursos | Utilizador |
| 5 | Registar Cadeiras | Admin |
| 6 | Ver Cadeiras | Utilizador |
| 7 | Exportar Grupos | Professor |
| 8 | Formar Grupos | Professor |
| 9 | Gerir Estudantes | Admin |
| 10 | Registar Resultado Académico | Professor |
| 11 | Gerir Competências das Cadeiras | Professor |
| 12 | Consultar Perfil do Estudante | Professor |
| 13 | Registar Avaliação Inicial | Professor |
| 14 | Definir Projeto (Contexto de Agrupamento) | Professor |
| 15 | Importar Histórico Académico *(proposta da equipa)* | Professor |
| 16 | Pesquisar e Filtrar Estudantes *(proposta da equipa)* | Utilizador |

---

## 1. Registar Contas

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Admin |
| **Descrição** | 1. O caso de uso inicia quando o Admin seleciona a opção *Registar Conta*.<br>2. O Sistema lê os tipos de conta.<br>3. O Sistema apresenta os tipos de conta.<br>4. O Admin seleciona o tipo de conta.<br>5. O Sistema pede as informações do Utilizador da nova conta.<br>6. O Admin insere as informações do Utilizador.<br>7. O Sistema regista o Utilizador. |
| **Cenário Alternativo** | **As credenciais do novo utilizador já estão em uso:**<br>1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Só o Admin pode registar contas.<br>• O username é único e tem 3 a 30 caracteres (letras minúsculas, dígitos, ".", "_" ou "-").<br>• Os campos obrigatórios têm de estar preenchidos.<br>• O tipo de conta é ADMIN ou TEACHER (Professor).<br>• A palavra-passe tem pelo menos 8 caracteres e nunca é guardada em texto simples, apenas o seu hash (BCrypt). |
| **Conceitos do Domínio** | Conta, Tipo de Conta, Admin, Professor, Credenciais. |

---

## 2. Efetuar Login

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Utilizador |
| **Descrição** | 1. O caso de uso inicia quando o Utilizador abre a aplicação sem sessão iniciada.<br>2. O Sistema pede as credenciais do Utilizador.<br>3. O Utilizador insere o username e a palavra-passe.<br>4. O Sistema autentica o Utilizador e mostra as funcionalidades do seu tipo de conta. |
| **Cenário Alternativo** | **1. O Utilizador não se encontra registado no Sistema ou a palavra-passe está errada:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta o mesmo aviso nos dois casos ("username ou palavra-passe inválidos"), para não revelar que contas existem.<br>**2. A sessão expirou:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema volta a pedir as credenciais. |
| **Regras de Negócio / Restrições** | • O acesso às funcionalidades depende do tipo de conta: o Admin pode fazer tudo o que o Professor faz.<br>• Sem sessão iniciada só é possível fazer login.<br>• A sessão dura 8 horas. |
| **Conceitos do Domínio** | Credenciais, Tipo de Conta. |

---

## 3. Registar Cursos

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Admin |
| **Descrição** | 1. O caso de uso inicia quando o Admin seleciona a opção *Registar Curso*.<br>2. O Sistema pede os detalhes do Curso.<br>3. O Admin insere os detalhes do Curso.<br>4. O Sistema regista o Curso. |
| **Cenário Alternativo** | **O código ou o nome do curso já estão em uso:**<br>1. O sistema apresenta um aviso. |
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
| **Regras de Negócio / Restrições** | • Só o Admin pode registar cadeiras.<br>• O código da cadeira é único.<br>• Cada cadeira pertence a pelo menos um curso.<br>• Os ECTS são positivos e o semestre é 1 ou 2. |
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
| **Descrição** | 1. O caso de uso inicia quando o Professor abre um conjunto de grupos de um dos seus projetos (UC8).<br>2. O Professor seleciona *Export CSV*.<br>3. O Sistema gera e descarrega o ficheiro. |
| **Cenário Alternativo** | **O conjunto não existe ou pertence a um projeto de outro professor:**<br>1. O sistema indica que o conjunto não existe. |
| **Regras de Negócio / Restrições** | • Só o dono do projeto (ou um Admin) pode exportar os seus conjuntos.<br>• O ficheiro é CSV separado por ";" e em UTF-8, para abrir diretamente no Excel em português.<br>• Uma linha por estudante: projeto, data da geração, nº do grupo, nº de estudante, nome, email e competência mais forte.<br>• Valores que uma folha de cálculo executaria como fórmula (começados por =, +, - ou @) são escritos com um apóstrofo antes, para não serem executados. |
| **Conceitos do Domínio** | Conjunto de Grupos, Grupo |

---

## 8. Formar Grupos

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o Professor abre um dos seus projetos (UC14) e seleciona *Generate groups*.<br>2. O Professor escolhe a estratégia: equilibrada e complementar (por omissão) ou aleatória.<br>3. O Sistema calcula o perfil atual de cada estudante elegível (UC12).<br>4. O Sistema decide quantos grupos formar e de que tamanho.<br>5. O Sistema forma os grupos.<br>6. O Sistema guarda os grupos como um novo conjunto, com a data e hora da geração.<br>7. O Sistema apresenta os grupos e explica cada um: membros, força do grupo em cada competência relevante e quem a assegura, pontos fortes e lacunas, e a comparação com grupos aleatórios. |
| **Cenário Alternativo** | **1. O projeto tem menos de 2 estudantes elegíveis:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema não forma grupos e apresenta um aviso.<br>**2. Os estudantes não se dividem no tamanho escolhido (ex.: 11 estudantes em grupos de 5):**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema forma os grupos mais próximos possível (4 + 4 + 3) e guarda e mostra um aviso.<br>**3. Há estudantes sem notas (cold start):**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema usa a estimativa do professor e a média da turma, e assinala esses estudantes. |
| **Regras de Negócio / Restrições** | • Todos os estudantes elegíveis são considerados, e cada um fica em exatamente um grupo de cada conjunto.<br>• O mesmo estudante não pode ser escolhido duas vezes.<br>• Número de grupos = ⌈estudantes / tamanho⌉; os tamanhos diferem no máximo em 1. Com tamanho 5, os grupos têm 4 ou 5 estudantes sempre que possível (23 estudantes: 5 + 5 + 5 + 4 + 4).<br>• Nenhum estudante fica sozinho num grupo.<br>• Todas as gerações ficam guardadas e podem ser consultadas mais tarde, mesmo depois de mudarem os perfis ou o projeto.<br>• A mesma informação e a mesma semente dão os mesmos grupos. |
| **Conceitos do Domínio** | Projeto (Contexto de Agrupamento), Conjunto de Grupos, Grupo, Perfil do Estudante |

> **Nota:** A estratégia está explicada e justificada em [sprint-3-design.md](sprint-3-design.md).

---

## 9. Gerir Estudantes

| Campo | Detalhes |
| :--- | :--- |
| **Objetivo** | Manter a informação que identifica cada estudante e o seu contexto académico. |
| **Ator** | Admin |
| **Descrição** | 1. O caso de uso inicia quando o Admin seleciona a opção *Estudantes*.<br>2. O Sistema apresenta a lista de estudantes.<br>3. O Admin escolhe criar um estudante, editar um existente ou apagá-lo.<br>4. O Sistema pede os dados do estudante (número, nome, email, curso, ano de entrada).<br>5. O Admin insere ou altera os dados.<br>6. O Sistema valida e guarda o estudante. |
| **Cenário Alternativo** | **1. O número de estudante ou o email já existem:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso e não guarda.<br>**2. Faltam campos obrigatórios ou o email é inválido:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema indica os campos com erro.<br>**3. O Admin tenta apagar um estudante que está em grupos guardados:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema recusa, para que os grupos anteriores continuem completos. |
| **Regras de Negócio / Restrições** | • O número de estudante e o email são únicos.<br>• Cada estudante pertence a um curso existente.<br>• O ano de entrada não pode ser posterior ao ano atual.<br>• Os dados ficam guardados entre execuções.<br>• Apagar um estudante apaga também o seu histórico académico, as avaliações iniciais e a sua participação em projetos. |
| **Conceitos do Domínio** | Estudante, Curso |

---

## 10. Registar Resultado Académico

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o Professor abre o histórico académico de um estudante.<br>2. O Sistema apresenta os registos existentes, por ano letivo e semestre.<br>3. O Professor escolhe adicionar um registo, ou alterar ou apagar um existente.<br>4. O Sistema pede a cadeira, o ano letivo, o semestre, o estado e a nota.<br>5. O Professor insere os dados.<br>6. O Sistema guarda o registo e atualiza o perfil do estudante. |
| **Cenário Alternativo** | **1. A nota está fora do intervalo 0–20 ou não corresponde ao estado:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso.<br>**2. Já existe um registo da mesma cadeira no mesmo ano letivo e semestre:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Um estudante pode ter vários registos da mesma cadeira (repetição ou melhoria de nota).<br>• Aprovado ⇒ nota ≥ 10; reprovado ⇒ nota < 10; inscrito ⇒ sem nota.<br>• A cadeira tem de pertencer ao curso do estudante.<br>• O ano letivo tem a forma 2025/2026. |
| **Conceitos do Domínio** | Registo Académico, Cadeira, Estudante |

---

## 11. Gerir Competências das Cadeiras

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o Professor seleciona a opção *Competências*.<br>2. O Sistema apresenta as competências existentes (ex.: Programação, Bases de Dados, Resolução de Problemas).<br>3. O Professor cria uma competência ou seleciona uma cadeira.<br>4. Para a cadeira, o Professor associa competências e indica o peso de cada uma (maior que 0 e no máximo 1).<br>5. O Sistema guarda as associações. |
| **Cenário Alternativo** | **1. O peso está fora do intervalo:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso.<br>**2. A competência já está associada à cadeira:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • As competências são configuráveis, não fixas no código.<br>• Uma cadeira pode desenvolver várias competências, e uma competência pode vir de várias cadeiras.<br>• Alterar os pesos atualiza os perfis calculados. |
| **Conceitos do Domínio** | Competência, Cadeira, Peso da Competência |

> **Nota:** Desde o Sprint 3 as competências e os pesos são editados no ecrã *Competencies*: criar e renomear competências, associar uma cadeira com um peso, mudar o peso e remover a associação. O código da competência não muda.

---

## 12. Consultar Perfil do Estudante

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o Professor seleciona um estudante.<br>2. O Sistema calcula o perfil a partir dos registos académicos e das avaliações iniciais.<br>3. O Sistema apresenta, para cada competência, a pontuação (0 a 1), a confiança e a origem da evidência (notas, estimativa do professor ou ambas).<br>4. O Professor seleciona uma competência.<br>5. O Sistema mostra cada evidência que contribuiu para a pontuação, com o seu peso e a sua percentagem. |
| **Cenário Alternativo** | **Não existe nenhuma evidência para uma competência:**<br>1. O sistema mostra essa competência como "desconhecida", e não como 0. |
| **Regras de Negócio / Restrições** | • De cada cadeira conta apenas a melhor nota. |
| **Conceitos do Domínio** | Perfil do Estudante, Pontuação de Competência, Evidência, Registo Académico, Avaliação Inicial |

> **Nota:** O perfil é calculado sempre que é pedido, nunca introduzido à mão, por isso está sempre atualizado. A fórmula está documentada em [sprint-2-design.md](sprint-2-design.md).

---

## 13. Registar Avaliação Inicial (cold start)

| Campo | Detalhes |
| :--- | :--- |
| **Objetivo** | Dar um perfil inicial útil a estudantes com pouco ou nenhum histórico académico. |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o Professor abre um estudante com pouca ou nenhuma evidência académica.<br>2. O Sistema apresenta as competências e a avaliação inicial atual, se existir.<br>3. O Professor indica uma estimativa de 0 a 1 para as competências que conseguiu observar, e deixa vazias as restantes.<br>4. O Sistema guarda a avaliação com a data e atualiza o perfil. |
| **Cenário Alternativo** | **1. O valor está fora do intervalo 0–1:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso e não guarda.<br>**2. O Professor apaga a estimativa de uma competência:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. Essa competência deixa de ter avaliação inicial. |
| **Regras de Negócio / Restrições** | • Há no máximo uma avaliação inicial por estudante e competência.<br>• Uma estimativa vale tanto como uma cadeira de 6 ECTS totalmente dedicada à competência, por isso o seu peso no perfil diminui à medida que surgem notas.<br>• As competências sem estimativa nem notas continuam "desconhecidas". |
| **Conceitos do Domínio** | Avaliação Inicial, Competência, Estudante, Evidência |

---

## 14. Definir Projeto (Contexto de Agrupamento)

| Campo | Detalhes |
| :--- | :--- |
| **Objetivo** | Definir a situação em que os grupos vão ser formados: um projeto de uma cadeira, um trabalho, uma aula prática... |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o Professor seleciona *New project*.<br>2. O Sistema pede o nome, uma descrição, a cadeira e o ano letivo (opcionais), o tamanho dos grupos, os estudantes e as competências relevantes.<br>3. O Professor pode carregar os estudantes inscritos na cadeira nesse ano letivo, e acrescentar ou retirar estudantes.<br>4. O Professor indica a importância de cada competência relevante (de 0.1 a 1).<br>5. O Sistema guarda o projeto e mostra quantos estudantes tem e os grupos que vão resultar (ex.: 5 + 5 + 5 + 4 + 4). |
| **Cenário Alternativo** | **1. O tamanho do grupo está fora de 2 a 10, falta o nome ou nenhuma competência é relevante:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso e não guarda.<br>**2. Um estudante foi escolhido duas vezes:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema apresenta um aviso e não guarda.<br>**3. Os estudantes não se dividem no tamanho escolhido:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema guarda e avisa que alguns grupos vão ter tamanhos diferentes. |
| **Regras de Negócio / Restrições** | • O tamanho dos grupos é escolhido pelo Professor para cada projeto, entre 2 e 10 (5 por omissão).<br>• Um projeto pertence ao Professor que o criou: os outros professores não o veem (um Admin vê todos).<br>• A importância de cada competência é maior que 0 e no máximo 1.<br>• Um projeto pode originar vários conjuntos de grupos (UC8); mudar o projeto não altera os conjuntos já gerados. |
| **Conceitos do Domínio** | Projeto (Contexto de Agrupamento), Competência Relevante, Estudante, Cadeira |

---

## Casos de uso propostos pela equipa

### 15. Importar Histórico Académico

| Campo | Detalhes |
| :--- | :--- |
| **Objetivo** | Carregar resultados académicos a partir de um ficheiro, em vez de os introduzir um a um. |
| **Ator** | Professor |
| **Descrição** | 1. O caso de uso inicia quando o Professor seleciona *Import*.<br>2. O Sistema mostra o formato esperado do ficheiro CSV (número de estudante, cadeira, ano letivo, semestre, estado, nota) e um ficheiro de exemplo.<br>3. O Professor escolhe o ficheiro.<br>4. O Sistema valida e guarda cada linha com as mesmas regras do UC10.<br>5. O Sistema apresenta um resumo: linhas importadas e linhas rejeitadas, com o número da linha e o motivo. |
| **Cenário Alternativo** | **1. O ficheiro está vazio ou não tem as colunas necessárias:**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema rejeita o ficheiro e indica o problema.<br>**2. Algumas linhas são inválidas (nota fora do intervalo, estudante ou cadeira inexistente, registo repetido):**<br>&nbsp;&nbsp;&nbsp;&nbsp;1. O sistema ignora essas linhas e lista-as no resumo; as restantes são importadas. |
| **Regras de Negócio / Restrições** | • Os estudantes e as cadeiras têm de existir; a importação só cria registos académicos.<br>• As colunas podem estar separadas por vírgulas ou por ponto e vírgula (Excel em português), em qualquer ordem.<br>• Uma linha inválida nunca impede a importação das outras. |
| **Conceitos do Domínio** | Registo Académico, Estudante, Cadeira |

**Porque acrescenta valor:** numa turma real há dezenas de estudantes com vários anos de
notas. Introduzir tudo à mão é lento e propenso a erros, o que tornaria o StudyMatch pouco
prático. A importação torna o produto utilizável com dados reais e mostra claramente as
linhas com problemas, o que melhora a qualidade dos dados.

---

### 16. Pesquisar e Filtrar Estudantes

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Utilizador |
| **Descrição** | 1. O caso de uso inicia quando o Utilizador abre a lista de estudantes.<br>2. O Sistema apresenta os campos de pesquisa e os filtros.<br>3. O Utilizador escreve um nome ou número, e/ou escolhe filtros: curso, ano de entrada, cadeira em que está inscrito, nível mínimo numa competência, ou "perfil com pouca evidência".<br>4. O Sistema apresenta os estudantes que cumprem todos os critérios, com o número de resultados.<br>5. O Utilizador pode ordenar os resultados (por nome, número ou nível numa competência). |
| **Cenário Alternativo** | **Nenhum estudante cumpre os critérios:**<br>1. O sistema indica que não há resultados e sugere remover filtros. |
| **Regras de Negócio / Restrições** | • A pesquisa por nome ignora maiúsculas e acentos ("joao" encontra "João").<br>• Os filtros combinam-se (todos têm de ser cumpridos).<br>• "Perfil com pouca evidência" significa um estudante ainda sem notas (só estimativas do professor ou nada).<br>• O nível mínimo e a ordenação por nível exigem uma competência escolhida.<br>• A operação é só de leitura. |
| **Conceitos do Domínio** | Estudante, Curso, Cadeira, Perfil do Estudante, Pontuação de Competência |

**Porque acrescenta valor:** com dezenas ou centenas de estudantes, uma lista simples deixa
de ser útil. O filtro "perfil com pouca evidência" mostra logo quem precisa de uma avaliação
inicial (UC13), e o filtro por competência ajuda a preparar a formação de grupos.
