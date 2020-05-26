package org.mos.vrfblk.utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public interface IStorable {

	void toBytes(ByteBuffer buff);

	void fromBytes(ByteBuffer buff);
	
	String getHexKey();
	
	long calcSize();
	
	BigInteger getBits();
	
	byte[] getData();

}