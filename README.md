# O'Clock - Sistema de Gestão de Ponto

[![JavaFX](https://img.shields.io/badge/JavaFX-17+-blue.svg)](https://openjfx.io/)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.java.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A9.svg)](https://www.mysql.com/)

## 🚀 Sobre o Projeto

O O'Clock é um sistema intuitivo desenvolvido em JavaFX, projetado para simplificar o registro e controle de ponto de funcionários em pequenas e médias empresas. Ele oferece uma interface de usuário amigável e recursos essenciais para gerenciar a entrada e saída de colaboradores, garantindo precisão e segurança nos dados.

### ✨ Funcionalidades Principais

* **Login Seguro:** Autenticação de usuários com senhas criptografadas (utilizando SHA-256) armazenadas no banco de dados.
* **Registro de Ponto:** Funcionários podem registrar seus horários de entrada e saída com um clique.
* **Perfis de Acesso:** Distinção entre usuários comuns (funcionários) e administradores, com telas e funcionalidades específicas para cada perfil.
* **Gestão de Usuários (Admin):** Administradores podem visualizar, adicionar, editar e remover usuários.
* **Redefinição de Senha:** Funcionalidade para redefinição de senha, incluindo validação da senha atual e criptografia da nova senha.
* **Banco de Dados MySQL:** Armazenamento robusto e eficiente de dados de usuários e registros de ponto.

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 21+
* **Framework UI:** JavaFX
* **Banco de Dados:** MySQL 8.0+
* **Gerenciamento de Dependências:** Maven (recomendado, se estiver usando)
* **Criptografia:** SHA-256
* **IDE:** Eclipse (recomendado)

## 💻 Como Rodar o Projeto

Siga os passos abaixo para configurar e executar o projeto em sua máquina local.

### Pré-requisitos

* JDK (Java Development Kit) 17 ou superior.
* MySQL Server 8.0 ou superior (com phpMyAdmin ou MySQL Workbench para gestão).
* Eclipse IDE (ou outra IDE Java de sua preferência).
* Scene Builder (para edição dos arquivos FXML, se desejar).

### Configuração do Banco de Dados

1.  Crie um banco de dados MySQL chamado `OnClock`.
2.  Execute os seguintes scripts SQL para criar as tabelas necessárias:

    ```sql
    -- Tabela de Usuários
    CREATE TABLE usuarios (
        email VARCHAR(255) PRIMARY KEY,
        nome VARCHAR(255) NOT NULL,
        senha VARCHAR(255) NOT NULL, -- Armazenará o hash da senha
        permissao VARCHAR(50) NOT NULL, -- Ex: 'admin', 'usuario'
        telefone VARCHAR(255) NOT NULL
    );

    -- Tabela de Registros de Ponto
    CREATE TABLE registros_ponto (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_email VARCHAR(255) NOT NULL,
        data_hora DATETIME NOT NULL,
        CONSTRAINT fk_user_email FOREIGN KEY (user_email) REFERENCES usuarios(email) ON UPDATE CASCADE ON DELETE RESTRICT
    );
    ```
    * **Importante:** Caso necessário criar um usuário de admin, utilizar a classe Cadastro.

### Importar para o Eclipse

1.  Abra o Eclipse IDE.
2.  Vá em `File` > `Import...` > `Maven` > `Existing Maven Projects` (se você estiver usando Maven) ou `General` > `Existing Projects into Workspace` (se não for Maven).
3.  Selecione a pasta raiz do projeto e clique em `Finish`.

### Configurar Conexão com o Banco (se necessário)

* Verifique e ajuste as credenciais de conexão com o banco de dados nas classes da camada `model` (ex: `FazerLogin.java`, `Cadastro.java`, `BaterPonto.java`) se seu usuário e senha do MySQL forem diferentes de `root` e senha vazia.

### Executar a Aplicação

1.  Localize a classe `Main.java` (geralmente em `src/application/Main.java`).
2.  Clique com o botão direito na classe `Main.java` > `Run As` > `Java Application`.

## 🤝 Contribuição

Contribuições são bem-vindas! Se você deseja contribuir, siga estes passos:

1.  Faça um fork do projeto.
2.  Crie uma branch para sua feature (`git checkout -b feature/MinhaNovaFuncionalidade`).
3.  Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`).
4.  Envie para a branch original (`git push origin feature/MinhaNovaFuncionalidade`).
5.  Abra um Pull Request.

## 📧 Contato

Se tiver dúvidas ou sugestões, entre em contato:

* [Lucas Silva Rodrigues/lcssilvaa] - [lucassilvarodrigss16l@gmail.com]
