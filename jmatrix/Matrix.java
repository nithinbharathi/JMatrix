package jmatrix;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author : Nithin Bharathi 17-Jul-2023
 */

public class Matrix{
	
	long mat[][];
	int colSize,rowSize;
	long res[][];
	private int threadCapacity = Math.max(1, Runtime.getRuntime().availableProcessors()-1);
	StringBuilder matrixRepresentation = null;
	
	ArrayList<Thread>threadPool;
	
	public Matrix(long mat[],int rowSize, int colSize){		
		this.colSize = colSize;
		this.rowSize = rowSize;
		this.mat = transform(mat);
	}
	
	private long[][] transform(long mat[]){
		validateDimensions(mat.length);
		long matrix[][] = new long[rowSize][colSize];
		int row = 0,col=0;
		for(int i = 0;i<mat.length;i++){
			matrix[row][col++] = mat[i];
			col%=colSize;
			row = col==0?row+1:row;
		}
		return matrix;
	}
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
	private Matrix boradCast(Matrix mat){
		return mat;
	}
	
	public long sum(){
		long sum = 0;
		for(int i = 0;i<rowSize;i++){
			sum+=Arrays.stream(mat[i]).sum();
		}
		return sum;
	}
	
	public void multiply(Matrix other){
		for(int i = 0;i<rowSize;i++){
			for(int j = 0;j<colSize;j++){
				for(int z = 0;z<colSize;z++){
					res[i][j]+=res[i][z]*res[z][i];
				}
			}
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
					mat1.res[row][col] = mat1.mat[row][itr]*mat2.mat[itr][col];
				}
			}
		}
		
	}
}
