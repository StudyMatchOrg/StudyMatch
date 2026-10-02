# Especificação de Casos de Uso

---

## 1. Registar Contas

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Admin |
| **Descrição** | 1. O caso de uso inicia quando o Admin seleciona a opção *Registar Conta*.<br>2. O Sistema lê os tipos de conta.<br>3. O Sistema apresenta os tipos de conta.<br>4. O Admin seleciona o tipo de conta.<br>5. O Sistema pede as informações do Utilizador da nova conta.<br>6. O Utilizador insere as suas informações.<br>7. O Sistema regista o Utilizador. |
| **Cenário Alternativo** | **As credenciais do novo utilizador já estão em uso:**<br>1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Só o Admin pode registar contas.<br>• O email (ou username) é único.<br>• Os campos obrigatórios têm de estar preenchidos.<br>• O tipo de conta tem de ser um dos existentes. |
| **Conceitos do Domínio** | Tipo de Conta, Admin, Professor, Credenciais. |

---

## 2. Efetuar Login

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Utilizador |
| **Descrição** | 1. O caso de uso inicia quando o Utilizador seleciona a opção *Login*.<br>2. O Sistema pede as credenciais do Utilizador.<br>3. O Utilizador insere as credenciais.<br>4. O Sistema autentica o Utilizador. |
| **Cenário Alternativo** | **O Utilizador não se encontra registado no Sistema:**<br>1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • O acesso às funcionalidades depende do tipo de conta. |
| **Conceitos do Domínio** | *Nenhum especificado* |

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
| **Conceitos do Domínio** | *Nenhum especificado* |

---

## 5. Registar Cadeiras

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Admin |
| **Descrição** | 1. O caso de uso inicia quando o Utilizador seleciona a opção *Registar Cadeira*.<br>2. O Sistema pede os detalhes da Cadeira.<br>3. O Utilizador insere os detalhes da Cadeira.<br>4. O Sistema regista a Cadeira. |
| **Cenário Alternativo** | **As credenciais da cadeira já estão em uso:**<br>1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Só o Admin pode registar cadeiras.<br>• O código da cadeira é único.<br>• Cada cadeira pertence a pelo menos um curso. |
| **Conceitos do Domínio** | Cadeira |

---

## 6. Ver Cadeiras

| Campo | Detalhes |
| :--- | :--- |
| **Ator** | Utilizador |
| **Descrição** | 1. O caso de uso inicia quando o Utilizador seleciona a opção *Ver Cadeiras*.<br>2. O Sistema lê as Cadeiras registadas.<br>3. O Sistema apresenta as Cadeiras. |
| **Cenário Alternativo** | **Não existem cadeiras registadas:**<br>1. O sistema apresenta um aviso. |
| **Regras de Negócio / Restrições** | • Qualquer utilizador autenticado pode consultar.<br>• A listagem pode ser filtrada por curso.<br>• A operação é só de leitura. |
| **Conceitos do Domínio** | *Nenhum especificado* |

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