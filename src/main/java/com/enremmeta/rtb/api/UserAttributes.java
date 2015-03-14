package com.enremmeta.rtb.api;

/**
 * User attributes, what are not only being read from DB, but also changed and written back to DB.
 * 
 * @see UserFrequencyCapAttributes
 * @see UserExperimentAttributes
 * 
 * @author Vladimir Zamyatin (<a href="mailto:boba@opendsp.com">boba@opendsp.com</a>)
 *         <p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *         </p>
 */

public class UserAttributes {
    private UserFrequencyCapAttributes userFrequencyCap;
    private UserExperimentAttributes userExperimentData;

    public UserAttributes(UserExperimentAttributes userExperimentData,
                    UserFrequencyCapAttributes userFrequencyCap) {
        this.setUserExperimentData(userExperimentData);
        this.setUserFrequencyCap(userFrequencyCap);
    }

    public UserFrequencyCapAttributes getUserFrequencyCap() {
        return userFrequencyCap;
    }

    public void setUserFrequencyCap(UserFrequencyCapAttributes userFrequencyCap) {
        this.userFrequencyCap = userFrequencyCap;
    }

    public UserExperimentAttributes getUserExperimentData() {
        return userExperimentData;
    }

    public void setUserExperimentData(UserExperimentAttributes userExperimentData) {
        this.userExperimentData = userExperimentData;
    }
}
