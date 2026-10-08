**ENTIDADES:**

*Pessoa:*

* *id* (PK)
* *nome* - (value-concepts: "Miguel Leitão"; "Guilherme Barros")
* *email* - (value-concepts: "51631@alunos.upt.pt"; "51634@alunos.upt.pt")
* *password* - (value-concepts: "du23yjhih2f27ify2kh2g27"; "d8h23hjgyh8ugffyfhf2ydg2h")
* *tipo* - (value-concepts: "estudante"; "docente")

*Competencia:*

* id (PK)
* nome - (value-concepts: "Java"; "SQL")

*Curso*:

* *id* (PK)
* *nome* - (value-concepts: "Engenharia Informática"; "Direito")

*Disciplina*:

* *id* (PK)
* *curso\_id* (FK)
* *nome* - (value-concepts: "Programação Orientada a Objetos"; "Laboratório de Aplicações Multimédia")

*Grupo*:

* *id* (PK)
* *numero* - (value-concepts: "12"; "23")