package com.canfer.app.webservice.invoiceone;

import java.util.Collections;
import java.util.List;

import org.hibernate.internal.util.xml.XmlInfrastructureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.soap.client.SoapFaultClientException;

import com.canfer.app.model.Archivo.ArchivoXML;
import com.canfer.app.model.Log;
import com.canfer.app.wsdl.invoiceone.ObtenerEstatusCuentaResponse;
import com.canfer.app.wsdl.invoiceone.ValidayVerificaXMLResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

@Service
public class ValidationService {

	@Autowired
	private ResponseWebService validationAnswer;
	
	private Jaxb2Marshaller marshaller;
	private Client client;

	// using the client configuration
	public ValidationService(ClientConfiguration clientConfiguration) {
		this.marshaller = clientConfiguration.marshaller();
		this.client = clientConfiguration.Client(this.marshaller);
		
	}
	
	/*
	 * INVOICE ONE WEBSERVICE METHODS
	 *  
	 **/
	
	//Valida y Verifica con MultipartFile 
	public List<String> validaVerifica(ArchivoXML file) {

		try {
			
			// create string from xml doc
			String xmlString = file.toString();

			//TODO REMOVE HARDCODEO
			// use user and passwd from account of web service
			String user = "Pruebas";
			String passwd = "Htp.7894";

			// send request to web service
			ValidayVerificaXMLResponse response = client.getInfo(user, passwd, xmlString);
			// decode response
			byte[] decodedResponse = Base64.getDecoder().decode(response.getValidayVerificaXMLResult());
			String utf8EncodedString = new String(decodedResponse, StandardCharsets.UTF_8);
			// get answer from validation in list
			return validationAnswer.getValidation(utf8EncodedString);
			
		} catch (SoapFaultClientException e) {
			Log.falla("No se pudo conectar con el Web Service de INVOICE ONE.", "ERROR_CONNECTION");;
			return Collections.emptyList();          
		} catch (XmlInfrastructureException e) {
			Log.falla("Ocurrió un error con el XML: "+e.getMessage(), "ERROR_FILE");
			return Arrays.asList("0", "Este documento no fue procesado por el Web Service", "No encontrado");          
		}
		

	}
	
	

	public void estatusCuenta() {
		try {
			//TODO REMOVE HARDCODEO
			// use user and passwd from account of web service
			String user = "PAE92070";
			String passwd = "a0e6$X8x";

			// send request to web service
			ObtenerEstatusCuentaResponse response = client.getInfo(user, passwd);

			// get list of results
			List<Integer> estatus = new ArrayList<>();

			estatus.add(response.getObtenerEstatusCuentaResult().getPruebas().getFoliosDisponibles());
			estatus.add(response.getObtenerEstatusCuentaResult().getPruebas().getFoliosUtilizados());
			estatus.add(response.getObtenerEstatusCuentaResult().getProduccion().getFoliosDisponibles());
			estatus.add(response.getObtenerEstatusCuentaResult().getProduccion().getFoliosUtilizados());

		} catch (SoapFaultClientException e) {
			e.printStackTrace();
		}
	}

}
