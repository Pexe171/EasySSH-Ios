# Arquitetura do EasySSH

## Visão Geral

O EasySSH é um aplicativo Android local-first. A versão atual não usa backend: os dados ficam no aparelho e a conexão SSH é feita diretamente do Android para a VPS ou instância EC2.

Essa decisão reduz latência, simplifica privacidade e evita expor chaves privadas a qualquer serviço intermediário. O custo é que sincronização entre aparelhos, backup de perfis e gerenciamento remoto ficam fora do escopo inicial.

## Camadas

- `ui`: telas Jetpack Compose, diálogos e `MainViewModel`.
- `domain`: modelos de perfil, modo de IP e regras de validação.
- `data`: entidade Room, DAO, banco local e repositório.
- `core.crypto`: criptografia AES-GCM com Android Keystore e armazenamento de chaves privadas.
- `ssh`: conexão SSHJ, verificação de host key, sessão PTY e ciclo de vida do terminal.
- `terminal`: ponte entre WebView restrita e assets locais do `xterm.js`.

## Estrutura de Dados

O Room armazena apenas metadados do perfil:

- apelido da máquina;
- usuário SSH;
- host fixo ou modo de IP rotativo;
- porta SSH;
- nome do arquivo de chave criptografada;
- nome de exibição da chave importada;
- impressão digital confiável do servidor.

A chave privada é criptografada e salva em arquivo privado do aplicativo, dentro de `files/private_keys`.

## Fluxo de Conexão SSH

1. A interface solicita o host quando o perfil usa IP rotativo.
2. O `MainViewModel` lê e descriptografa a chave privada em memória.
3. O SSHJ recebe a chave por um arquivo temporário, necessário porque o carregador de chaves trabalha com caminho de arquivo.
4. O aplicativo registra uma versão moderna do BouncyCastle antes de iniciar a conexão SSH.
5. O verificador de host key calcula a impressão digital do servidor e compara com a impressão digital já confiada.
6. No primeiro acesso, a interface pede confirmação Trust On First Use.
7. Após a autenticação, uma sessão PTY é aberta.
8. A saída do shell é enviada para o `xterm.js`.
9. A entrada digitada no terminal volta pela ponte JavaScript da WebView e segue para a sessão SSH.

## Decisões Técnicas

- Kotlin e Jetpack Compose foram escolhidos por serem o caminho nativo atual para Android moderno.
- Room foi usado para persistência local tipada, com baixo overhead e bom suporte a Flow.
- Android Keystore protege a chave AES usada para criptografar os arquivos `.pem` importados.
- SSHJ concentra a implementação SSH e evita manter protocolo criptográfico próprio.
- `xterm.js` entrega comportamento de terminal mais próximo ao esperado em SSH real.
- WebView usa apenas assets locais do app, sem navegação externa.

## Banco de Dados

O banco local é suficiente para a versão atual porque não há autenticação de conta, sincronização em nuvem ou painel web. PostgreSQL passa a fazer sentido apenas quando existir backend para recursos como equipes, histórico sincronizado ou inventário de servidores.

## Itens Fora do Escopo Atual

- Login por senha.
- Chaves `.pem` com passphrase.
- Conversão de chaves PuTTY `.ppk`.
- Múltiplas abas de terminal simultâneas.
- Barra de atalhos com Ctrl, Shift e comandos salvos.
- Assinatura de release e empacotamento para distribuição pública.
