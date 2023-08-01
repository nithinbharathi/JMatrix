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
	private double res[][];
	
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
	private int threadCapacity = Math.max(1, Runtime.getRuntime().availableProcessors()-1);
	
	/**
	 * a static string representation of the 2 dimensional array that is computed only once.
	 * Primarily used when the view method is invoked on Matrix object.
	 */
	StringBuilder matrixRepresentation = null;
	
	private ArrayList<Thread>threadPool;
	
	public Matrix(T mat[],int rowSize, int colSize){		
		this.colSize = colSize;
		this.rowSize = rowSize;
		this.arrayDimensions = setArrayDimensions(rowSize,colSize);
		this.mat = transform(mat);
	}
	
	/**
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
	
	/**
	 * sets the array dimensions that are later used to create a new instance at runtime.
	 */
	private int[] setArrayDimensions(int rowSize,int colSize){
		return new int[]{rowSize,colSize};
	}
	
	/**
	 * returns a 2 dimensional array based on the type of class instantiated at runtime.
	 */
	private T[][] getArray(Class<?> componentType,int dimensions[]){
		return (T[][])Array.newInstance(componentType, dimensions);
	}
	
	/**
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
	
	/**
	 * constructs a matrix representational view of the Matrix object. Note that the running time of this 
	 * method is always constant after the first time this method is invoked. The first invocation costs O(N).
	 */
	public void view(){	
		if(matrixRepresentation == null)buildMatrix();
		
		System.out.println(matrixRepresentation);
		
	}
	
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
	
/*	public void add(Matrix other){	
		Matrix broadcastedOther,broadcastedCurrent;
		if(requiresBroadcasting(other)){
			broadcastedOther = broadCast(other);
		}
		if(requiresBroadcasting(this)){
			broadcastedCurrent = braodCast(this);
		}
		for(int i = 0;i<broadcastedCurrent.mat.length;i++){
			for(int j =0;j<broadcastedCurrent.mat.length;j++){
				res[i][j] = mat[i][j]+other.mat[i][j];
			}
		}
	}*/
	
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
	
	private void createOrResetThreadPool(){		
		if(threadPool == null)threadPool = new ArrayList<>();
		threadPool.clear();
	}
	public double sum(){
		startCounter();
		double sum = Arrays.stream(res).parallel().mapToDouble(x->x[0]).sum();
		stopCounter();
		setTimeTaken();
		return sum;
	}
	
	public void multiply(Matrix other){
		
		initializeResultantMatrix(other);
		
		if(requiresBroadcasting(other)){
			broadcastedMultiplication(other);
			return;
		}
		
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
	}
	
	private void logBroadcastException(){
		try {
			throw new Exception("Operands cannot be broadcasted");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void broadcastedMultiplication(Matrix other){
		if(!isBroadcastable(other)){
			logBroadcastException();
			return;
		}
		

	}
	private void broadcastedAddition(Matrix mat1,Matrix mat2){
		if(!isBroadcastable(mat1)){
			logBroadcastException();
			return;
		}
		if(mat1.rowSize != 1 && mat1.colSize != 1){
			broadcastedAddition(mat2,mat1);
			return;
		}
		
		int row = Math.max(mat1.rowSize,mat2.rowSize);
		int col  = Math.max(mat1.colSize, mat2.colSize);
		res  = new double[row][col];
		if(mat1.rowSize == 1){
			for(int i = 0;i<mat2.rowSize;i++){
				for(int j = 0;j<mat2.colSize;j++){
					res[i][j] = mat2.mat[i][j].doubleValue() + mat1.mat[0][j].doubleValue();
				}
			}
		}else{
			for(int i = 0;i<mat2.colSize;i++){
				for(int j = 0;j<mat2.rowSize;j++){
					res[j][i] = mat2.mat[j][i].doubleValue() + mat1.mat[j][0].doubleValue();
				}
			}
		}
		
	}
	
	public void add(Matrix other){
		
		
		if(requiresBroadcasting(other)){
			broadcastedAddition(other,this);
			return;
		}
		res = new double[rowSize][colSize];
		for(int i =0;i<rowSize;i++){
			for(int j =0;j<colSize;j++){
				res[i][j] = mat[i][j].doubleValue() + other.mat[i][j].doubleValue();
			}
		}
	}
	
	private void startCounter(){
		startTime = System.nanoTime();
	}
	
	private void stopCounter(){
		endTime = System.nanoTime();
	}
	
	private void setTimeTaken(){
		timeTaken = endTime-startTime;
	}
	
	private void initializeResultantMatrix(Matrix other){
		res = new double[rowSize][other.colSize];
	}
	
	private void parallelTaskSetup(Matrix other){
		initializeResultantMatrix(other);
		
	}
	
	public void parallelMultiply(Matrix other){
		parallelTaskSetup(other);

		startCounter();
		for(int i = 0;i<rowSize;i++){
			MultiplierTask rangeArithmeticObj = new MultiplierTask(this,other,i);
			Thread thread = new Thread(rangeArithmeticObj);
			threadPool.add(thread);
			thread.start();
			if(threadPool.size()  == threadCapacity){
				waitForThreads();
			}
		}
		stopCounter();
		setTimeTaken();
		createOrResetThreadPool();
		
	}
	
	
	public void parallelAdd(Matrix other){
		parallelTaskSetup(other);
		startCounter();
		for(int row = 0;row<rowSize;row++){
			additionTask additionParams = new additionTask(this,other, row);
			
			if(threadPool.size()<threadCapacity){
				Thread additionThread = new Thread(additionParams);
				additionThread.start();
				threadPool.add(additionThread);
			}else{
				waitForThreads();
			}
			
		}
		stopCounter();
		setTimeTaken();
		
	}
	
	//todo
	public void parallelSubtract(Matrix other){
		
	}
	
	//todo
	public void parallelDivide(Matrix other){
		
	}
	
	private void waitForThreads(){
		for(Thread thread: threadPool){
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
	
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
