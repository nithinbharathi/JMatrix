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
	T res[][];
	
	
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
	
	ArrayList<Thread>threadPool;
	
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
	public StringBuilder view(){
		if(matrixRepresentation == null){
			matrixRepresentation = new StringBuilder();
			for(int i =0;i<rowSize;i++){
				for(int j =0;j<colSize;j++){
					matrixRepresentation.append(mat[i][j]);
					matrixRepresentation.append(" ");
				}
				matrixRepresentation.append("\n");
			}
		}
		return matrixRepresentation;
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
	
	private boolean requiresBroadcasting(Matrix mat){
		return true;
	}
	private Matrix broadCast(Matrix mat){
		return mat;
	}
	
	public double sum(){
		long sum = 0;
		for(int i = 0;i<rowSize;i++){
			for(int j = 0;j<colSize;j++){
				sum+= res[i][j].doubleValue();
			}
		}
		return sum;
	}
	
	public void multiply(Matrix other){
		initializeResultantMatrix(other);
		for(int i = 0;i<rowSize;i++){
			for(int j = 0;j<colSize;j++){
				for(int z = 0;z<colSize;z++){
				//	res[i][j] = res[i][j].doubleValue()*res[j][i].doubleValue();
				}
			}
		}
	}
	private void initializeResultantMatrix(Matrix other){
		if(res == null){
			res = getArray(other.getClass().getComponentType(), new int[]{rowSize,other.colSize});
		}
	}
	
	public void parallelMultiply(Matrix other){
		
		if(threadPool == null)threadPool = new ArrayList<>();
		
		for(int i = 0;i<rowSize;i++){
			MultiplierTask rangeArithmeticObj = new MultiplierTask(this,other,i);
			Thread thread = new Thread(rangeArithmeticObj);
			threadPool.add(thread);
			thread.start();
			if(threadPool.size()  == threadCapacity){
				waitForThreads(threadPool);
			}
		}
	}
	
	//todo
	public void parallelAdd(Matrix other){
		
	}
	
	//todo
	public void parallelSubtract(Matrix other){
		
	}
	
	//todo
	public void parallelDivide(Matrix other){
		
	}
	
	private void waitForThreads(ArrayList<Thread> threads){
		for(Thread thread: threads){
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class MultiplierTask implements Runnable{
		
		Matrix mat1 , mat2;
		int row;
		public MultiplierTask(Matrix mat1, Matrix mat2, int row){
			this.mat1 = mat1;
			this.mat2 = mat2;
			this.row = row;
		}

		@Override
		public void run() {
			for(int col = 0;col<mat1.colSize;col++){
				for(int itr = 0;itr<mat1.colSize;itr++){
					mat1.res[row][col]= mat1.res[row][col].doubleValue() +
										(mat1.mat[row][itr].doubleValue()*mat2.mat[itr][col].doubleValue());
				}
			}
		}
		
	}
}
