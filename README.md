## JMatrix

JMatrix is a lightweight java package that is designed for working with 2 dimensional matrices efficiently. It supports parallel computations for Numerical calculations like multiplications using the concept of multithreading to improve the running time. The implementation also takes into account spatial locality to avoid unnecessary cache miss.The class currently supports all the wrapper classes that extend the Number class in java and it makes use of bounded generics to enforce type check during compile time. 

## Current Benchmarks

The computation metrics are recorded for a 2 dimensional matrix of size 1000X1000. Therefore a total of 2*10e6 FLOPS including the scalar addition and multiplication are involved.

|			Operation			   |	Execution Time (in seconds)	|	
|----------------------------------|--------------------------------|
| Matrix multiplication (Normal)   |			3.409885218 	    |
| Matrix multiplication (Parallel) |  			0.774605248 	    | 

Each Matrix object consists of a timeTaken propery that tracks the running time of the arithmetic operations performed on the object.
The recorded time is mesured in nanoseconds.