package de.vzg.mets;

import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.services.staticcontent.MCRObjectStaticContentGenerator;

public class MCREpicurStaticContentTransformer extends MCRObjectStaticContentGenerator {

    public MCREpicurStaticContentTransformer(String configID) {
        super(configID);
    }

    @Override
    protected boolean filter(MCRObject object) {
        if(!MCRMODSWrapper.isSupported(object)){
            return false;
        }

        return new MCRMODSWrapper(object).getElements("mods:identifier[@type='urn']").size()>0;
    }
}
