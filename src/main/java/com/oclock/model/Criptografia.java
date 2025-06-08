package com.oclock.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Criptografia {

    public String gerarHash(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(texto.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Erro ao gerar hash: " + e.getMessage());
            return null;
        }
    }

    /* MAIN EM CASO DE PERCA DE USUÁRIO, ELE GERA UM HASH COM A PALAVRA QUE VC QUER
     * 
    public static void main(String[] args) {
        Criptografia crip = new Criptografia();
        String senhaExemplo = "senha123"; // A senha que você quer usar
        String hashGerado = crip.gerarHash(senhaExemplo);
        System.out.println("Senha: " + senhaExemplo);
        System.out.println("Hash SHA-256: " + hashGerado);
        */
    }
