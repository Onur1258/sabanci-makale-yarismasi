import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Reader {
    
    private ArrayList<Integer> yearRange = new ArrayList<>();
    private ArrayList<String> fileNames = new ArrayList<>();
    
    public Reader(){}


    public void input(){
        Scanner k = new Scanner(System.in);
        fileNames.add("files/alliance.csv");
        fileNames.add("files/common_enemy.csv");
        fileNames.add("files/economic_interdependence.csv");
        fileNames.add("files/joint_democracy.csv");
        fileNames.add("files/negotiations.csv");
        fileNames.add("files/nuclear_transfers.csv");
        fileNames.add("files/mid_directeddyads.csv");
        fileNames.add("files/sanctions.csv");
        fileNames.add("files/trade.csv");
        

        System.out.println("Enter starting year:");
        int sYear = k.nextInt();

        System.out.println("Enter finishing year:");
        int fYear = k.nextInt();

        for (int i = sYear; i <= fYear; i++) {
            yearRange.add(i);
        }

        k.close();
    }
    
    public void fileReader(HashMap<String,ArrayList<Data>> everyInfo) throws FileNotFoundException{
        for (String fileName : fileNames) {//0=ccode1 1=ccode2 2=year 3=edge
            
            Scanner k = new Scanner(new File(fileName));
            k.nextLine();
            ArrayList<Data> tmp = new ArrayList<>();

            while(k.hasNextLine()){
                String[] tmpLine = k.nextLine().split(",");
                if(tmpLine.length >=4 ){
                    String ccode1 = tmpLine[0];
                    String ccode2 = tmpLine[1];
                    int year = Integer.parseInt(tmpLine[2]) ;
                    if(!tmpLine[3].equals("") && yearContains(year)){
                        double edge = Double.parseDouble(tmpLine[3]);
                        tmp.add(new Data(ccode1, ccode2, year, edge)); 
                    } 
                }
            }
            everyInfo.put(fileName, tmp);
            k.close();
        }
    }

    public void swReader(ArrayList<int[]> swFile) throws FileNotFoundException{
        Scanner k = new Scanner(new File("files/proliferation.csv"));
        k.nextLine();
        while(k.hasNextLine()){
            String[] tmpLine = k.nextLine().split(",");
            
            String level = tmpLine[0];
           
            if(!level.equals("")){
                int[] tmpArr = new int[3];
                String ccode1 = tmpLine[1];
                String year = tmpLine[2];
                tmpArr[0] = Integer.parseInt(ccode1);
                tmpArr[1] = Integer.parseInt(year);
                tmpArr[2] = Integer.parseInt(level);
                swFile.add(tmpArr);
            }
        }
        k.close();
    }

    public ArrayList<Integer> getYearRange(){
        return yearRange;
    }

    public boolean yearContains(int year){
        for (Integer i : yearRange) {
            if(i == year){
                return true;
            }
        }
        return false;
    }
}
