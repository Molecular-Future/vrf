package org.mos.vrfblk.utils;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.mos.mcore.model.Transaction.TransactionInfo;


public class TxArrays implements Serializable, IStorable {
	private static final long serialVersionUID = 5829951203336980749L;

	public List<TransactionInfo> tx = new ArrayList<>();
	public byte[] data;
	public BigInteger bits;
	public String hexKey;

	public void toBytes(ByteBuffer buff) {
		byte[] keybb = hexKey.getBytes();
		byte bitsbb[] = bits.toByteArray();

		int totalSize = (4 + keybb.length) + (4 + data.length) + (4 + bitsbb.length);

		buff.putInt(totalSize);

		buff.putInt(keybb.length);
		buff.put(keybb);

		buff.putInt(data.length);
		buff.put(data);

		buff.putInt(bitsbb.length);
		buff.put(bitsbb);
	}

	public void fromBytes(ByteBuffer buff) {
		int totalSize = buff.getInt();

		int len = buff.getInt();
		byte[] hexKeybb = new byte[len];
		buff.get(hexKeybb);
		hexKey = new String(hexKeybb);

		len = buff.getInt();
		data = new byte[len];
		buff.get(data);

		len = buff.getInt();
		byte bitsbb[] = new byte[len];
		buff.get(bitsbb);
		bits = new BigInteger(bitsbb);

	}

	public synchronized void setBits(BigInteger bits) {
		this.bits = this.bits.or(bits);
	}

	public TxArrays() {

	}

	public TxArrays(String hexKey, byte[] data, BigInteger bits) {
		super();
		this.hexKey = hexKey;
		this.data = data;
		this.bits = bits;
	}

	@Override
	public long calcSize() {
		return data.length + hexKey.length() * 3 + bits.bitLength() / 8;
	}

	@Override
	public String getHexKey() {
		return this.hexKey;
	}

	@Override
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public BigInteger getBits() {
		return bits;
	}
	
}
