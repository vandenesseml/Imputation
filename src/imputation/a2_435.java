
package imputation;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        hotDeckImputation();
        conditionalHotDeckImputation();
        calculateAndDisplayMAE();
        formatAndSaveImputedDatasets();
    }
    protected static void init() {
        fileMap = new HashMap();
        fileMap.put("dataset_complete.csv", new String[8796][14]);
        fileMap.put("dataset_missing01.csv", new String[8796][14]);
        fileMap.put("dataset_missing10.csv", new String[8796][14]);
        datasetMap = new HashMap();
        imputedDatasetMap = new HashMap();
        datasetKeys = new HashSet();
    }
    protected static void buildDatasets(){
        try{
        for(String key: fileMap.keySet()) {
            String datasetKey = key.substring(0,key.length()-4);
            datasetMap.put(datasetKey, new Float[8796][14]);
            FileReader fileReader = new FileReader(key);
            int datasetIndex = 0;
            Scanner datasetScanner = new Scanner(fileReader);
            while(datasetScanner.hasNextLine()){
                fileMap.get(key)[datasetIndex] = datasetScanner.nextLine().split(",");
                datasetMap.get(datasetKey)[datasetIndex] = 
                        formatDatasetToFloat(fileMap.get(key)[datasetIndex]);
                datasetIndex++;
                if(fileMap.get(key)[datasetIndex-1][0].equals("F1")) {
                   datasetIndex--;
               }
            } 
        }
        datasetMap.keySet().forEach((key) -> {
            datasetKeys.add(key);
            });
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
        imputedDatasetMap.put("V70480478_missing01_imputed_mean",
                copyArray(datasetMap.get("dataset_missing01")));
        imputedDatasetMap.put("V70480478_missing10_imputed_mean", 
                copyArray(datasetMap.get("dataset_missing10")));
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
                        imputedDatasetMap.get("V70480478_missing01_imputed" +
                            "_mean_conditional")[i][j] = classYMeanMap.get(j);
                        } else {
                            imputedDatasetMap.get("V70480478_missing10" +
                                    "_imputed" + "_mean_conditional")[i][j] = 
                                    classYMeanMap.get(j);
                        }
                    } else if (datasetMap.get(datasetKey)[i][13] == (float)0){
                        if(datasetKey.equals("dataset_missing01")){
                            imputedDatasetMap.get("V70480478_missing01" +
                                    "_imputed_mean_conditional")[i][j] = 
                                    classNMeanMap.get(j);
                        } else {
                            imputedDatasetMap.get("V70480478_missing10" +
                                    "_imputed_mean_conditional")[i][j] =
                                    classNMeanMap.get(j);
                        }
                    }
                }   
            }
        }
        });
    }
    protected static void hotDeckImputation() {
        imputedDatasetMap.put("V70480478_missing01_imputed_hd", 
                copyArray(datasetMap.get("dataset_missing01")));
        imputedDatasetMap.put("V70480478_missing10_imputed_hd", 
                copyArray(datasetMap.get("dataset_missing10")));
        Map<Float, Float[]> distanceMap = new HashMap<>();
        datasetKeys.forEach((datasetKey) -> {
            for(int i = 0;i < 8795;i++){
                Float[] array1 = new Float[13];
                Float[] array2 = new Float[13];
                System.arraycopy(datasetMap.get(datasetKey)[i], 0, array1, 0, 13);
                Float distance = (float)100;
                if(Arrays.asList(array1).contains(null)){
                    distanceMap.clear();
                    for(int j = 0;j < 8795;j++){
                        if(i != j){
                        System.arraycopy(datasetMap.get(datasetKey)[j], 0,
                                array2, 0, 13);
                        Float currentDistance = (float)0;
                        for(int k = 0;k < 13; k++){
                            if(array1[k] == null || array2[k] == null){
                              currentDistance = currentDistance + 1;
                            } else {
                              currentDistance = currentDistance + (float)
                                      (Math.pow((array1[k] - array2[k]),2));
                            }
                          }
                        currentDistance = (float)Math.sqrt(currentDistance);
                        distanceMap.put(currentDistance, datasetMap.get(datasetKey)[j]);
                        distance = currentDistance;
                        } 
                    }
                    boolean duplicateNulls = true;
                    Float globalKey = (float)0;
                    Map<Float, Float[]>sortedDistanceMap = 
                            new TreeMap<>(distanceMap);
                    for(Float key: sortedDistanceMap.keySet()){
                        duplicateNulls = true;
                        for(int l = 0; l < 13; l++){
                            if(datasetMap.get(datasetKey)[i][l] == null){
                                if(sortedDistanceMap.get(key)[l] != null){
                                    duplicateNulls = false;
                                    globalKey = key;
                                } else{
                                    duplicateNulls = true;
                                    break;
                                }
                            }
                        }
                        if(!duplicateNulls) {
                            break; 
                        }
                    }
                    if(!duplicateNulls){
                        for(int m = 0;m < 13;m++){
                            if(datasetKey.equals("dataset_missing01")){
                                if(imputedDatasetMap.get("V70480478_missing01" +
                                        "_imputed_hd")[i][m] == null){
                                    imputedDatasetMap.get("V70480478_missing01" +
                                            "_imputed_hd")[i][m] = 
                                            sortedDistanceMap.get(globalKey)[m];
                                }
                            } else if(datasetKey.equals("dataset_missing10")) {
                                if(imputedDatasetMap.get("V70480478_missing10" +
                                        "_imputed_hd")[i][m] == null){
                                    imputedDatasetMap.get("V70480478" +
                                            "_missing10_imputed_hd")[i][m] = 
                                            sortedDistanceMap.get(globalKey)[m];
                                } 
                            }
                        }
                    } else {
                        int x = 9;
                    }
                }
            }
        });
    }
    protected static void conditionalHotDeckImputation() {
        imputedDatasetMap.put("V70480478_missing01_imputed_hd_conditional", 
                copyArray(datasetMap.get("dataset_missing01")));
        imputedDatasetMap.put("V70480478_missing10_imputed_hd_conditional", 
                copyArray(datasetMap.get("dataset_missing10")));
        
                Map<Float, Float[]> distanceMap = new HashMap<>();
        datasetKeys.forEach((datasetKey) -> {
            for(int i = 0;i < 8795;i++){
                Float[] array1 = new Float[14];
                Float[] array2 = new Float[14];
                System.arraycopy(datasetMap.get(datasetKey)[i], 0, array1, 0, 14);
                Float classFeature = array1[13];
                Float distance = (float)100;
                if(Arrays.asList(array1).contains(null)){
                    distanceMap.clear();
                    for(int j = 0;j < 8795;j++){
                        if((i != j) && ((datasetMap.get(datasetKey)[j][13])
                                .intValue() == classFeature.intValue())){
                            System.arraycopy(datasetMap.get(datasetKey)[j], 0,
                                    array2, 0, 14);
                            Float currentDistance = (float)0;
                            for(int k = 0;k < 13; k++){
                                if(array1[k] == null || array2[k] == null){
                                  currentDistance = currentDistance + 1;
                                } else {
                                  currentDistance = currentDistance + (float)
                                          (Math.pow((array1[k] - array2[k]),2));
                                }
                            }
                            currentDistance = (float)Math.sqrt(currentDistance);
                            distanceMap.put(currentDistance, datasetMap
                                    .get(datasetKey)[j]);
                            distance = currentDistance;
                        } 
                    }
                    boolean duplicateNulls = true;
                    Float globalKey = (float)0;
                    Map<Float, Float[]>sortedDistanceMap = 
                            new TreeMap<>(distanceMap);
                    for(Float key: sortedDistanceMap.keySet()){
                        for(int l = 0; l < 13; l++){
                            if(datasetMap.get(datasetKey)[i][l] == null){
                                if(sortedDistanceMap.get(key)[l] != null){
                                    duplicateNulls = false;
                                    globalKey = key;
                                } else{
                                    duplicateNulls = true;
                                    break;
                                }
                            }
                        }
                        if(!duplicateNulls) {
                            break; 
                        }
                    }
                    if(!duplicateNulls){
                        for(int m = 0;m < 13;m++){
                            if(datasetKey.equals("dataset_missing01")){
                                if(imputedDatasetMap.get("V70480478_missing01" +
                                        "_imputed_hd_conditional")[i][m] == null){
                                    imputedDatasetMap.get("V70480478_missing01" +
                                            "_imputed_hd_conditional")[i][m] = 
                                            sortedDistanceMap.get(globalKey)[m];
                                }
                            } else if(datasetKey.equals("dataset_missing10")) {
                                if(imputedDatasetMap.get("V70480478_missing10" +
                                        "_imputed_hd_conditional")[i][m] == null){
                                    imputedDatasetMap.get("V70480478" +
                                            "_missing10_imputed_hd_conditional")[i][m] = 
                                            sortedDistanceMap.get(globalKey)[m];
                                } 
                            }
                        }
                    }
                }
            }
        });
    }
    protected static void calculateAndDisplayMAE(){
        imputedDatasetMap.keySet().forEach((imputedDatasetMapKey -> {
            String datasetMapKey = "dataset_" + imputedDatasetMapKey
                    .substring(10, 19);
            Float absSummedDifferences = (float)0;
            int numberOfNulls = 0;
            for(int i = 0; i < 8795;i++){
                for(int j = 0;j < 14;j++){
                    if(datasetMap.get(datasetMapKey)[i][j] == null){
                        try{
                            
                        absSummedDifferences = absSummedDifferences + Math
                                .abs((imputedDatasetMap
                                    .get(imputedDatasetMapKey)[i][j]) -
                                    (datasetMap.get("dataset_complete")[i][j]));
                        numberOfNulls++;
                        } catch (Exception e){
                            int x = 0;
                        }
                    }
                }
            }
            float MAE = (((float)1/(float)numberOfNulls)*absSummedDifferences);
            String imputedDatasetName = imputedDatasetMapKey;
            String algorithm = imputedDatasetName.substring(27,
                    imputedDatasetName.length());
            String datasetNumber = imputedDatasetMapKey.substring(17,19);
            System.out.println("MAE_" + datasetNumber + algorithm + " = " + 
                    String.format("%.4f",MAE));
        }));
    }
    protected static void formatAndSaveImputedDatasets(){
        imputedDatasetMap.keySet().forEach((imputedDatasetMapKey) -> {
            String fileName = imputedDatasetMapKey + ".csv";
            String[][] printArray = new String[8796][14];
            PrintWriter printWriter = null;
            String fileOutput = "";
            try {
                printWriter = new PrintWriter(fileName);
                for(int i = 0;i < 14;i++){
                    if(i != 13){
                        fileOutput = fileOutput + "F" + (i+1) + ",";
                    } else {
                        fileOutput = fileOutput + "Class\n";
                    }
                }
                for(int i = 0;i < 8795;i++){
                    for(int j = 0;j < 14;j++){
                        if(j == 13) {
                            if((imputedDatasetMap.get(imputedDatasetMapKey)[i][j])
                                    .intValue() == 1){
                                fileOutput = fileOutput + "Y\n";
                        } else if ((imputedDatasetMap.get(imputedDatasetMapKey)[i][j])
                                .intValue() == 0) {
                                fileOutput = fileOutput + "N\n";
                            }
                        } else {
                        fileOutput = fileOutput + String.format("%.5f",
                                imputedDatasetMap.get(imputedDatasetMapKey)[i][j]) + ",";
                        }
                    }
                }
                printWriter.write(fileOutput);
                printWriter.close();
            } catch (FileNotFoundException ex) {
                    Logger.getLogger(a2_435.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
        });
    }
}

