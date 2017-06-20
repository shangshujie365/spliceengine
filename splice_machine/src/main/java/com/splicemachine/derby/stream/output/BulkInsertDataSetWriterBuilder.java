/*
 * Copyright (c) 2012 - 2017 Splice Machine, Inc.
 *
 * This file is part of Splice Machine.
 * Splice Machine is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3, or (at your option) any later version.
 * Splice Machine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with Splice Machine.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.splicemachine.derby.stream.output;

import com.splicemachine.db.iapi.sql.execute.ExecRow;
import com.splicemachine.db.iapi.types.RowLocation;
import com.splicemachine.derby.impl.sql.execute.sequence.SpliceSequence;
import com.splicemachine.derby.stream.iapi.OperationContext;
import com.splicemachine.si.api.txn.TxnView;

public interface BulkInsertDataSetWriterBuilder extends InsertDataSetWriterBuilder {


    BulkInsertDataSetWriterBuilder bulkImportDirectory(String bulkImportDirectory);

    BulkInsertDataSetWriterBuilder samplingOnly(boolean samplingOnly);

    BulkInsertDataSetWriterBuilder outputKeysOnly(boolean outputKeysOnly);

    BulkInsertDataSetWriterBuilder skipSampling(boolean skipSampling);

    BulkInsertDataSetWriterBuilder indexName(String indexName);

}