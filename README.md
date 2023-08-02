## JMatrix

JMatrix is a lightweight java package that is designed for working with 2 dimensional matrices efficiently. It supports parallel computations for Numerical calculations like multiplications using the concept of multithreading to improve the running time. The implementation also takes into account spatial locality to avoid unnecessary cache miss.The class currently supports all the wrapper classes that extend the Number class in java and it makes use of bounded generics to enforce type check during compile time. Additionally, the current implementation of the arithmetic operations (non-parallel) supports broadcasting similar to numpy by allowing different matrix shapes to be involved in the computation if they satisfy the broadcasting rules. 

## Current Benchmarks

The computation metrics are recorded for a 2 dimensional matrix of size 1000X1000. Therefore a total of 2*10e6 FLOPS including the scalar addition and multiplication are involved.

|			Operation			   |	Execution Time (in seconds)	|	
|----------------------------------|--------------------------------|
| Matrix multiplication (Normal)   |			3.409885218 	    |
| Matrix multiplication (Parallel) |  			0.774605248 	    | 

Each Matrix object consists of a timeTaken propery that tracks the running time of the arithmetic operations performed on the object.
The recorded time is mesured in nanoseconds.

## Example

Below is a contrived example of some of the operations the package supports.

```
package import
import jmatrix.Matrix;
	
public class JTest{
	
public static void main(String[] args) {
	int n  = 100;
	Integer a[] = new Integer[n];
	Integer b[] = new Integer[n];	
	for(int i =0;i<n;i++){
		a[i] = 1;
		b[i] = 1;
	 }
			
//creating the matrix objects
	Matrix<Integer> mat1 = new Matrix<>(a,10,10);
	Matrix<Integer> mat2 = new Matrix<>(b,10,10);
	Matrix<Integer>mat = new Matrix<>(c);
			
//testing the package operations
	mat1.view();
	mat2.view();
	mat1.multiply(mat2);
	System.out.printf("%.8f\n",mat1.timeTaken/1e9);
	mat1.parallelMultiply(mat2);
	mat1.result();
	System.out.printf("%.8f\n",mat1.timeTaken/1e9);
  }
		
	
}
```
