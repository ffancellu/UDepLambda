package deplambda.util;

import deplambda.DRT.XDRS;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Created by ffancellu on 12/04/2017.
 */
public class XMLReader {

    public static XDRS parseXML(File drs) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(drs);

        doc.getDocumentElement().normalize();

        Node xdrs = doc.getElementsByTagName("xdrs").item(0);
        XDRS xdrsRoot = new XDRS();
        xdrsRoot.parseContent(xdrs);

        return xdrsRoot;
    }
}
