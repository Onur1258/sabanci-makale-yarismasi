import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class wholeProject {
    
    private HashMap<String,ArrayList<Data>> everyInfo;//String = fileName, list = Data
    private Reader reader;
    private ArrayList<int[]> swFileInfo;//0=ccode 1=year 2=level
    private HashMap<String,HashMap<Integer,HashMap<String,Double>>> yearData;//String=fileName hashInteger=year hashString = ccode hashDouble = edgeSum
    private HashMap<String,HashMap<Integer,Double>> corInfos;//String=fileName mapInteger=year double=val
   
    private HashMap<Integer,HashMap<String,Double>> metricInfos;//Integer = year,String ccode doıuble = val

    private HashMap<Integer,Double> levelRank;//Integer level =0,1,2,3 double = val
    private HashMap<Integer,HashMap<Integer,Double>> levelRankPerYear = new HashMap<>();//Integer level =0,1,2,3 double = val
    private HashMap<String,HashMap<Integer,Double>> fileRank;//0 alliance 1 common ... 10 war

    private ArrayList<Data> interCCodes;

    public wholeProject(){
        everyInfo = new HashMap<>();
        reader = new Reader();
        swFileInfo = new ArrayList<>();
        yearData = new HashMap<>();
        corInfos = new HashMap<>();
        metricInfos = new HashMap<>();
        levelRank = new HashMap<>();
        fileRank = new HashMap<>();
        interCCodes = new ArrayList<>();
    }

    public void run() throws IOException{
        reader.input();
        reader.fileReader(everyInfo);
        reader.swReader(swFileInfo);
        fillYearData();
        cor();
        normYearData();
        metric();  
        rank();
        fillInterArr();
        fileRank();
        csvWriter();             
    }
    private void fillYearData(){
        for (String fileName : everyInfo.keySet()) {
            ArrayList<Data> fileData = everyInfo.get(fileName);
            HashMap<Integer,HashMap<String,Double>>  yearDataMap = new HashMap<>();

            for (Data data : fileData) {
                fillYearDataAux(data,yearDataMap);
            }
            yearData.put(fileName, yearDataMap);
        }
    }
    private void  fillYearDataAux(Data data, HashMap<Integer,HashMap<String,Double>>  yearDataMap){
        if(yearDataMap.containsKey(data.getYear())){
            HashMap<String,Double> map = yearDataMap.get(data.getYear());
            if(map.containsKey(data.getCode1())){
                map.put(data.getCode1(), map.get(data.getCode1()) + data.getEdge());
            }
            else{
                map.put(data.getCode1(),data.getEdge());
            }
            if(map.containsKey(data.getCode2())){
                map.put(data.getCode2(), map.get(data.getCode2()) + data.getEdge());
            }
            else{
                map.put(data.getCode2(),data.getEdge());
            }
        }
        else{
            HashMap<String,Double> map = new HashMap<>();
            map.put(data.getCode1(), data.getEdge());
            map.put(data.getCode2(), data.getEdge());
            yearDataMap.put(data.getYear(), map);
        }
    }

    private void cor(){
        for (String fileName : yearData.keySet()) {
            HashMap<Integer,Double> corInfoVal =  new HashMap<>();
            for (Integer year : yearData.get(fileName).keySet()) {    
                corAux(corInfoVal,fileName,year);
            }
            corInfos.put(fileName, corInfoVal);
        }
    }
    private void corAux(HashMap<Integer,Double> tmp,String fileName,int year){
       
        double val = corNumer(year,fileName)/corDenum(year,fileName);
        if(!Double.isNaN(val)){
            tmp.put(year, val);
        }
    }
    private double corNumer(int year, String fileName){
        double swAvg = swAvgYear(year);
        double mapAvg = yearDataAvg(year, fileName);
        double result = 0.0;

        HashMap<String,Double> tmpData = yearData.get(fileName).get(year);   

        for (int[] ds : swFileInfo) {
            int tmpYear = ds[1];
            if(tmpYear == year){
                int ccode = ds[0];
                if(tmpData.get(Integer.toString(ccode)) != null){
                    result += ((ds[2]-swAvg)*(tmpData.get(Integer.toString(ccode)) - mapAvg));
                }
            }
        }
        return result;
    }
    private double corDenum(int year, String fileName){
        double swAvg = swAvgYear(year);
        double mapAvg = yearDataAvg(year, fileName);
        
       
        double firstPart = 0;
        double secondPart = 0;

        HashMap<String,Double> tmpData = yearData.get(fileName).get(year); 

        for (int[] ds : swFileInfo) {
            int tmpYear = ds[1];
            if(tmpYear == year){
                int ccode = ds[0];
                if(tmpData.get(Integer.toString(ccode)) !=null){
                    double tmp1 = ds[2] - swAvg; 
                    double tmp2 = tmpData.get(Integer.toString(ccode)) - mapAvg;
                    firstPart +=  tmp1 * tmp1;
                    secondPart += tmp2 * tmp2;
                }
            }
        }
        
        return Math.sqrt(firstPart) * Math.sqrt(secondPart);
    }
    private double swAvgYear(int year){
        double swAvg = 0;
        double size = 0;

        for (int[] ds : swFileInfo) {
            int tmpYear = ds[1];
            if(year == tmpYear){
                size++;
                swAvg+=ds[2];
            }
        }
        return swAvg / size;
    }
    private double yearDataAvg(int year, String fileName){
        double sum=0;
        HashMap<String,Double> map = yearData.get(fileName).get(year);
        for (Double d : map.values()) {
            sum+=d;
        }
        return sum/map.size();
    }

    private void normYearData(){
        for (String fileName : yearData.keySet()) {
            for (Integer year : yearData.get(fileName).keySet()){
                HashMap<String,Double> tmp = yearData.get(fileName).get(year);
                normYearDataAux(tmp);
            }
        }
    }
    private void normYearDataAux(HashMap<String,Double> map){
        double min = minValMap(map);
        double max = maxValMap(map);
        double secondPart = max - min; 

        for (String str : map.keySet()) {
            double x = map.get(str);
            double firsPart = x - min;
            map.put(str, 2*(firsPart/secondPart)-1);
        }
    }
    private double minValMap(HashMap<String,Double> map){
        double min = Double.MAX_VALUE;
        for (Double i : map.values()) {
            if(i < min){
                min = i;
            }
        }
        return min;
    }
    private double maxValMap(HashMap<String,Double> map){
        double max = Double.MIN_VALUE;
        for (Double i : map.values()) {
            if(i > max){
                max = i;
            }
        }
        return max;
    }

    private void metric(){
        ArrayList<Integer> yearRange = reader.getYearRange();
        for (int i = 2; i < yearRange.size(); i++) {
            HashMap<String,Double> tmp = new HashMap<>();
            for (String fileName : yearData.keySet()) {
                HashMap<String,Double> cCodeValPair = yearData.get(fileName).get(yearRange.get(i));
                double corVal = corInfos.get(fileName).get(yearRange.get(i) - 1);
                for (String ccode : cCodeValPair.keySet()) {
                    double val = cCodeValPair.get(ccode);
                    double tmpMetric = corVal * val;
                    if(tmp.containsKey(ccode)){
                        tmp.put(ccode, tmp.get(ccode) + tmpMetric);
                    }
                    else{
                        tmp.put(ccode, tmpMetric);
                    }
                }
                    
                
            }
            metricInfos.put(yearRange.get(i), tmp);   
        }
    }
    private void rank(){
        HashMap<Integer,HashMap<String,Double>> metricValScaled = scale0to100(metricInfos);
        int layer0Counter = 0;
        int layer1Counter = 0;
        int layer2Counter = 0;
        int layer3Counter = 0;
        double l0 = 0;
        double l1 = 0;
        double l2 = 0;
        double l3 = 0;

        for (int[] ds : swFileInfo) {
            int year = ds[1];
            
            if(reader.getYearRange().contains(year)){
                String ccode = Integer.toString(ds[0]);
                int level = ds[2];
                double val = returnMetricVal(metricValScaled, year,ccode);
                if(level == 0){
                    l0+=val;
                    layer0Counter++;
                }
                if(level == 1){
                    l1+=val;
                    layer1Counter++;
                }
                if(level == 2){
                    l2+=val;
                    layer2Counter++;
                }
                if(level == 3){
                    l3+=val;
                    layer3Counter++;
                }
            }
        }

        levelRank.put(0, l0/layer0Counter);
        levelRank.put(1, l1/layer1Counter);
        levelRank.put(2, l2/layer2Counter);
        levelRank.put(3, l3/layer3Counter);

        for (int i = 2; i < reader.getYearRange().size(); i++) {
            int lC0 = 0;
            int lC1 = 0;
            int lC2 = 0;
            int lC3 = 0;

            double r0 = 0;
            double r1 = 0;
            double r2 = 0;
            double r3 = 0;
            HashMap<Integer,Double> tmpMap = new HashMap<>();

            int year = reader.getYearRange().get(i);
            for (int[] ds : swFileInfo){
                int tmpYear = ds[1];
                if(reader.getYearRange().contains(tmpYear)){
                    if(tmpYear == year){
                        String ccode = Integer.toString(ds[0]);
                        int level = ds[2];
                        double val = returnMetricVal(metricValScaled, year,ccode);
                        if(level == 0){
                            r0+=val;
                            lC0++;
                        }
                        if(level == 1){
                            r1+=val;
                            lC1++;
                        }
                        if(level == 2){
                            r2+=val;
                            lC2++;
                        }
                        if(level == 3){
                            r3+=val;
                            lC3++;
                        }
                    }
                }
            }
            tmpMap.put(0, r0/lC0);
            tmpMap.put(1, r1/lC1);
            tmpMap.put(2, r2/lC2);
            tmpMap.put(3, r3/lC3);

            levelRankPerYear.put(year, tmpMap);
        }
    }
    
    
    private double returnMetricVal( HashMap<Integer,HashMap<String,Double>> metricValScaled,int year,String ccode){
        double val = 0.0;
        for (Integer tmpYear : metricValScaled.keySet() ) {
            if(tmpYear == year){
                for (String str : metricValScaled.get(year).keySet()) {
                    if(str.equals(ccode)){
                        return metricValScaled.get(year).get(ccode);
                    }
                }
                break;
            }    
        }
        return val;
    }
    private HashMap<Integer,HashMap<String,Double>> scale0to100(HashMap<Integer,HashMap<String,Double>> metricInfos1){
        HashMap<Integer,HashMap<String,Double>> tmp = new HashMap<>();

        for (Integer year : metricInfos1.keySet()) {
            HashMap<String,Double> tmpInfo = new HashMap<>();

            HashMap<String,Double> tmpVal = metricInfos1.get(year);

            ArrayList<String[]> sorted = sortMap(tmpVal);
           

            double scale = 0;
            double increment = 100.0/(sorted.size()-1);

            for (int i = 0; i < sorted.size(); i++){
                tmpInfo.put(sorted.get(i)[0], scale);
                if( i == sorted.size() - 1){
                    break;
                }
                if(!sorted.get(i)[1].equals(sorted.get(i+1)[1])){
                    scale += increment;
                }
            }
            tmp.put(year, tmpInfo);
        }
        return tmp;
    }
    private ArrayList<String[]> sortMap( HashMap<String,Double> tmpVal){
        ArrayList<String[]> sorted = new ArrayList<>();
        
        for (String ccode : tmpVal.keySet()) {
            String[] tmp = new String[2];
            tmp[0] = ccode;
            tmp[1] = Double.toString(tmpVal.get(ccode));
            
            if(sorted.isEmpty()){
                sorted.add(tmp);
            }
            else{
                boolean isAdded = false;
                for (int i = 0; i < sorted.size(); i++) {
                    double val = Double.parseDouble(sorted.get(i)[1]);
                    if(tmpVal.get(ccode) <= val){
                        sorted.add(i, tmp);
                        isAdded = true;
                        break;
                    }
                }
                if(!isAdded){
                    sorted.add(tmp);
                }
            }
        }
        return sorted;
    }




    private void fileRank(){
        HashMap<Integer,HashMap<String,Double>> metricValScaled = scale0to100(metricInfos);
        for (String fName : everyInfo.keySet()) {
            HashMap<Integer,Double> map = new HashMap<>(); 
            for (int i = 2; i < reader.getYearRange().size(); i++) {
                int tmpCounter = 0;
                double tmpVal = 0;
                double result = 0;

                int yearR = reader.getYearRange().get(i);
                for (Data data : everyInfo.get(fName)){
                    int year = data.getYear(); 
                    if(year == yearR){
                        if(metricValScaled.containsKey(year)){
                            String ccode1 = data.getCode1();
                            String ccode2 = data.getCode2();
                            if(metricValScaled.get(year).containsKey(ccode1)){                    
                                double val = metricValScaled.get(year).get(ccode1);
                                tmpCounter++;
                                tmpVal+=val;      
                            }
                            if(metricValScaled.get(year).containsKey(ccode2)){                    
                                double val = metricValScaled.get(year).get(ccode2);
                                tmpCounter++;
                                tmpVal+=val;      
                            }
                        }
                    }
                }
                result = tmpVal/tmpCounter;
                map.put(yearR, result);
            }
            fileRank.put(fName, map);
        }
        HashMap<Integer,Double> map = new HashMap<>(); 
        for (int i = 2; i < reader.getYearRange().size(); i++){
            int yearR = reader.getYearRange().get(i);
            int tmpCounter = 0;
            double tmpVal = 0;
            double result = 0;

            for (Data data : interCCodes) {
                int year = data.getYear();
                if(year == yearR){
                    if(metricValScaled.containsKey(year)){
                        String ccode1 = data.getCode1();
                        String ccode2 = data.getCode2();
                        if(metricValScaled.get(year).containsKey(ccode1)){                    
                            double val = metricValScaled.get(year).get(ccode1);
                            tmpCounter++;
                            tmpVal+=val;      
                        }
                        if(metricValScaled.get(year).containsKey(ccode2)){                    
                            double val = metricValScaled.get(year).get(ccode2);
                            tmpCounter++;
                            tmpVal+=val;      
                        }   
                    }
                }
            }
            result = tmpVal/tmpCounter;
            map.put(yearR, result);
        }
        fileRank.put("NEG-NUC INTERACTION", map);

    }

    private void fillInterArr(){
        for (String fName1 : everyInfo.keySet()) {
            for (String fName2 : everyInfo.keySet()) {
                if(fName1.equals("files/negotiations.csv") && fName2.equals("files/nuclear_transfers.csv")){
                    for (Data data1 : everyInfo.get(fName1)) {
                        for (Data data2 :everyInfo.get(fName2)) {
                            if(data1.getYear() == data2.getYear()){
                                if( (data1.getCode1().equals(data2.getCode1())) && (data1.getCode2().equals(data2.getCode2())) ){
                                    interCCodes.add(new Data(data1.getCode1(), data1.getCode2(), data1.getYear(), data1.getEdge()));
                                }
                                if( (data1.getCode1().equals(data2.getCode2())) && (data1.getCode2().equals(data2.getCode1())) ){
                                    interCCodes.add(new Data(data1.getCode1(), data1.getCode2(), data1.getYear(), data1.getEdge()));
                                }
                            }
                        }
                    }
                }
            }
        }
        simpInterArr();
    }
    private void simpInterArr(){
        for (int i = 0; i < interCCodes.size(); i++) {
            for (int j = i+1; j < interCCodes.size(); j++) {
                if(isEqualData(interCCodes.get(i), interCCodes.get(j))){
                    interCCodes.remove(j);
                    j--;
                }
            }
        }
    }
    private boolean isEqualData(Data data1,Data data2){
        if(data1.getCode1().equals(data2.getCode1()) && data1.getCode2().equals(data2.getCode2()) && data1.getYear() == data2.getYear()){
            return true;
        }
        return false;
    }
    

    private void csvWriter() throws IOException{
        FileWriter writer = new FileWriter(new File("Ccode_Level_Metric_Year.csv"));

        for (int[] data : swFileInfo) {
            int year = data[1];
            if(metricInfos.containsKey(year)){
                String ccode = Integer.toString(data[0]);
                if(metricInfos.get(year).containsKey(ccode)){
                    int level = data[2];
                    writer.write(ccode + "," + level + "," + metricInfos.get(year).get(ccode) + "," + year + "\n");
                }
            }
        }
        writer.close();
        writer = new FileWriter(new File("CorInfos.csv"));
        
        for (String name : corInfos.keySet()) {
            double avg = 0;
            writer.write(name + "\n");
            for (Integer year : corInfos.get(name).keySet()) {
                writer.write(year + "," + corInfos.get(name).get(year) + "\n");
                avg+=corInfos.get(name).get(year);
            }
            writer.write("Avg," + avg/corInfos.get(name).size() + "\n\n");    
        }
        writer.close();
    }
}
