package com.qainfotech.ta.xrayrunner;


/**
 *
 * @author Ramandeep <ramandeepsingh@qainfotech.com>
 */
public class PostResults {
    
    public static void main(String args[]) throws Exception{
        XRayApiClient tr = new XRayApiClient();
        tr.pushResult();
    }
    
}
