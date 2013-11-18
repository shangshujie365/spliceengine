package com.splicemachine.derby.utils.marshall;

import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.iapi.sql.execute.ExecRow;
import org.apache.hadoop.hbase.KeyValue;

/**
 * @author Scott Fines
 * Date: 11/15/13
 */
public class PairDecoder {
		private final KeyDecoder keyDecoder;
		private final KeyHashDecoder rowDecoder;
		private final ExecRow templateRow;

		public PairDecoder(KeyDecoder keyDecoder,
											 KeyHashDecoder rowDecoder,
											 ExecRow templateRow) {
				this.keyDecoder = keyDecoder;
				this.rowDecoder = rowDecoder;
				this.templateRow = templateRow;
		}

		public ExecRow decode(KeyValue data) throws StandardException{
				templateRow.resetRowArray();
				keyDecoder.decode(data.getBuffer(),data.getRowOffset(),data.getRowLength(),templateRow);
				rowDecoder.set(data.getBuffer(),data.getValueOffset(),data.getValueLength());
				rowDecoder.decode(templateRow);
				return templateRow;
		}

		public int getKeyPrefixOffset(){
				return keyDecoder.getPrefixOffset();
		}
}
