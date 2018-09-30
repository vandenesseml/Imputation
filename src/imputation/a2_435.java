
package imputation;

import com.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author MaxlV
 */
public class a2_435 {
    protected static Map<String, String[][] > fileMap;
    protected static Map<String, Float[][]> datasetMap;
    protected static Map<String, Float[][]> imputedDatasetMap;
    protected static Set<String> datasetKeys;
    
    public static void main(String[] args) throws FileNotFoundException {
        init();
        buildDatasets();
        meanImputation();
        conditionalMeanImputation();
    }
    protected static void init() {
        fileMap = new HashMap();
        fileMap.put("dataset_complete.csv", new String[8796][14]);
        fileMap.put("dataset_missing01.csv", new String[8796][14]);
        fileMap.put("dataset_missing10.csv", new String[8796][14]);
        datasetMap = new HashMap();
        imputedDatasetMap = new HashMap();
        
        
        imputedDatasetMap.put("V70480478_missing01_imputed_hd", new Float[8796][14]);
        imputedDatasetMap.put("V70480478_missing01_imputed_hd_conditional", new Float[8796][14]);
        imputedDatasetMap.put("V70480478_missing10_imputed_hd", new Float[8796][14]);
        imputedDatasetMap.put("V70480478_missing10_imputed_hd_conditional", new Float[8796][14]);
    }
    protected static void buildDatasets(){
        try{
        for(String key: fileMap.keySet()) {
            String datasetKey = key.substring(0,key.length()-4);
            datasetMap.put(datasetKey, new Float[8796][14]);
            FileReader filereader = new FileReader(key);
            CSVReader csvReader = new CSVReader(filereader);
            int datasetIndex = 0;
            int index;
            while ((fileMap.get(key)[datasetIndex] = csvReader.readNext()) != null) { 
                
                datasetMap.get(datasetKey)[datasetIndex] = formatDatasetToFloat(fileMap.get(key)[datasetIndex]);
                datasetIndex++;
                if(fileMap.get(key)[datasetIndex-1][0].equals("F1")) {
                   datasetIndex--;
               }
            } 
        }
        datasetKeys = datasetMap.keySet();
        datasetKeys.remove("dataset_complete");
        }catch(IOException e) {
        }
    }
    protected static Float[] formatDatasetToFloat(String[] array){
        Float[] formattedDatasetArray = new Float[array.length];
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
            formattedDatasetArray[i] = Float.parseFloat(array[i]);
            } catch(NumberFormatException e) {
                formattedDatasetArray[i] = null;
            }
        }
        return formattedDatasetArray;
    }
    protected static Float[][] copyArray(Float[][] originalArray){
        Float[][] newArray = new Float[8795][14];
        for(int i = 0; i < 8795;i++){
            System.arraycopy(originalArray[i], 0, newArray[i], 0, 14);
        }
        return newArray;
    }
    protected static void meanImputation(){
        float summedValues = 0;
        int count = 0;
        Map<Integer,Integer> imputationIndexMap = new HashMap();
        imputedDatasetMap.put("V70480478_missing01_imputed_mean",copyArray(datasetMap.get("dataset_missing01")));
        imputedDatasetMap.put("V70480478_missing10_imputed_mean", copyArray(datasetMap.get("dataset_missing10")));
        for(String datasetMapKey: datasetKeys){
            for(int i = 0;i < 13; i++){
                for(int j = 0;j < 8795;j++){
                    if(datasetMap.get(datasetMapKey)[j][i] != null){
                        summedValues = summedValues + datasetMap
                                .get(datasetMapKey)[j][i];
                        count++;
                    } else {
                        imputationIndexMap.put(j,i);
                    }
                }
//              calculate mean of all values and replace null values with that
//              mean.
                Float imputedValue = summedValues/count;
                imputationIndexMap.keySet().forEach((imputationIndexMapKey) -> {
                    if(datasetMapKey.equals("dataset_missing01")){
                        imputedDatasetMap.get("V70480478_missing01_imputed_mean")
                                [imputationIndexMapKey][imputationIndexMap
                                        .get(imputationIndexMapKey)] 
                                = imputedValue;
                    } else if (datasetMapKey.equals("dataset_missing10")) {
                        imputedDatasetMap.get("V70480478_missing10_imputed_mean")
                                [imputationIndexMapKey][imputationIndexMap
                                        .get(imputationIndexMapKey)] 
                                = imputedValue;
                    }
                });
                imputationIndexMap.clear();
                count = 0;
                summedValues = 0;
            }
        }   
    }
    protected static void conditionalMeanImputation(){
        imputedDatasetMap.put("V70480478_missing01_imputed_mean_conditional", 
                copyArray(datasetMap.get("dataset_missing01")));
        imputedDatasetMap.put("V70480478_missing10_imputed_mean_conditional", 
                copyArray(datasetMap.get("dataset_missing10")));
        Map<Integer, Float> classYMeanMap = new HashMap();
        Map<Integer, Float> classNMeanMap = new HashMap();
        Map<Integer, Integer> classYCountMap = new HashMap();
        Map<Integer, Integer> classNCountMap = new HashMap();
        
        datasetKeys.forEach((datasetKey) -> {
            for(int i = 0;i < 13;i++){
            classYMeanMap.put(i,(float)0);
            classNMeanMap.put(i,(float)0);
            classYCountMap.put(i,0);
            classNCountMap.put(i,0);
        }
            int coount = 0;
        for(int i = 0;i<8795;i++){
            for(int j = 0;j < 13;j++){
                if(datasetMap.get(datasetKey)[i][j] != null){
                    if(datasetMap.get(datasetKey)[i][13] == (float)1){
                        Float currentFeatureSum = classYMeanMap.get(j);
                        classYMeanMap.put(j, (currentFeatureSum +
                                datasetMap.get(datasetKey)[i][j]));
                        int currentFeatureCount = (int)classYCountMap.get(j);
                        classYCountMap.put(j, ++currentFeatureCount);
                    } else if (datasetMap.get(datasetKey)[i][13] == (float)0){
                        Float currentFeatureSum = classNMeanMap.get(j);
                        classNMeanMap.put(j, (currentFeatureSum +
                                datasetMap.get(datasetKey)[i][j]));
                        int currentFeatureCount = classNCountMap.get(j);
                        classNCountMap.put(j, ++currentFeatureCount);
                    }
                }   
            }
        }
        for(int i = 0;i < 13;i++){
            Float classYMean = classYMeanMap.get(i)/classYCountMap.get(i);
            Float classNMean = classNMeanMap.get(i)/classNCountMap.get(i);
            classYMeanMap.put(i,classYMean);
            classNMeanMap.put(i,classNMean);
        }
        for(int i = 0;i<8795;i++){
            for(int j = 0;j < 13;j++){
                if(datasetMap.get(datasetKey)[i][j] == null){
                    if(datasetMap.get(datasetKey)[i][13] == (float)1){
                        if(datasetKey.equals("dataset_missing01")){
                        imputedDatasetMap.get("V70480478_missing01_imputed_mean_conditional")[i][j] = classYMeanMap.get(j);
                        } else {
                            imputedDatasetMap.get("V70480478_missing10_imputed_mean_conditional")[i][j] = classYMeanMap.get(j);
                        }
                    } else if (datasetMap.get(datasetKey)[i][13] == (float)0){
                        if(datasetKey.equals("dataset_missing01")){
                            imputedDatasetMap.get("V70480478_missing01_imputed_mean_conditional")[i][j] = classNMeanMap.get(j);
                        } else {
                            imputedDatasetMap.get("V70480478_missing10_imputed_mean_conditional")[i][j] = classNMeanMap.get(j);
                        }
                    }
                }   
            }
        }
        });
    }
}

