//License
package archie_v1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

public class archieXMLcreator {
    
    public Document CreateDocument(DefaultMutableTreeNode dirTree) {
        Element root = new Element("root");
        CreateElements(dirTree, root);

        return new Document(root);
    }

    private void CreateElements(DefaultMutableTreeNode dir, Element element) {
        Path filePath = (Path) dir.getUserObject();
        Element file = new Element("file");
        Element name = new Element("name");
        name.setText(filePath.getFileName().toString());
        file.addContent(name);

        if (filePath.toFile().isFile()) {
        setFileElements(filePath, file);
        } else {
            file.setName("folder");
            setFolderElements(dir, file);
            for (Object files : Collections.list(dir.children())) {
                CreateElements((DefaultMutableTreeNode) files, file);
            }
        }
        element.addContent(file);
    }
    
    private FileHelper getFile(Path filePath){
        //Todo: context specific file type selection
        basicFile bf = new basicFile(filePath);
        return bf;
    }
    
    //setFileElements sets several content types for files, such as filetype and modification date.
    private void setFileElements(Path filePath, Element file) {
        FileHelper fh = getFile(filePath);
        Map<FileHelper.MetaDataType, String> fileMap = fh.getMetaData();
        
        for (FileHelper.MetaDataType attribute : fileMap.keySet()){
            Element metaData = new Element(attribute.toString());
            metaData.setText(fileMap.get(attribute));
            file.addContent(metaData);
        }
    }

    //setFileElements sets several content types for folders, such as filecount.
    private void setFolderElements(DefaultMutableTreeNode folderNode, Element folder) {
            Element fileCount = new Element("filecount");
            fileCount.setText(Integer.toString(folderNode.getChildCount()));
            folder.addContent(fileCount);
    }
    
    public void saveToXML(Document xml){
        XMLOutputter outputter = new XMLOutputter();

        try {
            PrintWriter writer = new PrintWriter("basic_xml.xml");
            outputter.output(xml, writer);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ARCHIE.class.getName()).log(Level.SEVERE, "File not found", ex);
        } catch (IOException ex) {
            Logger.getLogger(ARCHIE.class.getName()).log(Level.SEVERE, "Writer not found", ex);
        }
    }
}
