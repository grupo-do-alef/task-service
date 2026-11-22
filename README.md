# TrampoRápido — Task Service

Serviço responsavel pelas vagas, candidaturas e dúvidas do TrampoRápido.

## Contexto e papel no projeto

O TrampoRápido conecta profissionais a oportunidades de trabalho. Sua implementação está distribuída em três repositórios:

| Repositório | Papel |
| --- | --- |
| tramporapido-ui | Interface web em Next.js, React e TypeScript. |
| user-service | Cadastro, autenticação por JWT, dados pessoais e currículos. |
| task-service | Gestão das oportunidades, candidaturas e perguntas relacionadas às vagas. |

Este serviço concentra as operações do processo de contratação. Possui um PostgreSQL próprio e referencia usuários por UUID, sem relacionamento JPA entre os bancos. A identidade para operações protegidas depende do user-service.


## Tecnologias

- Java 21 e Spring Boot 4.1.1.
- Spring MVC e Spring Security.
- Spring Data JPA/Hibernate e PostgreSQL 17.
- Lombok; Springdoc/OpenAPI e HATEOAS nas dependências.
- Maven Wrapper, Docker Compose, Jenkins e GitHub Actions.

## Organização

~~~text
Controller → Service → Repository → PostgreSQL
~~~

- adapters/inbound: controllers REST de vagas, candidaturas e dúvidas.
- application: regras e operações de negócio, com transações.
- adapters/outbound: entidades JPA e repositórios Spring Data.
- config: segurança, CORS e filtro de autenticação.

Apesar do nome adapters, a estrutura usa entidades JPA diretamente nos contratos HTTP e não possui a mesma separação por portas do user-service.

## Recursos e API

### Vagas

| Método | Rota | Uso |
| --- | --- | --- |
| GET | /tasks | Listar; aceita filtro opcional status. |
| GET | /tasks/{id} | Consultar uma vaga. |
| POST | /tasks | Criar vaga como contratante, intermediário ou administrador. |
| PATCH | /tasks/{id}/status?status=... | Alterar status com verificação de proprietário ou administrador. |
| DELETE | /tasks/{id} | Excluir com verificação de proprietário ou administrador. |

A vaga contém título, descrição, cidade, bairro, remuneração, modalidade, datas e UUID do contratante. Os status são OPEN, ONGOING, FINISHED e CANCELED. As modalidades são HYBRID, HOME_OFFICE e IN_PERSON. Na criação, o status padrão é OPEN.

### Candidaturas

| Método | Rota | Uso |
| --- | --- | --- |
| POST | /applications | Criar candidatura; o candidato vem da autenticação. |
| GET | /applications/mine | Listar candidaturas do próprio candidato. |
| GET | /applications/task/{taskId} | Listar candidaturas da vaga, com controle de acesso. |
| GET | /applications/{id} | Consultar com controle de acesso. |
| PATCH | /applications/{id}/status?status=... | Alterar status como responsável autorizado. |
| DELETE | /applications/{id} | Excluir com controle de acesso. |

As candidaturas vinculam taskId e candidateId. Os status são PENDING, IN_ANALYSIS, APPROVED, REJECTED e CANCELED. Uma nova candidatura recebe PENDING quando nenhum status é informado e data atual quando ausente. O serviço impede outra candidatura não cancelada para o mesmo candidato e vaga.

### Dúvidas

| Método | Rota | Uso |
| --- | --- | --- |
| GET | /duvidas/task/{taskId} | Listar perguntas de uma vaga. |
| POST | /duvidas | Criar pergunta; o autor vem da autenticação. |
| PATCH | /duvidas/{id}/responder | Responder como responsável pela vaga ou administrador. |

A resposta é recebida como String no corpo. As entidades armazenam pergunta, resposta, datas, UUID da vaga e UUID do autor.

As leituras GET de vagas e perguntas por vaga são públicas na configuração de segurança. As demais operações exigem autenticação e, conforme o endpoint, papel e propriedade do recurso.

## Integração e autenticação

O frontend envia o JWT no cabeçalho Authorization com o prefixo Bearer. O SessionAuthenticationFilter não valida a assinatura localmente: consulta GET /api/auth/me no user-service e espera id, email e role.

O mapeamento esperado é:

| Papel recebido | Autoridade no task-service |
| --- | --- |
| WORKER | ROLE_CANDIDATE |
| EMPLOYER | ROLE_CONTRACTOR |
| INTERMEDIARY | ROLE_INTERMEDIARY |
| ADMIN | ROLE_ADMINISTRATOR |

O UUID retornado se torna a identidade usada para vincular candidatos, autores e contratantes. O cliente HTTP possui timeouts de conexão e leitura de cinco segundos. A autenticação é stateless.


## Persistência

- TaskEntity: vagas, na tabela Task.
- ApplicationEntity: candidaturas, na tabela applications.
- DuvidaVagaEntity: perguntas e respostas, na tabela job_questions_tb.

Os identificadores são UUIDs. O Hibernate usa ddl-auto=update. As referências a usuários são identificadores, sem banco compartilhado com o user-service.

## Limitações atuais
- O projeto contempla apenas 3 casos de uso que estabelecemos anteriormente em outra entrega, o que torna a navegação funcional incompleta.
