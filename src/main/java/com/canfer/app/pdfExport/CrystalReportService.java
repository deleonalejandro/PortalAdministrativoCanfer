package com.canfer.app.pdfExport;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.canfer.app.cfd.Comprobante;
import com.canfer.app.cfd.XmlService;
import com.canfer.app.model.ComprobanteFiscal;
import com.canfer.app.model.Documento;
import com.canfer.app.model.Documento.DocumentoPDF;
import com.canfer.app.model.Log;
import com.canfer.app.repository.ComprobanteFiscalRespository;
import com.canfer.app.repository.DocumentoRepository;
import com.canfer.app.repository.EmpresaRepository;
import com.canfer.app.service.DocumentoService;
//Crystal Java Reporting Component (JRC) imports.
import com.crystaldecisions.reports.sdk.*;
import com.crystaldecisions.sdk.occa.report.lib.*;
import com.crystaldecisions.sdk.occa.report.exportoptions.*;

//Java imports.
import java.io.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


//TODO COMPROBAR QUE TODAS LAS RUTAS ESTEN CORRECTAS

@Service
public class CrystalReportService {
	
	@Autowired
	private EmpresaRepository empresaRepository; 
	@Autowired
	private DocumentoRepository documentoRepository; 
	
	public String exportPDF(String empresa, Integer pago, String user, String password, String rfc, Long id) {

		String REPORT_NAME = "C:\\Users\\aadministrador\\Desktop\\AVISO_PAGO_PAECRSAP-JDBC .rpt";
		 String EXPORT_FILE = "C:\\Users\\alex2\\PortalProveedores\\ExportedPDFs";
		 String path = EXPORT_FILE + File.separator + rfc + File.separator + pago + ".pdf";
		 
		try {

			//Open report.			
			ReportClientDocument reportClientDoc = new ReportClientDocument();			
			reportClientDoc.open(REPORT_NAME, 0);
			
			//NOTE: If parameters or database login credentials are required, they need to be set before.
			//calling the export() method of the PrintOutputController.
			
			//Incluir Parametros
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "Empresa", empresa);
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "Pago", pago);
			
			//Incluir DB login
			reportClientDoc.getDatabaseController().logon(user, password);
			
			//Export report and obtain an input stream that can be written to disk.
			//See the Java Reporting Component Developer's Guide for more information on the supported export format enumerations
			//possible with the JRC.
			ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream)reportClientDoc.getPrintOutputController().export(ReportExportFormat.PDF);
			
			//Release report.
			reportClientDoc.close();
						
			//Use the Java I/O libraries to write the exported content to the file system.
			byte byteArray[] = new byte[byteArrayInputStream.available()];

			//Create a new file that will contain the exported result.
			
			
			File file = new File(path);
			FileOutputStream fileOutputStream = new FileOutputStream(file);

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(byteArrayInputStream.available());
			int x = byteArrayInputStream.read(byteArray, 0, byteArrayInputStream.available());

			byteArrayOutputStream.write(byteArray, 0, x);
			byteArrayOutputStream.writeTo(fileOutputStream);

			//Close streams.
			byteArrayInputStream.close();
			byteArrayOutputStream.close();
			fileOutputStream.close();
			
			//Guardamos el Crystal
					
 			DocumentoPDF doc = new DocumentoPDF(idTabla, empresaRepository.findByRfc(rfc),"Documentos Fiscales", "Aviso de Pago", 
					"pdf", path);
			documentoRepository.save(doc);
			
			return path;			
		}
		catch(ReportSDKException ex) {
		
			Log.activity("No se pudo generar el Crystal Report para el Pago: " + pago,empresa, "ERROR_FILE" );
			return null; 
		}
		catch(Exception ex) {
			
			Log.activity("Ocurrió un error al exportar el aviso de Pago: "+ pago+".", empresa, "ERROR");
			ex.printStackTrace();
			return null; 	
		}
		
		

	}

	public String exportGenerico(Long id, ComprobanteFiscal comprobanteFiscal) {
		
		String sName = comprobanteFiscal.getDocumento().getArchivoXML().getNombre(); 
		String REPORT_NAME = sName.substring(0, sName.length() - 3) + "pdf";
		
		String sPath = comprobanteFiscal.getDocumento().getArchivoXML().getRuta();
		String path = sPath.substring(0, sPath.length() - 3) + "pdf";
		
		String pathQR = "C:\\Users\\aadministrador\\Desktop\\CurrentQR.png";
		
		File file = new File(path);
		
		boolean existsQR = false;
		
		
		Comprobante comprobante = comprobanteFiscal.getDocumento().getArchivoXML().toCfdi();
		
		try {

			//Open report.			
			ReportClientDocument reportClientDoc = new ReportClientDocument();			
			reportClientDoc.open(REPORT_NAME, 0);
			
			//NOTE: If parameters or database login credentials are required, they need to be set before.
			//calling the export() method of the PrintOutputController.
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "folio", comprobante.getFolio());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "serie", comprobante.getSerie());
			
			//Incluir Parametros
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "rfcEmisor", comprobante.getEmisorRfc());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "nombreEmisor", comprobante.getEmisorNombre());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "rfcReceptor", comprobante.getReceptorRfc());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "nombreReceptor", comprobante.getReceptorNombre());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "usoCFDI", comprobante.getReceptorUsoCFDI());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "uuid", comprobante.getUuidTfd());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "csd", comprobante.getSelloCfdTfd());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "emision", comprobante.getFecha());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "tipo", comprobante.getTipoDeComprobante());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "regimen", comprobante.getEmisorRegimenFiscal());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "moneda", comprobante.getMoneda());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "formaPago", comprobante.getFormaPago());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "total", comprobante.getTotal());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "sellocfdi", comprobante.getSelloCfdTfd());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "sellosat", comprobante.getSelloSatTfd());
			reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "sello", comprobante.getSello());
			
			
			String sello = comprobante.getSelloCfdTfd();
			String ultimosDig = sello.substring(sello.length() - 8);
			
			
			//save QR
			String urlSAT = "https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx?id="+comprobante.getUuidTfd()+
					"&re="+comprobante.getEmisorRfc()+"&rr="+comprobante.getReceptorRfc()+"&tt="+comprobante.getTotal()+"&fe="+ultimosDig;
			
			try {	
				
				existsQR  = generateQRCodeImage(urlSAT, 350, 350, pathQR);
				
			} catch (WriterException e) {
	        	
	            Log.activity("No se pudo generar un QR para el PDF genérico "+comprobante.getUuidTfd()+".", comprobante.getReceptorNombre(), "ERROR");
	            
	        } catch (IOException e) {
	        	
	        	 Log.activity("No se pudo generar un QR para el PDF genérico "+comprobante.getUuidTfd()+".", comprobante.getReceptorNombre(), "ERROR_FILE");
	        
	        }
			
			if (existsQR) { 
				
				reportClientDoc.getDataDefController().getParameterFieldController().setCurrentValue("", "qr", pathQR);
			
			}
			
			//Export report and obtain an input stream that can be written to disk.
			//See the Java Reporting Component Developer's Guide for more information on the supported export format enumerations
			//possible with the JRC.
			ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream)reportClientDoc.getPrintOutputController().export(ReportExportFormat.PDF);
			
			//Release report.
			reportClientDoc.close();
						
			//Use the Java I/O libraries to write the exported content to the file system.
			byte byteArray[] = new byte[byteArrayInputStream.available()];

			//Create a new file that will contain the exported result.
			FileOutputStream fileOutputStream = new FileOutputStream(file);

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(byteArrayInputStream.available());
			int x = byteArrayInputStream.read(byteArray, 0, byteArrayInputStream.available());

			byteArrayOutputStream.write(byteArray, 0, x);
			byteArrayOutputStream.writeTo(fileOutputStream);

			//Close streams.
			byteArrayInputStream.close();
			byteArrayOutputStream.close();
			fileOutputStream.close();
			
			//Guardamos el PDF Generico
			Documento documento = documentoService.save(factura, "pdf", doc.getModulo(), path);
			documentoRepository.save(documento);
			
			return path;			
		}
		catch(ReportSDKException ex) {
		
			Log.activity("No se pudo generar el PDF Generico para: " + comprobanteFiscal.getUuid() +
					".", comprobante.getReceptorNombre(), "ERROR_FILE");
			
			return null; 
			
		}
		catch(Exception ex) {
			
			Log.activity("Ocurrió un error al exportar un PDF genérico para " + comprobanteFiscal.getUuid() 
				+".", comprobante.getReceptorNombre(), "ERROR");
			
			return null; 	
			
		}
		
		

	}
	

	    private boolean generateQRCodeImage(String text, int width, int height, String filePath)
	            throws WriterException, IOException {
	    	
	        QRCodeWriter qrCodeWriter = new QRCodeWriter();
	        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

	        Path path = FileSystems.getDefault().getPath(filePath);
	        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
	        
	        return true; 
	        
	    }
}


   