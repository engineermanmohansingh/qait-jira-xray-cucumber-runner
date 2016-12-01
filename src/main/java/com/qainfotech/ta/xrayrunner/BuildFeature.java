package com.qainfotech.ta.xrayrunner;

import com.qainfotech.ta.xrayrunner.*;

/**
 *
 * @author Ramandeep <ramandeepsingh@qainfotech.com>
 */
public class BuildFeature {
    
    public static void main(String args[]) throws Exception{
        XRayApiClient tr = new XRayApiClient();
        tr.createFeatureFiles();
    }
    
}
