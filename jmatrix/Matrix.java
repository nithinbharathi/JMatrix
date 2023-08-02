package jmatrix;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class contains various methods for performing numerical 
 * calculations on 2 dimensional arrays. Some of the operations 
 * support parallelism for faster computation by utilizing multiple
 * threads available during runtime. The class is applicable to only
 * Numerical types that extend the Number class in java (Integer, 
 * Long, Float, and Double).
 *  
 * @author : Nithin Bharathi 17-Jul-2023
 */

public class Matrix<T extends Number>{
	/**
	 * The array buffer into which elements of the Matrix
	 * are stored. The capacity of the array depends on the
	 * input dimensions provided by the user during instantiation.
	 */
	T mat[][];
	
	/**
	 * The dimensions of the Matrix.
	 */
	int colSize,rowSize;
	
	/**
	 * Array buffer used to store the result of the matrix operations 
	 * performed on the Matrix objects.
	 */
	private Double res[][];
	
	/**
	 * Holds the value in nanoseconds for the amount of time spent on a 
	 * numerical operation.
	 */
	public long timeTaken;
	
	/**
	 * Interal counters that record the start and end time of an arithmetic operation
	 */
	private long startTime,endTime;
	
	private int arrayDimensions[];
	
	/**
	 * runtime available count to determine the number of the threads that could
	 * possibly be created to speed of parallel calculations.
	 */
	private int processorCapacity = Math.max(1, Runtime.getRuntime().availableProcessors()-1);
	
	/**
	 * a static string representation of the 2 dimensional array that is computed only once.
	 * Primarily used when the view method is invoked on Matrix object.
	 */
	StringBuilder matrixRepresentation = null;
	
	private ArrayList<Thread>threadPool;
	
	private static enum Arithmetic{
		MUL,
		DIV,
		SUB,
		ADD
	}
	
	/**
	 * accepts a single dimensional array and transforms it into a matrix 
	 * based on the dimensions specified during instantiation.
	 * @param mat
	 * @param rowSize
	 * @param colSize
	 */	
	public Matrix(T mat[],int rowSize, int colSize){		
		this.colSize = colSize;
		this.rowSize = rowSize;
		this.arrayDimensions = setArrayDimensions(rowSize,colSize);
		this.mat = transform(mat);
	}
	
	/**
	 * accepts a 2d array as specified during instantiation. This constructor 
	 * does not require the dimensions to be specified explicity as the length 
	 * propery of the array will used to arrive at those values.
	 * @param mat
	 */
	public Matrix(T mat[][]){
		this.rowSize = mat.length;
		this.colSize = mat[0].length;
		this.arrayDimensions = setArrayDimensions(this.rowSize,this.colSize);
		this.mat  = mat;
	}
	
	/*
	 * Converts a linear array of numbers into a matrix with dimensions specified 
	 * by the user during the time of instantiation.
	 */
	private T[][] transform(T mat[]){
		validateDimensions(mat.length);
		T matrix[][] = getArray(mat.getClass().getComponentType(), arrayDimensions);
		int row = 0,col=0;
		for(int i = 0;i<mat.length;i++){
			matrix[row][col++] = mat[i];
			col%=colSize;
			row = col==0?row+1:row;
		}
		return matrix;
	}
	
	/*
	 * sets the array dimensions that are later used to create a new instance at runtime.
	 */
	private int[] setArrayDimensions(int rowSize,int colSize){
		return new int[]{rowSize,colSize};
	}
	
	/*
	 * returns a 2 dimensional array based on the type of class instantiated at runtime.
	 */
	private T[][] getArray(Class<?> componentType,int dimensions[]){
		return (T[][])Array.newInstance(componentType, dimensions);
	}
	
	/*
	 * checks if the matrix could be represented using the specified dimensions 
	 */
	private void validateDimensions(int len){
		if(rowSize*colSize != len || len == 0){
			try {
				throw new Exception("Given dimensions do not match the input matrix");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Constructs a matrix representational view of the Matrix object.
	 */
	private StringBuilder buildMatrix(){
		matrixRepresentation = new StringBuilder();
		for(int i =0;i<rowSize;i++){
			for(int j =0;j<colSize;j++){
				matrixRepresentation.append(mat[i][j]);
				matrixRepresentation.append(" ");
			}
			matrixRepresentation.append("\n");
		}
		return matrixRepresentation;
	}
	
	/**
	 * Prints out the matrix representation of the current instance. Note that the running 
	 * time  of this method is always constant after the first time this method is invoked 
	 * because the view is cachedfor the subsequent calls. However, The first invocation costs O(row*col).
	 */
	public void view(){	
		if(matrixRepresentation == null)buildMatrix();		
		System.out.println(matrixRepresentation);
		
	}
	
	public void result(){
		if(res == null){
			System.out.println("Resultant matrix is empty because no arithmetic operation was performed for this object");
			return;
		}
		
		for(int i = 0;i<res.length;i++){
			for(int j = 0;j<res[0].length;j++){
				System.out.print(res[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	public double sum(){
		startCounter();
		double sum = Arrays.stream(res).parallel().mapToDouble(x->x[0]).sum();
		stopCounter();
		setTimeTaken();	
		return sum;
	}
	
	/**
	 * adds the given scalar to all the elements of the matrix and returns a new instance.
	 */
	public <E extends Number>Matrix add(E scalarValue){
		initializeResultantMatrix(rowSize,colSize);
		for(int row = 0;row<rowSize;row++){
			for(int col = 0;col<colSize;col++){
				res[row][col] = mat[row][col].doubleValue() + scalarValue.doubleValue();
			}
		}
		return new Matrix(res);
	}
	
	public Matrix add(Matrix other){			
		if(requiresBroadcasting(other)){
			return broadcastedArithmetic(other,this,Arithmetic.ADD);			
		}				
		initializeResultantMatrix(rowSize,colSize);		
		for(int i =0;i<rowSize;i++){
			for(int j =0;j<colSize;j++){
				res[i][j] = mat[i][j].doubleValue() + other.mat[i][j].doubleValue();
			}
		}
		return new Matrix(res);
	}
	
	public <E extends Number>Matrix multiply(E scalarValue){
		initializeResultantMatrix(rowSize,colSize);
		for(int row = 0;row<rowSize;row++){
			for(int col = 0;col<colSize;col++){
				res[row][col] = mat[row][col].doubleValue()*scalarValue.doubleValue();
			}
		}
		return new Matrix(res);
		
	}
	public Matrix multiply(Matrix other){		
		
		if(requiresBroadcasting(other)){
			return broadcastedArithmetic(other,this,Arithmetic.MUL);
			
		}		
		initializeResultantMatrix(this.rowSize,other.colSize);
		startCounter();		
		for(int i = 0;i<rowSize;i++){			
			for(int z = 0;z<colSize;z++){
				for(int j = 0;j<other.colSize;j++){
					res[i][j] += mat[i][z].doubleValue() * other.mat[z][j].doubleValue();
				}
			}
		}
		
		stopCounter(); 
		setTimeTaken();
		return new Matrix(res);
	}
	
	private void logBroadcastException(){
		try {
			throw new Exception("Invalid dimensions for broadcasting to be done.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private double performArithmetic(Arithmetic operation,double operand1,double operand2){
		double result = 0;		
		switch(operation){
			case MUL:
				result = operand1 * operand2;
				break;
			case DIV:
				result = operand1/operand2;
				break;
			case SUB:
				result = operand1 - operand2;
				break;
			case ADD:
				result = operand1+operand2;
				break;
		}				
		return result;
	}
	
	
	/*
	 * verifies if the matrix involved in the operation can be broadcasted based on the rules:
	 * https://numpy.org/doc/stable/user/basics.broadcasting.html#general-broadcasting-rules
	 */
	private boolean isBroadcastable(Matrix other){
		if(other.colSize == colSize){
			return other.rowSize == 1 || this.rowSize == 1;
		}else if(other.rowSize == rowSize) {
			return this.colSize == 1 || other.colSize == 1;
		}
			
		return false;
	}
	
	private boolean requiresBroadcasting(Matrix mat){
		return rowSize != mat.rowSize  || colSize != mat.colSize;
	}
	
	/*
	 * broadcasting implementations for the arithmetic operations. If the matrices
	 * involved in the operation do not satisfy the broadcasting rules, InvalidDimensionForBroadCasting 
	 * exception is thrown.
	 */	

	private Matrix broadcastedArithmetic(Matrix mat1,Matrix mat2, Arithmetic operation){
		if(!isBroadcastable(mat1)){
			logBroadcastException();
			return this;
		}
		if(mat1.rowSize != 1 && mat1.colSize != 1){
			return broadcastedArithmetic(mat2,mat1,operation);
		}
		
		int row = Math.max(mat1.rowSize,mat2.rowSize);
		int col  = Math.max(mat1.colSize, mat2.colSize);
		
		initializeResultantMatrix(row,col);
		
		if(mat1.rowSize == 1){
			for(int i = 0;i<row;i++){
				for(int j = 0;j<col;j++){					
					res[i][j] = performArithmetic(operation,mat2.mat[i][j].doubleValue(),mat1.mat[0][j].doubleValue());
				}
			}
		}else{
			for(int i = 0;i<row;i++){
				for(int j = 0;j<col;j++){					
					res[j][i] = performArithmetic(operation,mat2.mat[j][i].doubleValue(),mat1.mat[j][0].doubleValue());
				}
			}
		}
		return new Matrix(res);
		
	}
	
	
	/*
	 * startCounter, stopCounter, & setTimeTaken are used all together at the same
	 * time for recording the running time of a particular numerical operation. Currently
	 * these methods are invoked on specific operations. This has to be further extended
	 * as an optional parameter to let the user decide if the running time has to be recorded.
	 */
	
	private void startCounter(){
		startTime = System.nanoTime();
	}
	
	private void stopCounter(){
		endTime = System.nanoTime();
	}
	
	private void setTimeTaken(){
		timeTaken = endTime-startTime;
	}
	
	private void initializeResultantMatrix(int row, int col){
		res = new Double[row][col];
	}
	
	private boolean hasReachedProcessorCapacity(){
		return threadPool.size()  == processorCapacity;
	}
	
	private void createOrResetThreadPool(){		
		if(threadPool == null)threadPool = new ArrayList<>();
		threadPool.clear();
	}
	
	/**
	 * computes the matrix multiplication between two matrices parallely. The number of the threads
	 * that operate on the matrix is determined by the number of processors that system has.
	 */
	public void parallelMultiply(Matrix other){
		initializeResultantMatrix(other.rowSize,other.colSize);
		startCounter();
		
		for(int i = 0;i<rowSize;i++){
			MultiplierTask multiplicationParams = new MultiplierTask(this,other,i);
			Thread thread = new Thread(multiplicationParams);
			threadPool.add(thread);
			thread.start();
			if(!hasReachedProcessorCapacity()){
				waitForThreads();
			}
		}
		
		stopCounter();
		setTimeTaken();
		createOrResetThreadPool();
		
	}
	
	
	public void parallelAdd(Matrix other){
		initializeResultantMatrix(other.rowSize,other.colSize);
		startCounter();
		
		for(int row = 0;row<rowSize;row++){
			additionTask additionParams = new additionTask(this,other, row);						
			Thread additionThread = new Thread(additionParams);
			additionThread.start();
			threadPool.add(additionThread);
			 if(hasReachedProcessorCapacity()){
				waitForThreads();
			}
			
		}
		
		stopCounter();
		setTimeTaken();
		createOrResetThreadPool();
		
	}
	
	//todo
	public void parallelSubtract(Matrix other){
		
	}
	
	//todo
	public void parallelDivide(Matrix other){
		
	}
	
	/**
	 * 
	 */
	private void waitForThreads(){
		for(Thread thread: threadPool){
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
	
	/**
	 * classes that implement the parallel task for several numerical operations.
	 *
	 */
	private class MultiplierTask implements Runnable{		
		private Matrix mat1 , mat2;
		private int row;
		public MultiplierTask(Matrix mat1, Matrix mat2, int row){
			this.mat1 = mat1;
			this.mat2 = mat2;
			this.row = row;
		}
		@Override
		public void run() {			
			for(int itr = 0;itr<mat1.colSize;itr++){
				for(int col = 0;col<mat2.colSize;col++){
					mat1.res[row][col] += (mat1.mat[row][itr].doubleValue()*mat2.mat[itr][col].doubleValue());
				}
			}
		}
		
	}
	
	private class additionTask implements Runnable{
		private Matrix mat1,mat2;
		private int row;
		public additionTask(Matrix mat1, Matrix mat2, int row){
			this.mat1 = mat1;
			this.mat2 = mat2;
			this.row = row;
		}
		@Override
		public void run() {
			for(int col = 0;col<mat1.colSize;col++){
				mat1.res[row][col] = mat1.mat[row][col].doubleValue() + mat2.mat[row][col].doubleValue();
			}
		}
		
	}
}
