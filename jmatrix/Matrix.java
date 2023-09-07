package jmatrix;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * This class contains various methods for performing numerical 
 * calculations on 2 dimensional arrays. Some of the operations 
 * support parallelism for faster computation by utilizing multiple
 * threads available during runtime. The class is applicable to all
 * the Numerical types that extend the Number class in java (Integer, 
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
	private T mat[][];
	
	/**
	 * The dimensions of the Matrix.
	 */
	public final int colSize,rowSize;
	
	/**
	 * Array buffer used to store the result of the matrix operations 
	 * performed on the Matrix objects.
	 */
	private Double res[][];
	
	/**
	 * Holds the value in nanoseconds to denote the amount of time spent
	 * on a numerical operation.
	 */
	public long timeTaken;
	
	/**
	 * Interal counters that record the start and end time of an arithmetic operation
	 */
	private long startTime,endTime;
	
	private int arrayDimensions[];
	
	/**
	 * Runtime available count to determine the number of the threads that could
	 * possibly be created to speed of parallel calculations.
	 */
	private int processorCapacity = Math.max(1, Runtime.getRuntime().availableProcessors()-1);
	
	/**
	 * Used for displaying a static string representation of the 2 
	 * dimensional array that is computed only once. Primarily used 
	 * when the view method is invoked on Matrix object.
	 */
	private StringBuilder matrixRepresentation = null;
	
	private ArrayList<Thread>threadPool;
	
	private static enum Arithmetic{
		MUL,
		DIV,
		SUB,
		ADD
	}
	
	/**
	 * Accepts a single dimensional array and transforms it into a matrix 
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
	 * Accepts a 2d array as specified during instantiation. This constructor 
	 * does not require the dimensions to be specified explicity as the length 
	 * propery of the array will be used to arrive at those values.
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
	 * Sets the array dimensions that are later used to create a new instance at runtime.
	 */
	private int[] setArrayDimensions(int rowSize,int colSize){
		return new int[]{rowSize,colSize};
	}
	
	/*
	 * Returns a 2 dimensional array based on the type of class instantiated at runtime.
	 */
	private T[][] getArray(Class<?> componentType,int dimensions[]){
		return (T[][])Array.newInstance(componentType, dimensions);
	}
	
	/*
	 * Checks if the matrix could be represented using the specified dimensions 
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
	

	private boolean standardArithmeticApplicable(Matrix mat1, Matrix mat2, Arithmetic Operation){
		return Operation == Arithmetic.MUL?mat1.colSize == mat2.rowSize:
				(mat1.rowSize == mat2.rowSize
				&& mat1.colSize == mat2.colSize);
	}
	
	private void throwInvalidDimensionException(){
		try {
			throw new Exception("Invalid Dimensions for the matrices involved in the arithmetic operation.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	 * because the representation is cached using a stringbuilder object for the subsequent 
	 * calls. However, The first invocation costs O(row*col).
	 */
	public void view(){	
		if(matrixRepresentation == null)buildMatrix();		
		System.out.println(matrixRepresentation);
		
	}
	
	public double sum(){
		startCounter();
		double sum = Arrays.stream(mat)
					.flatMap(Arrays::stream)
					.mapToDouble(x->x.doubleValue())
					.sum();
		stopCounter();
		setTimeTaken();	
		return sum;
	}
	
	/**
	 * Adds the given scalar to all the elements of the matrix 
	 * and returns a new instance of the resultant matrix.
	 */
	public <E extends Number>Matrix<Double> add(E scalarValue){
		initializeResultantMatrix(rowSize,colSize);
		for(int row = 0;row<rowSize;row++){
			for(int col = 0;col<colSize;col++){
				res[row][col] = mat[row][col].doubleValue() + scalarValue.doubleValue();
			}
		}
		return new Matrix<>(res);
	}
	
	/**
	 * Performs matrix addition. If the matrices involved in the operation 
	 * differ in their dimensions but follow the broadcasting rules, the operation
	 * is performed by "broadcasting" the smaller matrix across the larger one. This
	 * is done without creating additional copies of the matrix.
	 */
	public Matrix<Double> add(Matrix other){			
		if(isBroadcastable(this,other)){
			return broadcastedArithmetic(this,other,Arithmetic.ADD,false);			
		}	
		if(standardArithmeticApplicable(this, other,Arithmetic.ADD)){
			return performStandardArithmetic(other,Arithmetic.ADD);
		}
		if(other.rowSize == 1 && other.colSize == 1){
			return add(other.mat[0][0]);
		}
		if(this.rowSize == 1 && this.colSize == 1){
			return other.add(this.mat[0][0]);
		}
		throwInvalidDimensionException();
		return other;
	}
	
	private <E extends Number> Matrix<Double> subtract(E scalarValue,boolean reOrdered){
		initializeResultantMatrix(rowSize,colSize);
		for(int row = 0;row<rowSize;row++){
			for(int col = 0;col<colSize;col++){
				res[row][col] = reOrdered?performArithmetic(Arithmetic.SUB,scalarValue.doubleValue(),mat[row][col].doubleValue())
								: performArithmetic(Arithmetic.SUB,mat[row][col].doubleValue(),scalarValue.doubleValue());
			}
		}
		return new Matrix<>(res);
	}
	
	/**
	 * Substracts the scalar value from all the numbers of the matrix
	 * and returns a new instance of the resultant matrix.
	 */
	public <E extends Number> Matrix<Double>subtract(E scalarValue){
		return subtract(scalarValue,false);
	}
	
	/**
	 * Performs matrix subtraction. If the matrices involved in the operation 
	 * differ in their dimensions but follow the broadcasting rules, the operation
	 * is performed by "broadcasting" the smaller matrix across the larger one. This
	 * is done without creating additional copies of the matrix.
	 */
	public Matrix<Double> subtract(Matrix other){
		if(isBroadcastable(this,other)){
			return broadcastedArithmetic(this,other,Arithmetic.SUB,false);
		}
		if(standardArithmeticApplicable(this,other,Arithmetic.SUB)){
			return performStandardArithmetic(other,Arithmetic.SUB);
		}
		if(other.rowSize == 1 && other.colSize == 1){
			return subtract(other.mat[0][0],false);
		}
		if(this.rowSize == 1 && this.colSize == 1){
			return other.subtract(this.mat[0][0],true);
		}
		throwInvalidDimensionException();	
		return other;
	}
	
	private Matrix<Double>performStandardArithmetic(Matrix other,Arithmetic operation){
		initializeResultantMatrix(rowSize,colSize);
		for(int row = 0;row<rowSize;row++){
			for(int col = 0;col<colSize;col++){
				res[row][col] = performArithmetic(operation,mat[row][col].doubleValue(),other.mat[row][col].doubleValue());
			}
		}
		return new Matrix(res);
	}
	
	/**
	 * Multiplies the matrix numbers with the scalar value passed as input
	 * and returns a new instance of the resultant matrix.
	 */
	public <E extends Number>Matrix<Double> multiply(E scalarValue){
		initializeResultantMatrix(rowSize,colSize);
		for(int row = 0;row<rowSize;row++){
			for(int col = 0;col<colSize;col++){
				res[row][col] = mat[row][col].doubleValue()*scalarValue.doubleValue();
			}
		}
		return new Matrix<Double>(res);		
	}
	
	/**
	 * Performs matrix multiplication.If the matrices involved in the operation 
	 * differ in their dimensions but follow the broadcasting rules, the operation
	 * is performed by "broadcasting" the smaller matrix across the larger one. This
	 * is done without creating additional copies of the matrix.Note that this uses 
	 * the standard algorithm for matrix multiplication that runs in O(N*N*N) 
	 */
	public Matrix<Double> multiply(Matrix other){		
		if(isBroadcastable(this,other)){
			return broadcastedArithmetic(this,other,Arithmetic.MUL,false);		
		}
		if(standardArithmeticApplicable(this,other,Arithmetic.MUL)){
			return standardArithmeticMultiply(other);
		}
		if(other.rowSize == 1 && other.colSize == 1){
			return multiply(other.mat[0][0]);
		}
		if(this.rowSize == 1 && this.colSize == 1){
			return other.multiply(this.mat[0][0]);
		}
		throwInvalidDimensionException();	
		return other;
	}
	
	private Matrix<Double> standardArithmeticMultiply(Matrix mat2){
		initializeResultantMatrix(this.rowSize,mat2.colSize);
		startCounter();		
		for(int i = 0;i<rowSize;i++){			
			for(int z = 0;z<colSize;z++){
				for(int j = 0;j<mat2.colSize;j++){
					res[i][j] += mat[i][z].doubleValue() * mat2.mat[z][j].doubleValue();
				}
			}
		}
		
		stopCounter(); 
		setTimeTaken();
		return new Matrix<>(res);
	}
	
	/* Utility method that computes different arithmetic operations. Broadcasting 
	 * makes use of this method primarily and this helped reuse the code by avoiding 
	 * the need to rewrite braodcasted implementations for different operations.
	 */
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
	 * Verifies if the matrix involved in the operation can be broadcasted based on the rules:
	 * https://numpy.org/doc/stable/user/basics.broadcasting.html#general-broadcasting-rules.
	 * In the case of 2D matrices all we have to check is, if mat1 is a col or row vector, mat2
	 * should have the same row or col size respectively as mat1.
	 */
	private boolean isBroadcastable(Matrix mat1,Matrix mat2){
		boolean isBroadcastable = false;
		if(mat1.colSize == mat2.colSize){
			isBroadcastable |= mat1.rowSize == 1 || mat2.rowSize == 1;
		}
		if(mat1.rowSize == mat2.rowSize) {
			isBroadcastable |= mat1.colSize == 1 || mat2.colSize == 1;
		}			
		return isBroadcastable;
	}
		
	/*
	 * Broadcasting implementation for the arithmetic operations. If the matrices
	 * involved in the operation do not satisfy the broadcasting rules, then
	 * InvalidDimensionForBroadCasting exception is thrown else the smaller matrix 
	 * is extended across the larger matrix and the arithmetic is performed.
	 */	
	private Matrix<Double> broadcastedArithmetic(Matrix mat1,Matrix mat2, Arithmetic operation, boolean reOrdered){				
		int rows = Math.max(mat1.rowSize,mat2.rowSize);
		int cols  = Math.max(mat1.colSize, mat2.colSize);
		
		if(mat2.rowSize != rows || mat2.colSize != cols){ // broadcasting is always done on mat2 so making sure mat2 is the larger matrix.
			return broadcastedArithmetic(mat2,mat1,operation,true); // note that the matrices are swapped.
		}
		
		initializeResultantMatrix(rows,cols);
		
		if(mat1.colSize == mat2.colSize && mat1.rowSize == 1){  //expand across rows when the columns of two matrices are equal and mat1 is a row vector.
			for(int row = 0;row<rows;row++){
				for(int col = 0;col<cols;col++){					
					res[row][col] = performArithmetic(operation,
									reOrdered?mat2.mat[row][col].doubleValue():mat1.mat[0][col].doubleValue(), // a-b != b-a
									reOrdered?mat1.mat[0][col].doubleValue():mat2.mat[row][col].doubleValue());
				}
			}
		}else{
			for(int col = 0;col<cols;col++){
				for(int row = 0;row<rows;row++){
					res[row][col] = performArithmetic(operation,
									reOrdered?mat2.mat[row][col].doubleValue():mat1.mat[row][0].doubleValue(),
									reOrdered?mat1.mat[row][0].doubleValue():mat2.mat[row][col].doubleValue());
				}
			}		
		}
		return new Matrix<Double>(res);		
	}
	
	/**
	 * Returns the maximum value across the entire matrix.
	 */
	public double max(){	
		return Arrays.stream(mat)
						.flatMap(Arrays::stream)
						.mapToDouble(x->x.doubleValue())
						.max().getAsDouble();			
	}
	
	/**
	 * Computes the maximum value across the specified axis of a 2d matrix.
	 * A 2 dimensional matrix has 2 axes: vertical axis that runs along the 
	 * columns and a horizontal axis that runs along each row. An axis value
	 * of 1 computes the max across all the columns for each row and a value
	 * of 0 computes the max across all the rows of for each column.
	 */
	public ArrayList<Double> max(int axis){
		ArrayList<Double>maxNumbers = new ArrayList<>();
		if(axis == 1){
			for(int row = 0;row<rowSize;row++){
				double maxNumber = Double.MIN_VALUE;
				for(int col = 0;col<colSize;col++){
					maxNumber = Math.max(mat[row][col].doubleValue(),maxNumber);
				}
				maxNumbers.add(maxNumber);
			}
		}else if(axis == 0){			
			for(int col = 0;col<colSize;col++){
				double maxNumber = Double.MIN_VALUE;
				for(int row = 0;row<rowSize;row++){
					maxNumber = Math.max(mat[row][col].doubleValue(),maxNumber);
				}
				maxNumbers.add(maxNumber);
			}
		}else {
			try {
				throw new Exception("Invalid parameter value for axis");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return maxNumbers;
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
		setZeros(res,row,col);
	}
	
	private boolean hasReachedProcessorCapacity(){
		return threadPool.size()  == processorCapacity;
	}
	
	private void createOrResetThreadPool(){		
		if(threadPool == null)threadPool = new ArrayList<>();
		threadPool.clear();
	}
	
	/**
	 * Returns a 2 dimensional matrix with the given dimensions filled with zeros.
	 */
	public static Matrix<Double> zeros(int rowSize, int colSize){
		Double zeroMatrix[][] = new Double[rowSize][colSize];
		setZeros(zeroMatrix,rowSize,colSize);
		return new Matrix(zeroMatrix);
	}
	
	/**
	 * Returns a 1 dimensional vector filled with zeros.
	 */
	public static Matrix<Double> zeros(int colSize){
		return zeros(1,colSize);
	}
	
	private static void setZeros(Double matrix[][],int rowSize,int colSize){
		for(int row = 0;row<rowSize;row++){
			Arrays.fill(matrix[row], 0D);
		}
	}
	
	/**
	 * Returns a new instance of the matrix with all the numbers squared.
	 */
	public Matrix<Double> square() {
		Double res[][] = new Double[rowSize][colSize];
		for(int row = 0;row<rowSize;row++) {
			for(int col = 0;col<colSize;col++) {
				res[row][col] = mat[row][col].doubleValue()*mat[row][col].doubleValue();
			}
		}
		return new Matrix(res);
		
	}
	
	/**
	 * Computes the matrix multiplication between two matrices parallely. The number of the threads
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
		
	public void parallelAdd(Matrix<T> other){
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
	 * Once the count of the threads that could be created reaches
	 * the threshold, this method is invoked for the created threads
	 * to terminate after their execution is complete. 
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
	 * Classes that implement the parallel task for several numerical operations.
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
