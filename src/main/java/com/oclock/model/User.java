package com.oclock.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class User {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty nome;
    private final SimpleStringProperty email;
    private final SimpleStringProperty senha;
    private final SimpleStringProperty permission;
    

    public User(int id, String name, String email, String senha, String permission) {
    	
    	Criptografia crip = new Criptografia(); 
        String senhaHash = crip.gerarHash(senha); 

        this.id = new SimpleIntegerProperty(id);
        this.nome = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.senha = new SimpleStringProperty(senhaHash);
        this.permission = new SimpleStringProperty(permission);
    }

    public User(String name, String email, String senha, String permission) {
        this(0, name, email, senha, permission); // ID será gerado pelo banco
    }

    // Getters
    public int getId() { return id.get(); }
    public String getName() { return nome.get(); }
    public String getEmail() { return email.get(); }
    public String getPassword() { return senha.get(); }
    public String getPermission() { return permission.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.nome.set(name); }
    public void setEmail(String email) { this.email.set(email); }
    public void setPassword(String password) { this.senha.set(password); }
    public void setPermission(String permission) { this.permission.set(permission); }

    // Propriedades para TableView
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty nameProperty() { return nome; }
    public SimpleStringProperty emailProperty() { return email; }
    public SimpleStringProperty passwordProperty() { return senha; }
    public SimpleStringProperty permissionProperty() { return permission; }
}