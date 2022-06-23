//Load balancing through source addressing

import java.util.*;
import java.io.*;

public class Hash      {      
    public static String getServer()      
    {      
        // Reconstruct a Map to avoid concurrency problems caused by the server's upload and download.
        Map<String, Integer> serverMap =  new HashMap<String, Integer>();      
        serverMap.putAll(IpMap.serverWeightMap);      

        // Get the Ip address List
        Set<String> keySet = serverMap.keySet();      
        ArrayList<String> keyList = new ArrayList<String>();      
        keyList.addAll(keySet);      

        String remoteIp = "129.0.0.1";      
        int hashCode = remoteIp.hashCode();      
        int serverListSize = keyList.size();      
        int serverPos = hashCode % serverListSize;      

        return keyList.get(serverPos);      
    }      
}

public class WeightRoundRobin   {   
    private static Integer pos;   

    public static String getServer()   
    {   
        Map<String, Integer> serverMap = new HashMap<String, Integer>();   
        serverMap.putAll(IpMap.serverWeightMap);   

        // Get the Ip address List 
        Set<String> keySet = serverMap.keySet();   
        Iterator<String> iterator = keySet.iterator();   

        List<String> serverList = new ArrayList<String>();   
        while (iterator.hasNext())   
        {   
            String server = iterator.next();   
            int weight = serverMap.get(server);   
            for (int i = 0; i < weight; i++)   
                serverList.add(server);   
        }   

        String server = null;   
        synchronized (pos)   
        {   
            if (pos > keySet.size())   
                pos = 0;   
            server = serverList.get(pos);   
            pos ++;   
        }   

        return server;   
    }   
}
