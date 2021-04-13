package webdata;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.FileNotFoundException;



public class main {

    public static void main(String[] args)
    {
        SlowIndexWriter sw = new SlowIndexWriter();
        sw.slowWrite("C:\\Users\\USER\\IdeaProjects\\webdata_project\\src\\webdata\\100.txt", "");
    }
}
