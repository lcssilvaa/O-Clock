package com.oclock.controller;

public class GestaoUsuariosController {
	
    private String userEmail;
	private String userPermission;

	public void initData(String email, String role) {
        this.userEmail = email;
        this.userPermission = role;
        System.out.println("E-mail: " + userEmail + ", Permissão: " + userPermission);
        updateSidebarVisibility();
    }

	private void updateSidebarVisibility() {
		// TODO Auto-generated method stub
		
	}


}
