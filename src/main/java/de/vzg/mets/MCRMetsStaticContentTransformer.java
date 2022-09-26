package de.vzg.mets;

import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.services.staticcontent.MCRObjectStaticContentGenerator;

import java.nio.file.Files;

public class MCRMetsStaticContentTransformer extends MCRObjectStaticContentGenerator {

    public MCRMetsStaticContentTransformer(String configID) {
        super(configID);
    }

    @Override
    protected boolean filter(MCRObject object) {
        if(object.getStructure().getDerivates().size()==0){
            return false;
        }

        return object.getStructure().getDerivates().stream().map(MCRMetaLinkID::getXLinkHref)
                .filter(MCRObjectID::isValid)
                .map(MCRObjectID::getInstance)
                .filter(MCRMetadataManager::exists)
                .anyMatch(der -> Files.exists(MCRPath.getPath(der.toString(), "/mets.xml")));
    }
}
