package org.mos.vrfblk;


import org.mos.mcore.odb.ODBDao;

import onight.tfw.ojpa.api.ServiceSpec;

public class ODSVRFVoteDao extends ODBDao {

	public ODSVRFVoteDao(ServiceSpec serviceSpec) {
		super(serviceSpec);
	}

	@Override
	public String getDomainName() {
		return "vrf.vote";
	}

	
}
