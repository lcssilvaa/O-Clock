package com.oclock.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Criptografia {

    public static String gerarHash(String senha) {

        //criptografa a senha no banco de dados

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); //SHA-256 hash criptografado
            byte[] hashBytes = md.digest(senha.getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

            // mostra o erro em caso de erro

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash da senha", e);
        }
    }
}
