# Casos de teste — task-service

## Escopo e situação

Inventário baseado no código de src/test. **Os testes não foram executados nesta atualização de documentação**; nenhum caso está declarado aprovado. A existência de uma asserção não comprova sua aprovação no ambiente atual.

Os testes de serviço usam Mockito. Os controllers usam WebMvcTest, serviços simulados e, nos cenários autenticados, WithMockUser.

## Casos automatizados existentes (12)

Os 12 testes do `task-service` estão distribuídos nas seguintes classes:

| Classe | Quantidade | Casos cobertos |
| --- | --- | --- |
| `TaskControllerTest` | 4 | Listagem pública de vagas; bloqueio de criação sem autenticação; bloqueio para candidato; criação por contratante. |
| `TaskServiceTest` | 2 | Status inicial `OPEN`; exceção ao consultar vaga inexistente. |
| `ApplicationControllerTest` | 3 | Candidatura por candidato; bloqueio de candidatura por contratante; bloqueio de listagem para contratante que não é dono da vaga. |
| `DuvidaVagaControllerTest` | 2 | Listagem pública de dúvidas; resposta pelo dono da vaga. |
| `TaskServiceApplicationTests` | 1 | Carregamento do contexto Spring. |

A tabela abaixo relaciona cada método de teste ao código-fonte, à pré-condição, à ação e ao resultado verificado.

| ID | Origem | Pré-condição | Ação | Resultado verificado pelo código |
| --- | --- | --- | --- | --- |
| 1 | [ApplicationControllerTest.deveCriarCandidaturaParaCandidato](https://github.com/grupo-do-alef/task-service/blob/main/src/test/java/com/github/ggarabs/task_service/adapters/inbound/ApplicationControllerTest.java) | Usuário simulado CANDIDATE e serviço simulado. | POST /applications com taskId. | HTTP 201 e status PENDING. |
| 2 | [ApplicationControllerTest.deveNegarCandidaturaParaContratante](https://github.com/grupo-do-alef/task-service/blob/main/src/test/java/com/github/ggarabs/task_service/adapters/inbound/ApplicationControllerTest.java) | Usuário simulado CONTRACTOR. | POST /applications com taskId. | HTTP 403. |
| 3 | [ApplicationControllerTest.deveNegarListagemParaContratanteNaoDono](https://github.com/grupo-do-alef/task-service/blob/main/src/test/java/com/github/ggarabs/task_service/adapters/inbound/ApplicationControllerTest.java) | Contratante simulado diferente do dono da vaga. | GET /applications/task/{taskId}. | HTTP 403. |
| 4 | [DuvidaVagaControllerTest.deveListarDuvidasPublicamente](https://github.com/grupo-do-alef/task-service/blob/main/src/test/java/com/github/ggarabs/task_service/adapters/inbound/DuvidaVagaControllerTest.java) | Serviço simulado retorna uma pergunta. | GET /duvidas/task/{taskId} sem autenticação. | HTTP 200 e pergunta esperada. |
| 5 | [DuvidaVagaControllerTest.deveResponderDuvidaPeloDonoDaVaga](https://github.com/grupo-do-alef/task-service/blob/main/src/test/java/com/github/ggarabs/task_service/adapters/inbound/DuvidaVagaControllerTest.java) | CONTRACTOR simulado é dono da vaga; serviços simulados. | PATCH /duvidas/{id}/responder com texto. | HTTP 200 e resposta esperada. |
| 6 | [TaskControllerTest.deveListarVagasPublicamente](https://github.com/grupo-do-alef/task-service/blob/main/src/test/java/com/github/ggarabs/task_service/adapters/inbound/TaskControllerTest.java) | Serviço simulado retorna uma vaga. | GET /tasks sem usuário autenticado. | HTTP 200 e título esperado na lista. |
| 7 | [TaskControllerTest.deveBloquearCriacaoNaoAutenticada](https://github.com/grupo-do-alef/task-service/blob/main/src/test/java/com/github/ggarabs/task_service/adapters/inbound/TaskControllerTest.java) | Nenhum usuário autenticado; requisição com CSRF de teste. | POST /tasks com título. | HTTP 403. |
| 8 | [TaskControllerTest.deveNegarCriacaoParaCandidato](https://github.com/grupo-do-alef/task-service/blob/main/src/test/java/com/github/ggarabs/task_service/adapters/inbound/TaskControllerTest.java) | Usuário simulado com papel CANDIDATE. | POST /tasks com título. | HTTP 403. |
| 9 | [TaskControllerTest.deveCriarVagaParaContratante](https://github.com/grupo-do-alef/task-service/blob/main/src/test/java/com/github/ggarabs/task_service/adapters/inbound/TaskControllerTest.java) | Usuário simulado CONTRACTOR e serviço simulado. | POST /tasks com título. | HTTP 201, título esperado e status OPEN. |
| 10 | [TaskServiceTest.deveDefinirStatusOpenPorPadrao](https://github.com/grupo-do-alef/task-service/blob/main/src/test/java/com/github/ggarabs/task_service/application/TaskServiceTest.java) | Repositório simulado; vaga sem status. | Chamar createTask. | Status OPEN e uma chamada a save. |
| 11 | [TaskServiceTest.deveLancarExcecaoQuandoVagaNaoExiste](https://github.com/grupo-do-alef/task-service/blob/main/src/test/java/com/github/ggarabs/task_service/application/TaskServiceTest.java) | Repositório retorna vazio para o UUID. | Consultar vaga inexistente. | NoSuchElementException. |
| 12 | [TaskServiceApplicationTests.contextLoads](https://github.com/grupo-do-alef/task-service/blob/main/src/test/java/com/github/ggarabs/task_service/TaskServiceApplicationTests.java) | Configuração necessária ao contexto disponível. | Carregar o contexto Spring. | Inicialização sem exceção; não valida um fluxo de negócio. |

## Como executar

Use JDK 21 e o Maven Wrapper. No PowerShell, na raiz do repositório:

~~~powershell
.\mvnw.cmd test
~~~

Configure um banco exclusivo para testes por SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME e SPRING_DATASOURCE_PASSWORD antes de executar a suíte completa. O teste de contexto utiliza a configuração disponível; evite apontá-lo para dados reais. Os relatórios Maven ficam em target/surefire-reports. Em Linux/macOS, use ./mvnw.

## Casos manuais complementares — não executados

Estes cenários são propostas de validação e não testes automatizados existentes.

| ID | Pré-condição | Passos | Resultado esperado |
| --- | --- | --- | --- |
| M1 | Dois usuários e contrato de identidade corrigido. | Candidatar-se duas vezes à mesma vaga sem cancelar a primeira candidatura. | Segunda tentativa retorna HTTP 409. |
| M2 | Contrato de identidade corrigido; dono e outro usuário. | Alterar/excluir vaga e responder dúvida com cada usuário. | Dono autorizado; usuário sem propriedade e sem privilégio administrativo recebe HTTP 403. |
| M3 | Dois candidatos e contrato de identidade corrigido. | Consultar /applications/mine como cada candidato. | Retorna apenas candidaturas do usuário autenticado. |
| M4 | Conta real e token emitido pelo user-service. | Acessar operação protegida usando Bearer token. | Identidade e papel reconhecidos após alinhamento de /api/auth/me. Atualmente bloqueado pelo contrato ausente. |
