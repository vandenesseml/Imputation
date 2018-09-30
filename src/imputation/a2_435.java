
package imputation;

import com.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author MaxlV
 */
public class a2_435 {
    protected static Map<String, String[][] > fileMap;
    public static void main(String[] args) throws FileNotFoundException {
        init();
        buildDatasets();
    }
    protected static void init() {
        fileMap = new HashMap();
        fileMap.put("dataset_complete.csv", new String[8796][14]);
        fileMap.put("dataset_missing01.csv", new String[8796][14]);
        fileMap.put("dataset_missing10.csv", new String[8796][14]);
    }
    protected static void buildDatasets(){
        try{
        for(String key: fileMap.keySet()) {
            System.out.println(key);
            FileReader filereader = new FileReader(key);
            CSVReader csvReader = new CSVReader(filereader);
            int datasetIndex = 0;
            while ((fileMap.get(key)[datasetIndex] = csvReader.readNext()) != null) { 
                datasetIndex++;
                if(fileMap.get(key)[datasetIndex-1][0].equals("F1")) {
                   datasetIndex--;
               }
            } 
        }
        }catch(Exception e) {
            System.out.println(e);
        }
       
    }
}
