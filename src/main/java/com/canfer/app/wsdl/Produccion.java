//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.07.16 at 07:11:58 PM CDT 
//


package com.canfer.app.wsdl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Produccion complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Produccion"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="FoliosUtilizados" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="FoliosDisponibles" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Produccion", propOrder = {
    "foliosUtilizados",
    "foliosDisponibles"
})
public class Produccion {

    @XmlElement(name = "FoliosUtilizados")
    protected int foliosUtilizados;
    @XmlElement(name = "FoliosDisponibles")
    protected int foliosDisponibles;

    /**
     * Gets the value of the foliosUtilizados property.
     * 
     */
    public int getFoliosUtilizados() {
        return foliosUtilizados;
    }

    /**
     * Sets the value of the foliosUtilizados property.
     * 
     */
    public void setFoliosUtilizados(int value) {
        this.foliosUtilizados = value;
    }

    /**
     * Gets the value of the foliosDisponibles property.
     * 
     */
    public int getFoliosDisponibles() {
        return foliosDisponibles;
    }

    /**
     * Sets the value of the foliosDisponibles property.
     * 
     */
    public void setFoliosDisponibles(int value) {
        this.foliosDisponibles = value;
    }

}