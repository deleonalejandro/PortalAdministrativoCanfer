package com.canfer.app.mail;


import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.canfer.app.model.ComprobanteFiscal;
import com.canfer.app.model.Empresa;
import com.canfer.app.model.Log;
import com.canfer.app.model.Pago;
import com.canfer.app.model.Proveedor;
import com.canfer.app.model.Usuario.UsuarioCanfer;
import com.canfer.app.model.Usuario.UsuarioProveedor;
import com.canfer.app.repository.EmpresaRepository;
import com.canfer.app.repository.UsuarioCanferRepository;
import com.canfer.app.service.RepositoryService;
import com.canfer.app.storage.StorageProperties;

/**
 * 
 * Servicio para redactar y enviar mails
 * 
 * @author Alejandro de Leon y Yasmin Femerling
 * @date 09/08/2020
 */

@Service
public class EmailSenderService {
	
	// ==============
	// PRIVATE FIELDS
	// ==============

	@Autowired
	private UsuarioCanferRepository usuarioCanferRep; 
	@Autowired
	private EmpresaRepository empresaRep; 
	@Autowired
	private RepositoryService superRepo; 
    @Autowired
    private TemplateEngine htmlTemplateEngine;
    @Autowired
    private EmailSenderProperties emailSenderProperties;
    @Autowired
    private StorageProperties storageProperties; 
    
	// ==============
	// PUBLIC METHODS
	// ==============
	
	
	/**
	   * Metodo para escribir un mail y enviarlo
	   * 
	   * @return void
	   */
	
	
	public void sendEmailAvisoPago(Pago pago){
		final String EMAIL_TEMPLATE_NAME = "emailNewAviso.html";
		
		//Obtener correo de contadores y de proveedor
		List<UsuarioCanfer> contadores = usuarioCanferRep.findAllByEmpresas(
				empresaRep.findByRfc(pago.getRfcEmpresa()));
		String to = pago.getCorreo();
		
		for(UsuarioCanfer contador:contadores) {
			
			to=to+","+contador.getCorreo();
			
		}
	
		
		//Obtener cfdi que se pago
		ComprobanteFiscal comprobante = superRepo.findFacturaByPago(pago);
	    //Obtener la empresa del pago
		Empresa empresa = superRepo.findEmpresaByRFC(pago.getRfcEmpresa());
		//Obtener la empresa del pago
		Proveedor proveedor = superRepo.findOneProveedorByRFC(pago.getRfcProveedor());
		
	    try {
	    	
	    	 // Prepare the evaluation context
	        final Context ctx = new Context();
	        
	        if (comprobante != null) {
			ctx.setVariable("fechaEmision",comprobante.getFechaEmision()); 
			ctx.setVariable("serie",comprobante.getSerie()); 
			ctx.setVariable("uuid",comprobante.getUuid()); 
			ctx.setVariable("folio",comprobante.getFolio()); 
			ctx.setVariable("tipoComprobante",comprobante.getTipoDocumento());
			ctx.setVariable("fechaCarga",comprobante.getFechaCarga()); 
			ctx.setVariable("estatusPago",comprobante.getEstatusPago());
			ctx.setVariable("estatusSAT",comprobante.getRespuestaValidacion());
	        }
	        
	        if (proveedor != null) {
	        	ctx.setVariable("nombreProveedor", proveedor.getNombre());
	        } else {
	        	ctx.setVariable("nombreProveedor", pago.getRfcProveedor());
	        }
			ctx.setVariable("empresa",empresa.getNombre());
			ctx.setVariable("rfcEmisor",pago.getRfcProveedor()); 
			ctx.setVariable("rfcReceptor",pago.getRfcEmpresa()); 
			ctx.setVariable("folioPago",pago.getIdNumPago()); 
			ctx.setVariable("fechaPago",pago.getFecMvto()); 
			ctx.setVariable("montoPago",pago.getTotalPago()); 
			ctx.setVariable("moneda",pago.getMoneda()); 
			ctx.setVariable("totalFactura",pago.getTotalFactura()); 
			ctx.setVariable("totalParcialidad",pago.getTotalParcialidad()); 
			
	    	
			// Prepare message using a Spring helper
			 MimeMessage message = emailSenderProperties.createMimeMessage();
	         MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
	        
	         // Create the HTML body using Thymeleaf
		     final String htmlContent = this.htmlTemplateEngine.process(EMAIL_TEMPLATE_NAME, ctx);
		     helper.setText(htmlContent, true /* isHtml */);
		     helper.addInline("logoEmpresa",
		             new File(storageProperties.getLogoLocation().resolve(empresa.getProfilePictureName()).toString()));
		     helper.addInline("logoCanfer",
		             new File(storageProperties.getLogoLocation().resolve("CANFER-logo-transparente.png").toString()));
		    helper.setTo(InternetAddress.parse(to));
		    helper.setFrom(emailSenderProperties.getUsername());
	        helper.setSubject("Generación de Aviso de Pago.");
	        
	        if (pago.getDocumento() != null) {
	        	
		        try {	
		        	helper.addAttachment("AvisoDePago.pdf", new File(pago.getDocumento().getArchivoPDF().getRuta()));
		        } catch (Exception e) {
		        	Log.activity("No se pudo adjuntar aviso de pago al correo a " + to +" con un aviso de pago: "+ pago.getIdNumPago()+".", empresa.getNombre(), "ERROR_FILE");;
			    }
	        
	        } 
	        emailSenderProperties.send(message);
	        
	    } catch (MessagingException | MailException e) {

	        Log.activity("No se pudo enviar correo a " + to + " con un aviso de pago: "+ pago.getIdNumPago()+".",empresa.getNombre(), "ERROR_CONNECTION");;
	    }
	}

	public void sendEmailNewDoc(ComprobanteFiscal comprobante){
		final String EMAIL_TEMPLATE_NAME = "emailNewDoc.html";
        
		//Obtener correo de contadores y de proveedor
		Optional<Proveedor> proveedor = superRepo.findProveedorByEmpresaAndClaveProv(comprobante.getEmpresa(), comprobante.getProveedorClaveProv());
	      
		
		String to = proveedor.get().getCorreo();
		List<UsuarioCanfer> contadores = usuarioCanferRep.findAllByEmpresas(
				empresaRep.findByRfc(comprobante.getRfcEmpresa()));
		for(UsuarioCanfer contador:contadores) {to=to+","+contador.getCorreo();}
		
	    try {
	        
	        // Prepare the evaluation context
	        final Context ctx = new Context();
			ctx.setVariable("profilePictureName", comprobante.getEmpresa().getProfilePictureName());
			ctx.setVariable("nombreProveedor", comprobante.getProveedorNombre());
			ctx.setVariable("folio",comprobante.getFolio()); 
			ctx.setVariable("empresa",comprobante.getEmpresaNombre()); 
			ctx.setVariable("fechaCarga",comprobante.getFechaCarga()); 
			ctx.setVariable("estatusPago",comprobante.getEstatusPago()); 
			ctx.setVariable("folio",comprobante.getFolio()); 
			ctx.setVariable("uuid",comprobante.getUuid()); 
			ctx.setVariable("versionCFD",comprobante.getVersionCfd()); 
			ctx.setVariable("timbre",comprobante.getFechaTimbre()); 
			ctx.setVariable("versionTimbre",comprobante.getVersionTimbre());
			ctx.setVariable("noSat",comprobante.getNoCertificadoSat()); 
			ctx.setVariable("rfcEmisor",comprobante.getRfcProveedor()); 
			ctx.setVariable("rfcReceptor",comprobante.getRfcEmpresa()); 
			ctx.setVariable("fechaEmision",comprobante.getFechaEmision()); 
			ctx.setVariable("NoCerRec",comprobante.getNoCertificadoEmpresa());
			ctx.setVariable("serie",comprobante.getSerie()); 
			ctx.setVariable("tipoComprobante",comprobante.getTipoDocumento()); 
			ctx.setVariable("estatusPago",comprobante.getEstatusPago());
			ctx.setVariable("estatusSAT",comprobante.getRespuestaValidacion());
			

	        // Prepare message using a Spring helper
			 MimeMessage message = emailSenderProperties.createMimeMessage();
	         MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

	        // Create the HTML body using Thymeleaf
	        final String htmlContent = this.htmlTemplateEngine.process(EMAIL_TEMPLATE_NAME, ctx);
	        helper.setText(htmlContent, true /* isHtml */);
	        helper.addInline("logoEmpresa",
	                new File(storageProperties.getLogoLocation().resolve(comprobante.getEmpresa().getProfilePictureName()).toString()));
	        helper.addInline("logoCanfer",
	                new File(storageProperties.getLogoLocation().resolve("CANFER-logo-transparente.png").toString()));
	        helper.setTo(InternetAddress.parse(to));
	        helper.setFrom(emailSenderProperties.getUsername());
	        helper.setSubject("Recepción de Documento Fiscal.");
			
		   
	        emailSenderProperties.send(message);
	        
	    } catch (MessagingException | MailException e) {

	    	Log.activity("No se pudo enviar correo a " + to + " con la confirmación de recepción de documento fiscal: "  
	        		+ comprobante.getUuid() + ".", comprobante.getEmpresaNombre() ,"ERROR_CONNECTION");
	    }
	}
	
	public void sendEmailUpdateDoc(ComprobanteFiscal comprobante){
		final String EMAIL_TEMPLATE_NAME = "emailUpdateDoc.html";
        
		//Obtener correo de contadores y de proveedor
		Optional<Proveedor> proveedor = superRepo.findProveedorByEmpresaAndClaveProv(comprobante.getEmpresa(), comprobante.getProveedorClaveProv());
	      
		
		String to = proveedor.get().getCorreo();
		List<UsuarioCanfer> contadores = usuarioCanferRep.findAllByEmpresas(
				empresaRep.findByRfc(comprobante.getRfcEmpresa()));
		for(UsuarioCanfer contador:contadores) {to=to+","+contador.getCorreo();}
		
	    try {
	        
	        // Prepare the evaluation context
	        final Context ctx = new Context();
	        ctx.setVariable("profilePictureName", comprobante.getEmpresa().getProfilePictureName());
			ctx.setVariable("nombreProveedor", comprobante.getProveedorNombre());
			ctx.setVariable("folio",comprobante.getFolio()); 
			ctx.setVariable("empresa",comprobante.getEmpresaNombre()); 
			ctx.setVariable("fechaCarga",comprobante.getFechaCarga()); 
			ctx.setVariable("estatusPago",comprobante.getEstatusPago()); 
			ctx.setVariable("folio",comprobante.getFolio()); 
			ctx.setVariable("uuid",comprobante.getUuid()); 
			ctx.setVariable("versionCFD",comprobante.getVersionCfd()); 
			ctx.setVariable("timbre",comprobante.getFechaTimbre()); 
			ctx.setVariable("versionTimbre",comprobante.getVersionTimbre());
			ctx.setVariable("noSat",comprobante.getNoCertificadoSat()); 
			ctx.setVariable("rfcEmisor",comprobante.getRfcProveedor()); 
			ctx.setVariable("rfcReceptor",comprobante.getRfcEmpresa()); 
			ctx.setVariable("fechaEmision",comprobante.getFechaEmision()); 
			ctx.setVariable("NoCerRec",comprobante.getNoCertificadoEmpresa());
			ctx.setVariable("serie",comprobante.getSerie()); 
			ctx.setVariable("tipoComprobante",comprobante.getTipoDocumento()); 
			ctx.setVariable("estatusPago",comprobante.getEstatusPago());
			ctx.setVariable("estatusSAT",comprobante.getRespuestaValidacion());
			ctx.setVariable("comentario",comprobante.getComentario());

	        // Prepare message using a Spring helper
			 MimeMessage message = emailSenderProperties.createMimeMessage();
	         MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

	        // Create the HTML body using Thymeleaf
	        final String htmlContent = this.htmlTemplateEngine.process(EMAIL_TEMPLATE_NAME, ctx);
	        helper.setText(htmlContent, true /* isHtml */);
	        helper.addInline("logoEmpresa",
	                new File(storageProperties.getLogoLocation().resolve(comprobante.getEmpresa().getProfilePictureName()).toString()));
	        helper.addInline("logoCanfer",
	                new File(storageProperties.getLogoLocation().resolve("CANFER-logo-transparente.png").toString()));
	        helper.setTo(InternetAddress.parse(to));
	        helper.setFrom(emailSenderProperties.getUsername());
	        helper.setSubject("Actualización de  un Documento Fiscal.");
		    
	        emailSenderProperties.send(message);
	        
	    } catch (MessagingException | MailException e) {

	        Log.falla("No se pudo enviar correo a " + to + " con la actualización del documento fiscal: "  
	        		+ comprobante.getUuid() + "." ,"ERROR_CONNECTION");
	    }
	}
	
	public void sendEmailNewAccount(UsuarioProveedor usuario, String pass){
		final String EMAIL_TEMPLATE_NAME = "emailUsuarioProv.html";
		
        
	    try {
	        
	        // Prepare the evaluation context
	        final Context ctx = new Context();
			ctx.setVariable("usuario", "Nombre de usuario: "+ usuario.getUsername());
			ctx.setVariable("rfc", "RFC asociado: "+ usuario.getRfcProveedor());
			ctx.setVariable("nombre", "¡Bienvenido "+ usuario.getNombre()+ " " + usuario.getApellido()+"!");
			ctx.setVariable("psss", "Contraseña: "+ pass);

			 // Prepare message using a Spring helper
			 MimeMessage message = emailSenderProperties.createMimeMessage();
	         MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

	        // Create the HTML body using Thymeleaf
	        final String htmlContent = this.htmlTemplateEngine.process(EMAIL_TEMPLATE_NAME, ctx);
	        helper.setText(htmlContent, true /* isHtml */);
	        helper.addInline("logoCanfer",
	                new File(storageProperties.getLogoLocation().resolve("CANFER-logo-transparente.png").toString()));
	        helper.setTo(InternetAddress.parse(usuario.getProveedores().get(0).getCorreo()));
	        helper.setFrom(emailSenderProperties.getUsername());
	        helper.setSubject("Nueva Cuenta en Portal de Proveedores Canfer.");
		    
	        emailSenderProperties.send(message);
	        
	    } catch (MessagingException | MailException e) {

	        Log.falla("No se pudo enviar correo a " + usuario.getCorreo() + " con la información del nuevo usuario proveedor: "  
	        		+ usuario.getUsername() + "." ,"ERROR_CONNECTION");
	    }
	}
	
	
}
