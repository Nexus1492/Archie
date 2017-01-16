//License
package archie_v1.fileHelpers;

import java.nio.file.Path;

public class databaseFile extends FileHelper {

    //File specific setters
    public databaseFile(Path filePath, boolean Islandora) {
        super(filePath, Islandora);
        setRecordThroughTika(MetadataKey.CreatorName, "creator");
        setRecordThroughTika(MetadataKey.CreatorAffiliation, "extended-properties_Company");
    }
}
