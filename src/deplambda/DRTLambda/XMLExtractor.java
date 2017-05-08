package deplambda.DRTLambda;

import deplambda.DRT.XDRS;
import deplambda.util.XMLReader;
import edu.stanford.nlp.util.Triple;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ffancellu on 26/04/2017.
 */
public class XMLExtractor {

    public static ArrayList<Triple<String,String,XDRS>> processOne(File rootDir)
            throws ParserConfigurationException, SAXException, IOException {
        ArrayList<Triple<String,String,XDRS>> xdrsList = new ArrayList<>();
        processFolder(rootDir,xdrsList);
        return xdrsList;
    }

    public static void processFolder(File rootDir,
                                     ArrayList<Triple<String,String,XDRS>> roots)
            throws IOException, SAXException, ParserConfigurationException {
        String[] subDirs = rootDir.list();
        for (String subDirPath : subDirs) {
            File child = new File(rootDir, subDirPath);
            if (child.isDirectory()) {
                processFolder(child, roots);
            } else if (child.toString().endsWith("en.drs.xml")) {
                System.out.println("Parsing: " + subDirPath + " " + child.toString());
                XDRS root = XMLReader.parseXML(child);
                String[] pathParts = child.toString().split("/");
                String par = pathParts[pathParts.length-3];
                String doc = pathParts[pathParts.length-2];
                roots.add(Triple.makeTriple(par,doc,root));
            }
        }
    }

    public static ArrayList<Triple<String,String,XDRS>> traverseFolder(File rootDir, List<String> subFolders)
            throws ParserConfigurationException, SAXException, IOException {

        ArrayList<Triple<String,String,XDRS>> roots = new ArrayList<>();
        for (String subFolder:subFolders){
            if (!subFolder.equals(".DS_Store")) {
                File parDir = new File(rootDir, subFolder);
                System.out.println(parDir);
                processFolder(parDir, roots);
            }
        }
        return roots;
    }
}


