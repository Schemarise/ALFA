package com.schemarise.alfa.runtime.utils;

import com.schemarise.alfa.runtime.Holder;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class XmlNamespaceFilter {

    public static void filter(String[] namespacesToFilter, Set<String> skipTags, InputStream is, OutputStream os) throws SAXException, TransformerException, ParserConfigurationException {
        InputSource inputSource = new InputSource(is);

        final Holder<Boolean> escape = new Holder<>(false);

        XMLFilterImpl xf = new XMLFilterImpl(XMLReaderFactory.createXMLReader()) {


            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {

                if (!escape.getValue())
                    super.characters(ch, start, length);
            }

            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes atts) throws SAXException {

                if (skipTags.contains(qName) || escape.getValue()) {
                    escape.setValue(true);
                    return;
                }

                AttributesImpl aImpl = new AttributesImpl();

                int l = atts.getLength();
                for (int i = 0; i < l; i++) {

                    String aQName = atts.getQName(i);
                    boolean filterable = false;
                    if (aQName != null) {
                        for (int j = 0; j < namespacesToFilter.length; j++) {
                            if (aQName.startsWith(namespacesToFilter[j])) {
                                filterable = true;
                                break;
                            }
                        }
                    }
                    if (filterable)
                        continue;

                    String[] s = aQName.split(":");
                    if (s.length > 1) {
                        aQName = s[1];
                    }

                    aImpl.addAttribute("",
                            atts.getLocalName(i), aQName,
                            atts.getType(i), atts.getValue(i));
                }

                String[] s = qName.split(":");
                if (s.length > 1) {
                    super.startElement("", localName, s[1], aImpl);
                } else {
                    super.startElement("", localName, qName, aImpl);
                }
            }

            @Override
            public void endElement(String uri, String localName,
                                   String qName) throws SAXException {

                if (skipTags.contains(qName)) {
                    escape.setValue(false);
                    return;
                }

                if (escape.getValue())
                    return;

                String[] s = qName.split(":");
                if (s.length > 1) {
                    super.endElement("", localName, s[1]);
                } else {
                    super.endElement("", localName, qName);
                }
            }

            @Override
            public void startPrefixMapping(String prefix, String uri) {
            }
        };

        xf.setFeature("http://xml.org/sax/features/namespaces", false);

        SAXSource src = new SAXSource(xf, inputSource);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        transformer.transform(src, new StreamResult(os));
    }
}
