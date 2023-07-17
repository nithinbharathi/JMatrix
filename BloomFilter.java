import java.util.*;    
import java.security.*;
import java.math.*;
import java.nio.*;

class BloomFilter
{    
    private byte[] set;
    private int keySize, setSize, size;
    private MessageDigest md;
 
    public BloomFilter(int capacity, int k)
    {
        setSize = capacity;
        set = new byte[setSize];
        keySize = k;
        size = 0;
        try 
        {
            md = MessageDigest.getInstance("MD5");
        } 
        catch (NoSuchAlgorithmException e) 
        {
            throw new IllegalArgumentException("Error : MD5 Hash not found");
        }
    }
    public void makeEmpty()
    {
        set = new byte[setSize];
        size = 0;
        try 
        {
             md = MessageDigest.getInstance("MD5");
        } 
        catch (NoSuchAlgorithmException e) 
        {
            throw new IllegalArgumentException("Error : MD5 Hash not found");
        }
    }
    public boolean isEmpty()
    {
        return size == 0;
    }
    public int getSize()
    {
        return size;
    }
    private int getHash(int i)
    {
        md.reset();
        byte[] bytes = ByteBuffer.allocate(4).putInt(i).array();
        md.update(bytes, 0, bytes.length);
        return Math.abs(new BigInteger(1, md.digest()).intValue()) % (set.length - 1);
    }

 public void add(Object obj)
    {
        int[] tmpset = getSetArray(obj);
        for (int i : tmpset)
            set[i] = 1;
        size++;
    }
    public boolean contains(Object obj) 
    {
        int[] tmpset = getSetArray(obj);
        for (int i : tmpset)
            if (set[i] != 1)
                return false;
        return true;
    }
    private int[] getSetArray(Object obj)
    {
        int[] tmpset = new int[keySize];
        tmpset[0] = getHash(obj.hashCode());
        for (int i = 1; i < keySize; i++)
            tmpset[i] = (getHash(tmpset[i - 1]));
        return tmpset;
    }    
}
 
class BloomFilterTest
{
    public static void main(String[] args)
    {
        Scanner scan = new Scanner(System.in);
        System.out.println("Bloom Filter Test\n");   
 
        System.out.println("Enter set capacity and key size");
        BloomFilter bf = new BloomFilter(scan.nextInt() , scan.nextInt());
 
        char ch;
        /*  Perform bloom filter operations  */
        do    
        {
            System.out.println("\nBloomFilter Operations\n");
            System.out.println("1. insert ");
            System.out.println("2. contains");
            System.out.println("3. check empty");
            System.out.println("4. clear");
            System.out.println("5. size");
 
            int choice = scan.nextInt();            
            switch (choice)
            {
            case 1 : 
                System.out.println("Enter integer element to insert");
                bf.add( new Integer(scan.nextInt()) );                     
                break;                          
            case 2 : 
                System.out.println("Enter integer element to search");
                System.out.println("Search result : "+ bf.contains( new Integer(scan.nextInt()) ));
                break;                                          
            case 3 : 
                System.out.println("Empty status = "+ bf.isEmpty());
                break;
            case 4 : 
                System.out.println("\nBloom set Cleared");
                bf.makeEmpty();
                break;    
            case 5 : 
                System.out.println("\nSize = "+ bf.getSize() );
                break;            
            default : 
                System.out.println("Wrong Entry \n ");
                break;   
            }    
 
            System.out.println("\nDo you want to continue (Type y or n) \n");
            ch = scan.next().charAt(0);                        
        } while (ch == 'Y'|| ch == 'y');    
    }
}
