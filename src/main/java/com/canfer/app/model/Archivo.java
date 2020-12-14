package com.canfer.app.model;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.input.BOMInputStream;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.boot.jaxb.internal.stax.XmlInfrastructureException;

import com.canfer.app.storage.StorageFileNotFoundException;
import com.canfer.app.storage.StorageProperties;
import com.canfer.app.cfd.Comprobante;



@Entity(name = "Archivo")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "Tipo_Archivo")
@Component
public abstract class Archivo {
	
	@Transient
	protected StorageProperties storageProperties;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Long idArchivo; 
	
	@Column
	protected String ruta;
	
	@Column
	protected String extension;
	
	@Column
	protected String nombre;
	
	@Column
	@CreationTimestamp
	protected LocalDateTime fechaCarga;
	
	@Column
	protected LocalDateTime fechaMod;
	
	@Transient
	protected String receptor;
	
	public Archivo() {
		
	}
	
	public  Archivo(String ruta, String extension, String nombre) {
		this.ruta = ruta; 
		this.extension = extension; 
		this.nombre = nombre; 
		this.storageProperties = new StorageProperties(); 
		
	}
	

	public Resource loadAsResource() {
		
		try {
			
			Path file = Paths.get(this.ruta);
			Resource resource = new UrlResource(file.toUri());
			
			if (resource.exists() || resource.isReadable()) {
				
				return resource;
				
			}
			
			else {
				
				throw new StorageFileNotFoundException("No se pudo leer el archivo: " + nombre);

			}
		}
		
		catch (MalformedURLException e) {
			
			throw new StorageFileNotFoundException("No se pudo leer el archivo: " + nombre);
			
		}
		
	}
	
 	public void deleteFile() {
 		
 		Path file = Paths.get(this.ruta);
 		
		if (file.toFile().exists()) {
			
			try {
				
				// delete file if exists
				Files.delete(file);
				
			} catch (IOException e) {
				
				Log.falla("No se logró eliminar el archivo " + this.nombre + ".", "ERROR_STORAGE");
				
			}
		}
 		
 	}
 			
 	public void move(String newRuta) {

		Path temp = null;
		
		try {
			
			temp = Files.move(Paths.get(this.ruta), Paths.get(newRuta), StandardCopyOption.REPLACE_EXISTING);
			
			if (temp != null) {
				
				this.ruta = newRuta;
				
			}
			
			
		} catch (IOException e) {
			
			Log.falla("No se logró mover el archivo " + this.nombre + ".", "ERROR_STORAGE");
			
		}
		
		

 	}
 	
 	public void accept(String nombre, String ruta) {
 		
 		this.nombre = nombre + '.' + this.extension;
 		
 		String targetRuta = String.valueOf(Paths.get(ruta, this.nombre));
 		
 		move(targetRuta);
 		
 	}
 	
 	public abstract void discard();
 	
	
	public String getRuta() {
		return ruta;
	}
	
	public void setRuta(String ruta) {
		this.ruta = ruta;
	}
	
	public String getExtension() {
		return extension;
	}
	
	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	public String getNombre() {
		return nombre;
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public LocalDateTime getFechaCarga() {
		return fechaCarga;
	}
	
	public void setFechaCarga(LocalDateTime fechaCarga) {
		this.fechaCarga = fechaCarga;
	}
	
	public LocalDateTime getFechaMod() {
		return fechaMod;
	}
	
	public void setFechaMod(LocalDateTime fechaMod) {
		this.fechaMod = fechaMod;
	}

	public String getReceptor() {
		return receptor;
	}

	public void setReceptor(String receptor) {
		this.receptor = receptor;
	}
	





	@Entity
	@DiscriminatorValue("ARCHIVO_XML")
	@Configurable
	@Aspect
	public static class ArchivoXML extends Archivo {
		
		
		public ArchivoXML() { 
			
		}
	
		public ArchivoXML(String ruta, String extension, String nombre) { 
			super(ruta, extension, nombre);
		}
		
		public Comprobante toCfdi() {
			
			//Get ruta to path, get file from path
			Path path = Paths.get(this.getRuta()); 
			File file = path.toAbsolutePath().toFile();
			
			JAXBContext context;
			BOMInputStream bis;

			try (InputStream in = new FileInputStream(file)) {
				
				bis = new BOMInputStream(in);
				context = JAXBContext.newInstance(Comprobante.class);
				
				return (Comprobante) context.createUnmarshaller()
						.unmarshal(new InputStreamReader(new BufferedInputStream(bis)));
				
			} catch (JAXBException | IOException e) {
				 
				Log.falla("No fue posible leer el comprobante fiscal digital: " + this.getNombre(), "ERROR_STORAGE");
				
				return null;
			} 
		} 
		
		public String  toString() {
			
			// Get the file from the path and return string
			
			//Get ruta to path, get file from path
			Path path = Paths.get(this.getRuta()); 
			File file = path.toAbsolutePath().toFile();

			try (InputStream is = new FileInputStream(file);
					BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				
				while (line != null) {
					sb.append(line).append("\n");
					line = br.readLine();
				}

				return sb.toString();

			} catch (Exception e) {
				e.printStackTrace();
				throw new XmlInfrastructureException("No fue posible leer el documento: " + this.getNombre());
			}

		}
	
		

		@Override
		public void discard() {
			
			move(String.valueOf(storageProperties.getErrorLocation().resolve(this.nombre)));
			
		}

		
		
	}
	
	@Entity
	@DiscriminatorValue("ARCHIVO_PDF")
	public static class ArchivoPDF extends Archivo {
		
		public ArchivoPDF() {
		}
		
		public ArchivoPDF(String ruta, String extension, String nombre) {
			
			super(ruta, extension, nombre); 
		}
		
		public void actualizar(MultipartFile newFile) {
			
			try {
				
				if (newFile.isEmpty()) {
					
					Log.activity("Error al guardar un archivo vacío. " + newFile.getOriginalFilename(), this.receptor, "UPDATE");
					
				}
				
				try (InputStream inputStream = newFile.getInputStream()) {
					
					Files.copy(inputStream, Paths.get(this.ruta), StandardCopyOption.REPLACE_EXISTING);
				}
				
			} catch (IOException e) {
				
				Log.activity("Error al guardar un archivo vacío. " + newFile.getOriginalFilename(), this.receptor, "UPDATE");
				
			}
			
		}

		@Override
		public void discard() {

			move(String.valueOf(storageProperties.getErrorLocation().resolve(this.nombre)));
			
		}
	}
}