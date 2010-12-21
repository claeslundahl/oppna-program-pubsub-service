package se.vgregion.pubsub.impl;

import java.io.StringReader;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import se.vgregion.dao.domain.patterns.entity.AbstractEntity;
import se.vgregion.pubsub.Field;

@Entity
@Table(name="FIELDS")
public class DefaultField extends AbstractEntity<String> implements Field {

    @Id
    @GeneratedValue
    @SuppressWarnings("unused") // only used by JPA
    private Long pk;

    @Column(nullable=false, unique=true)
    private String id = UUID.randomUUID().toString();
    
    @Basic(optional=false)
    private String xml;

    
    private static Element createElement(String namespace, String name, String value) {
        Element elm = new Element(name, namespace);
        elm.appendChild(value);
        return elm;
    }

    // For JPA
    protected DefaultField() {
    }

    
    public DefaultField(String namespace, String name, String value) {
        this(createElement(namespace, name, value));
    }

    public DefaultField(Element elm) {
        this.xml = XmlUtil.xmlToString(elm);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Element toXml() {
        return XmlUtil.stringToXml(xml);
    }

}
