package com.enremmeta.rtb.dao;

/**
 * Service generating GUIDs or UUIDs (TBD)
 *
 * @author Gregory Golberg (<a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public interface DaoIdService {
    long getNextId();
}
