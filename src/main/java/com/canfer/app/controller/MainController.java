package com.canfer.app.controller;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.businessobjects.reports.jdbinterface.common.SideOfNode;
import com.canfer.app.model.ComprobanteFiscal;
import com.canfer.app.model.Sucursal;
import com.canfer.app.repository.ComprobanteFiscalBaseRepository;
import com.canfer.app.repository.ComprobanteFiscalRespository;
import com.canfer.app.repository.EstadoRepository;
import com.canfer.app.security.AuthenticationFacade;
import com.canfer.app.service.EmpresaService;
import com.canfer.app.service.RepositoryService;

@Controller
public class MainController {

	@Autowired
	private AuthenticationFacade authenticationFacade;
	
	@Autowired
	private ComprobanteFiscalRespository cfRepo;
	
	@Autowired
	private RepositoryService superRepo;
	
	@Autowired
	private EmpresaService empresaService;
	

	public MainController() {
		// Constructor
	}

	@GetMapping(value = "/")
	public String home() {
		return "redirect:/login";
	}

	/*
	 * 
	 * LOGIN CONTROLLERS FOR PORTAL ADMINISTRATIVO
	 * 
	 */
	
	// Login form
	@RequestMapping("/login")
	public String login( Model model) {
		
		if (isAuthenticated()) {
	        return "redirect:/dashboard";
	    }
		
		model.addAttribute("empresas", empresaService.findAll());
	    return "login";
		
	}

	// Login form with error
	@RequestMapping("/login-error")
	public String loginError(Model model, @RequestParam("auth") String authentication ) {
		
		model.addAttribute("loginError", true);
		
		if (authentication.equalsIgnoreCase("pa")) {
			model.addAttribute("empresas", empresaService.findAll());
			return "login";
			
		} else {
			
			return "login-proveedores";
		}
	}
	
	@RequestMapping("/logoutSuccess")
	public String logout(Model model, @RequestParam("auth") String module) {
		
		model.addAttribute("success", true);
		
		if (module.equalsIgnoreCase("pa")) {
			model.addAttribute("empresas", empresaService.findAll());
			return "login";
			
		} else {
			
			return "login-proveedores";
		}
		
	}
	
	/*
	 * 
	 * LOGIN CONTROLLERS FOR PORTAL PROVEEDORES
	 * 
	 */
	
	@GetMapping(value = "/login/portalP")
	public String getLoginProveedores() {
		
		if (isAuthenticated()) {
	        return "redirect:/dashboardSupplier";
	    }
		
		return "login-proveedores";
	}
	

	/* TODO ERASE THIS METHOD AFTER TESTINS SP
	 * 
	 * --------------------------------------
	 * CHECK FOR THE PARAMETER'S NAMES MOST BE
	 * EXACTLY THE SAME.
	 * 
	 */
	
	@GetMapping(value = "/testsp")
	@ResponseBody
	public String testSp(@RequestParam String n) {
		
		String pServidor = "192.168.1.132";
		String pBaseDatos = "PAEPRODUCCION";
		Long pIdEmpresa = 8L;
		
		cfRepo.actualizaBitRs(pServidor, pBaseDatos, pIdEmpresa);
		
		return "true";
	}
	
	/*
	 * ENDLINE 
	 * ---------------------------------------
	 */

	@GetMapping("/sucursal")
	@ResponseBody
	public LocalDateTime testSucursal() {
		
		LocalDateTime dateTime = LocalDateTime.parse("18/02/2021",
		        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		
		return dateTime;
	}
	


	private boolean isAuthenticated() {
		Authentication authentication = authenticationFacade.getAuthentication();
		if (authentication == null || AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
			        return false;
			    }
	    return authentication.isAuthenticated();
	}
	
	





}
