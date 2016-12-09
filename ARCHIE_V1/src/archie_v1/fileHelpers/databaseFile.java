//License
package archie_v1.fileHelpers;

import java.nio.file.Path;
import java.util.Map;
import org.jdom2.Element;
import org.jdom2.Namespace;

public class databaseFile extends FileHelper {

    public databaseFile(Path filePath) {
        super(filePath);
    }

    @Override
    public Element[] getRelevantElements(Namespace ns) {
        Element[] elArray = new Element[4];
        
        elArray[0] = getTitle(ns);
        elArray[1] = getCreator(ns, "creator", "extended-properties_Company");
        elArray[2] = getOriginInfo(ns);
        elArray[3] = getTypeOfResource(ns);
        
        return elArray;
    }
    
}