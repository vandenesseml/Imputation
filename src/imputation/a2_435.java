
package imputation;

import com.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author MaxlV
 */
public class a2_435 {
    protected static Map<String, String[][] > fileMap;
    protected static Map<String, Double[][]> datasetMap;
    protected static Map<String, Double[][]> imputedDatasetMap;
    public static void main(String[] args) throws FileNotFoundException {
        init();
        buildDatasets();
    }
    protected static void init() {
        fileMap = new HashMap();
        fileMap.put("dataset_complete.csv", new String[8796][14]);
        fileMap.put("dataset_missing01.csv", new String[8796][14]);
        fileMap.put("dataset_missing10.csv", new String[8796][14]);
        datasetMap = new HashMap();
        imputedDatasetMap = new HashMap();
        imputedDatasetMap.put("V70480478_missing01_imputed_mean", new Double[8796][14]);
        imputedDatasetMap.put("V70480478_missing01_imputed_mean_conditional", new Double[8796][14]);
        imputedDatasetMap.put("V70480478_missing01_imputed_hd", new Double[8796][14]);
        imputedDatasetMap.put("V70480478_missing01_imputed_hd_conditional", new Double[8796][14]);
        imputedDatasetMap.put("V70480478_missing10_imputed_mean", new Double[8796][14]);
        imputedDatasetMap.put("V70480478_missing10_imputed_mean_conditional", new Double[8796][14]);
        imputedDatasetMap.put("V70480478_missing10_imputed_hd", new Double[8796][14]);
        imputedDatasetMap.put("V70480478_missing10_imputed_hd_conditional", new Double[8796][14]);
    }
    protected static void buildDatasets(){
        try{
        for(String key: fileMap.keySet()) {
            String datasetKey = key.substring(0,key.length()-4);
            datasetMap.put(datasetKey, new Double[8796][14]);
            FileReader filereader = new FileReader(key);
            CSVReader csvReader = new CSVReader(filereader);
            int datasetIndex = 0;
            int index;
            while ((fileMap.get(key)[datasetIndex] = csvReader.readNext()) != null) { 
                
                datasetMap.get(datasetKey)[datasetIndex] = formatDatasetToDouble(fileMap.get(key)[datasetIndex]);
                datasetIndex++;
                if(fileMap.get(key)[datasetIndex-1][0].equals("F1")) {
                   datasetIndex--;
               }
            } 
        }
        }catch(IOException e) {
        }
    }
    protected static Double[] formatDatasetToDouble(String[] array){
        Double[] formattedDatasetArray = new Double[array.length];
        for(int i = 0;i < array.length;i++) {
            if(Arrays.asList(array).contains("Y")){
                array[Arrays.asList(array).indexOf("Y")] = "1";
                } 
            if(Arrays.asList(array).contains("N")){
                array[Arrays.asList(array).indexOf("N")] = "0";
            }
            if(Arrays.asList(array).contains("?")){
                array[Arrays.asList(array).indexOf("?")] = "";
                }
            try{
            formattedDatasetArray[i] = Double.parseDouble(array[i]);
            } catch(Exception e) {
                formattedDatasetArray[i] = null;
            }
        }
        return formattedDatasetArray;
    }
}

