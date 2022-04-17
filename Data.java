public class Data {
    private String ccode1;
    private String ccode2;
    private int year;
    private double edge;
    
    public Data(String ccode1,String ccode2,int year,double edge){
        this.ccode1 = ccode1;
        this.ccode2 = ccode2;
        this.year = year;
        this.edge = edge;
    }


    public String getCode1(){
        return this.ccode1;
    }
    public String getCode2(){
        return this.ccode2;
    }

    public int getYear(){
        return this.year;
    }
    public double getEdge(){
        return this.edge;
    }
    
    public void setEdge(double d){
        edge = d;
    }

   
    public void printData(){
        System.out.println(ccode1+"    " + ccode2+ "  " + year+ "   " + edge);
    }
}
