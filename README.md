## JMatrix

JMatrix is a lightweight java package that is designed for working with 2 dimensional matrices efficiently. It supports parallel computations for Numerical calculations like multiplications using the concept of multithreading to improve the running time. The implementation also takes into account spatial locality to avoid unnecessary cache miss.The class currently supports all the wrapper classes that extend the Number class in java and it makes use of bounded generics to enforce type check during compile time. 

## Benchmarks for Numerical operations

Matrix multiplication (Normal) 3.409885218 seconds
Matrix multiplication (Parallel) 0.774605248 seconds

Each Matrix object consists of a timeTaken propery that tracks the running time of the arithmetic operations performed on the object.
The recorded time is mesured in nanoseconds.