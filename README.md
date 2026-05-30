# EasySSH

EasySSH é um aplicativo Android para acesso rápido a VPS e instâncias AWS EC2 por SSH usando chave privada `.pem` ou OpenSSH. O projeto prioriza uma experiência direta: cadastrar a máquina, importar a chave, conectar e usar um terminal interativo no celular.

A aplicação é local-first: não existe backend nesta versão. Perfis, chaves e impressões digitais dos servidores ficam no próprio aparelho, e a conexão SSH sai diretamente do Android para a VPS.

## Funcionalidades

- Interface Android nativa com Kotlin e Jetpack Compose.
- Cadastro de VPS com apelido, usuário SSH, porta e IP/DNS público.
- Suporte a IP fixo e IP rotativo, perguntando o endereço no momento da conexão.
- Importação de chave privada pelo seletor de arquivos do Android.
- Armazenamento local da chave com criptografia AES-GCM via Android Keystore.
- Conexão SSH com SSHJ e validação de servidor por Trust On First Use.
- Terminal interativo com `xterm.js` local dentro de WebView restrita.
- Ajuste de tamanho de fonte e espaçamento do terminal.
- Build debug para instalação direta em aparelho físico.

## Requisitos

- Android SDK instalado e disponível em `ANDROID_HOME` ou no caminho padrão do Android Studio.
- JDK 17.
- Gradle Wrapper incluído no repositório.
- Celular Android com depuração USB ativada ou emulador Android.

O JDK portátil esperado pelo script de build fica em:

```text
.tools\jdk17-dist\jdk-17.0.19+10
```

## Build

No PowerShell:

```powershell
.\scripts\build-debug.ps1
```

Comando manual equivalente:

```powershell
$env:JAVA_HOME=(Resolve-Path '.tools\jdk17-dist\jdk-17.0.19+10').Path
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat testDebugUnitTest assembleDebug
```

O APK debug é gerado em:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Instalação em um Android conectado por USB:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Uso com AWS EC2

1. Crie uma nova VPS no EasySSH.
2. Informe um apelido para identificar a máquina.
3. Escolha o usuário SSH. Em instâncias Ubuntu da AWS, normalmente é `ubuntu`; em Amazon Linux, costuma ser `ec2-user`.
4. Mantenha a porta `22`, exceto quando a instância usar uma porta SSH personalizada.
5. Informe o IP público ou DNS público da EC2. Se o endereço mudar com frequência, ative IP rotativo.
6. Selecione a chave privada `.pem` usada pela instância.
7. Toque em conectar e confirme a impressão digital do servidor no primeiro acesso.

Exemplo de dados para uma EC2 Ubuntu:

```text
Usuário: ubuntu
Host: ec2-54-233-177-199.sa-east-1.compute.amazonaws.com
Porta: 22
Chave: Luninha.pem
```

Nesta versão, a chave privada deve estar sem senha. Suporte a passphrase, múltiplas abas e botões de atalho do terminal podem ser adicionados em ciclos posteriores.

## Segurança

- `android:allowBackup` está desativado.
- As regras de backup excluem arquivos e banco local do aplicativo.
- A chave privada é criptografada antes de ser salva em armazenamento privado do app.
- Durante a autenticação, a chave descriptografada é gravada apenas em arquivo temporário de cache e removida em seguida.
- Alterações na impressão digital do servidor são bloqueadas pela verificação de host key.

## Teste SSH Local Opcional

O repositório inclui um ambiente Docker opcional para testar conexão SSH localmente. Ele não substitui o teste contra uma VPS real, mas ajuda a validar o fluxo do app sem depender da AWS.

```powershell
$env:EASYSSH_TEST_PUBLIC_KEY = "ssh-ed25519 AAAA..."
docker compose up --build ssh-test
```

No emulador Android:

```text
Host: 10.0.2.2
Porta: 2222
Usuário: easyssh
```

Em aparelho físico, use o IP da máquina Windows na rede local e a porta `2222`.

## Arquitetura

A visão técnica está documentada em [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Referências

- Android Compose: https://developer.android.com/develop/ui/compose/documentation
- Arquitetura Android: https://developer.android.com/topic/architecture/recommendations
- Android Keystore: https://developer.android.com/privacy-and-security/keystore
- Criptografia no Android: https://developer.android.com/privacy-and-security/cryptography
- Storage Access Framework: https://developer.android.com/guide/topics/providers/document-provider
- Room: https://developer.android.com/training/data-storage/room
- xterm.js: https://xtermjs.org/docs/
- SSHJ: https://github.com/hierynomus/sshj
