package com.splicemachine.derby.impl.sql.execute.operations;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.splicemachine.derby.utils.marshall.RowDecoder;
import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.iapi.services.loader.GeneratedMethod;
import org.apache.derby.iapi.sql.Activation;
import org.apache.derby.iapi.sql.execute.ExecRow;
import org.apache.derby.iapi.sql.execute.NoPutResultSet;
import org.apache.derby.iapi.types.DataValueDescriptor;
import org.apache.log4j.Logger;

import com.splicemachine.derby.iapi.sql.execute.SpliceNoPutResultSet;
import com.splicemachine.derby.iapi.sql.execute.SpliceOperation;
import com.splicemachine.derby.iapi.sql.execute.SpliceOperationContext;
import com.splicemachine.derby.iapi.sql.execute.SpliceRuntimeContext;
import com.splicemachine.derby.iapi.storage.RowProvider;
import com.splicemachine.utils.SpliceLogUtils;

public class NestedLoopJoinOperation extends JoinOperation {
	private static Logger LOG = Logger.getLogger(NestedLoopJoinOperation.class);
	protected ExecRow rightTemplate;
	protected NestedLoopIterator nestedLoopIterator;
	protected boolean isHash;
	protected static List<NodeType> nodeTypes;
	
	static {
		nodeTypes = new ArrayList<NodeType>();
		nodeTypes.add(NodeType.MAP);
		nodeTypes.add(NodeType.SCROLL);
	}
	
	public NestedLoopJoinOperation() {
		super();
	}
	
	public NestedLoopJoinOperation(SpliceOperation leftResultSet,
			   int leftNumCols,
			   SpliceOperation rightResultSet,
			   int rightNumCols,
			   Activation activation,
			   GeneratedMethod restriction,
			   int resultSetNumber,
			   boolean oneRowRightSide,
			   boolean notExistsRightSide,
			   double optimizerEstimatedRowCount,
			   double optimizerEstimatedCost,
			   String userSuppliedOptimizerOverrides) throws StandardException {
		super(leftResultSet,leftNumCols,rightResultSet,rightNumCols,activation,restriction,
				resultSetNumber,oneRowRightSide,notExistsRightSide,optimizerEstimatedRowCount,
				   optimizerEstimatedCost,userSuppliedOptimizerOverrides);	
		this.isHash = false;
        init(SpliceOperationContext.newContext(activation));
        recordConstructorTime(); 
	}
	
	@Override
	public List<NodeType> getNodeTypes() {
//		SpliceLogUtils.trace(LOG, "getNodeTypes");
		return nodeTypes;
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
//		SpliceLogUtils.trace(LOG,"readExternal");
		super.readExternal(in);
		isHash = in.readBoolean();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
//		SpliceLogUtils.trace(LOG,"writeExternal");
		super.writeExternal(out);
		out.writeBoolean(isHash);
	}

	@Override
	public void init(SpliceOperationContext context) throws StandardException{
//		SpliceLogUtils.trace(LOG,"init called");
		super.init(context);
		rightTemplate = activation.getExecutionFactory().getValueRow(rightNumCols);
	}

	@Override
	public NoPutResultSet executeScan(SpliceRuntimeContext runtimeContext) throws StandardException {
		SpliceLogUtils.trace(LOG, "executeScan");
		final List<SpliceOperation> operationStack = new ArrayList<SpliceOperation>();
		this.generateLeftOperationStack(operationStack);
		return new SpliceNoPutResultSet(activation,this, getReduceRowProvider(this,getRowEncoder(runtimeContext).getDual(getExecRowDefinition()),runtimeContext));
	}

    @Override
    public RowProvider getMapRowProvider(SpliceOperation top, RowDecoder rowDecoder, SpliceRuntimeContext spliceRuntimeContext) throws StandardException {
        //push the computation to the left side of the join
        //TODO -sf- push this to the largest table in the join (or make the largest table always be the left)
        return leftResultSet.getMapRowProvider(top, rowDecoder, spliceRuntimeContext);
    }

    @Override
	public String toString() {
		return "NestedLoop"+super.toString();
	}

	@Override
	public ExecRow nextRow(SpliceRuntimeContext spliceRuntimeContext) throws StandardException, IOException {
        return next(false, spliceRuntimeContext);
	}

    protected ExecRow leftNext(SpliceRuntimeContext spliceRuntimeContext) throws StandardException, IOException {
        return leftResultSet.nextRow(spliceRuntimeContext);
    }

    protected ExecRow next(boolean outerJoin, SpliceRuntimeContext spliceRuntimeContext) throws StandardException, IOException {
        beginTime = getCurrentTimeMillis();
        // loop until NL iterator produces result, or until left side exhausted
        while ((nestedLoopIterator == null || !nestedLoopIterator.hasNext())
                && ( (leftRow = leftNext(spliceRuntimeContext)) != null) ){
            if (nestedLoopIterator != null){
                nestedLoopIterator.close();
            }
            leftResultSet.setCurrentRow(leftRow);
            rowsSeenLeft++;
            nestedLoopIterator = new NestedLoopIterator(leftRow, isHash, outerJoin);
        }
        if (leftRow == null){
            mergedRow = null;
            setCurrentRow(mergedRow);
            return mergedRow;
        } else {
            SpliceLogUtils.trace(LOG, "nextRow loop iterate next ");
            ExecRow next = nestedLoopIterator.next();
            SpliceLogUtils.trace(LOG,"nextRow returning %s",next);
            setCurrentRow(next);
            nextTime += getElapsedMillis(beginTime);
            rowsReturned++;
            return next;
        }
    }
	
	@Override
	public void	close() throws StandardException, IOException {
		SpliceLogUtils.trace(LOG, "close in NestdLoopJoin");
		beginTime = getCurrentTimeMillis();
		clearCurrentRow();
		super.close();
		closeTime += getElapsedMillis(beginTime);
	}

    @Override
    public String prettyPrint(int indentLevel) {
        return "NestedLoopJoin:" + super.prettyPrint(indentLevel);
    }

    protected class NestedLoopIterator implements Iterator<ExecRow> {
		protected ExecRow leftRow;
		protected NoPutResultSet probeResultSet;
		private boolean populated;
        private boolean returnedRight=false;
        private boolean outerJoin = false;

		NestedLoopIterator(ExecRow leftRow, boolean hash, boolean outerJoin) throws StandardException {
			SpliceLogUtils.trace(LOG, "NestedLoopIterator instantiated with leftRow %s",leftRow);
			this.leftRow = leftRow;
			if (hash) {
				SpliceLogUtils.trace(LOG, "Iterator - executeProbeScan on %s",getRightResultSet());
				probeResultSet = (getRightResultSet()).executeProbeScan();
			}
			else {
				SpliceLogUtils.trace(LOG, "Iterator - executeScan on %s",getRightResultSet());
				probeResultSet = (getRightResultSet()).executeScan(new SpliceRuntimeContext());
			}
			probeResultSet.openCore();
			populated=false;
            this.outerJoin = outerJoin;
		}
		
		@Override
		public boolean hasNext() {
			SpliceLogUtils.trace(LOG, "hasNext called");
			if(populated)return true;
			rightResultSet.clearCurrentRow();
			try {
				ExecRow rightRow = probeResultSet.getNextRowCore();
                /*
                 * notExistsRightSide indicates that we need to reverse the logic of the join.
                 */
                if(notExistsRightSide){
                    rightRow = (rightRow == null) ? rightTemplate : null;
                }
                if (outerJoin && rightRow == null){
                    rightRow = getEmptyRightRow();
                }
                if(rightRow!=null){
                    if(oneRowRightSide &&returnedRight){
                        //skip this row, because it's already been found.
                        returnedRight = false;
                        populated=false;
                        return false;
                    }

                    SpliceLogUtils.trace(LOG, "right has result %s", rightRow);
					/*
					 * the right result set's row might be used in other branches up the stack which
					 * occur under serialization, so the activation has to be sure and set the current row
					 * on rightResultSet, or that row won't be serialized over, potentially breaking ProjectRestricts
					 * up the stack.
					 */
                    rightResultSet.setCurrentRow(rightRow); //set this here for serialization up the stack
                    rowsSeenRight++;
                    nonNullRight();
                    returnedRight=true;

                    mergedRow = JoinUtils.getMergedRow(leftRow,rightRow,false,rightNumCols,leftNumCols,mergedRow);
                }else {
					SpliceLogUtils.trace(LOG, "already has seen row and no right result");
					populated = false;
					return false;
				}

				if (restriction != null) {
					DataValueDescriptor restrictBoolean = restriction.invoke();
					if ((! restrictBoolean.isNull()) && restrictBoolean.getBoolean()) {
						SpliceLogUtils.trace(LOG, "restricted row %s",mergedRow);
						populated=false;
						hasNext();
					}
				}
			} catch (StandardException e) {
				SpliceLogUtils.logAndThrowRuntime(LOG, "hasNext Failed", e);
				try {
					close();
				} catch (StandardException e1) {
					SpliceLogUtils.logAndThrowRuntime(LOG, "close Failed", e1);
				}
				populated=false;
				return false;
			}
			populated=true;
			return true;
		}

        protected void nonNullRight() {
            //no op for inner loops
        }

        protected ExecRow getEmptyRightRow() throws StandardException {
            return null;  //for inner loops, return null here
        }

        @Override
		public ExecRow next() {
			SpliceLogUtils.trace(LOG, "next row=%s",mergedRow);
			populated=false;
			return mergedRow;
		}

		@Override
		public void remove() {
			SpliceLogUtils.trace(LOG, "remove");
		}
		public void close() throws StandardException {
			SpliceLogUtils.trace(LOG, "close, closing probe result set");
			beginTime = getCurrentTimeMillis();
			if (!isOpen)
				return;
			probeResultSet.close();
			isOpen = false;
			closeTime += getElapsedMillis(beginTime);
		}
	}
	
//	@Override
//	public long getTimeSpent(int type)
//	{
//		long totTime = constructorTime + openTime + nextTime + closeTime;
//
//		if (type == NoPutResultSet.CURRENT_RESULTSET_ONLY)
//			return	totTime - leftResultSet.getTimeSpent(ENTIRE_RESULTSET_TREE)
//							- rightResultSet.getTimeSpent(ENTIRE_RESULTSET_TREE);
//		else
//			return totTime;
//	}
	
}
