
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.github.javaparser.JavaParser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.YamlPrinter;
import com.sun.tools.javac.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Inspect {	
    public static void main(String[] args) throws IOException {
    	
    	File OringinalFile = new File("C:\\Users\\nvuggam\\Desktop\\Original.java");
         CompilationUnit cuOriginal = StaticJavaParser.parse(OringinalFile); 
		 File f1 = new File("C:\\Users\\nvuggam\\Desktop\\Original_AST.txt");
		 PrintStream o = new PrintStream(f1); System.setOut(o); YamlPrinter printer =
		 new YamlPrinter(true); //System.out.println(printer.output(cuOriginal));
		 
        
        File RevisedFile = new File("C:\\Users\\nvuggam\\Desktop\\Revised.java");
		CompilationUnit cuRevised = StaticJavaParser.parse(RevisedFile);
		File f2 = new File("C:\\Users\\nvuggam\\Desktop\\Revised_AST.txt");
		PrintStream h = new PrintStream(f2); System.setOut(h);
		System.out.println(printer.output(cuRevised));
		System.setOut(o);
		Node.BreadthFirstIterator iterator = new Node.BreadthFirstIterator(cuOriginal);
        while (iterator.hasNext()) {
            System.out.println("* " + iterator.next());
        }
        
        java.util.List<String> original = Files.readAllLines(OringinalFile.toPath());
        java.util.List<String> revised = Files.readAllLines(RevisedFile.toPath());

        Patch<String> patch = DiffUtils.diff(original, revised);
        
        File f3 = new File("C:\\Users\\nvuggam\\Desktop\\Diff.txt");
        PrintStream j = new PrintStream(f3);
        System.setOut(j);
        
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            System.out.println(delta);
        }   
        
        File f4 = new File("C:\\Users\\nvuggam\\Desktop\\Inserted.txt");
        File f5 = new File("C:\\Users\\nvuggam\\Desktop\\Modified.txt");
        File f6 = new File("C:\\Users\\nvuggam\\Desktop\\Deleted.txt");
        PrintStream C = new PrintStream(f5);
        PrintStream I = new PrintStream(f4);
        PrintStream D = new PrintStream(f6);
        
        //class_check
        Pattern ClassPattern = Pattern.compile("(?:public\\s)?(class|interface|enum)\\s([^\\s]+)");
        BufferedReader r = new BufferedReader(new FileReader(f3));
        String line;
        int count =0;
        while ((line = r.readLine()) != null) {
            Matcher m = ClassPattern.matcher(line);
            char current = line.charAt(1);
            while (m.find()) {
            	if(current == 'C')
            	{
            		System.setOut(C);
                    if(count%2 == 1)
                    System.out.println("class " + m.group(2));                    
            	}
            	else if(current == 'I')
            	{
            		
                    System.setOut(I);
                    System.out.println("class " + m.group(2));
            	}
            	else if(current == 'D')
            	{
            		
                    System.setOut(D);
                    System.out.println("class " + m.group(2));
            	}
            	count++;
            }
        }
        
        //Method_check
        Pattern MethodPattern = Pattern.compile("(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\]]+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])");
        BufferedReader s = new BufferedReader(new FileReader(f3));
        String Methodline;
        count =0;
        while ((Methodline = s.readLine()) != null) {
            Matcher m = MethodPattern.matcher(Methodline);
            char current = Methodline.charAt(1);
            while (m.find()) {
            	
            	if(current == 'C')
            	{
            		
                    System.setOut(C);
                    if(count%2 == 1)
                    System.out.println("Method " + m.group(2));                    
            	}
            	else if(current == 'I')
            	{
            		
                    System.setOut(I);
                    System.out.println("Method " + m.group(2));
            	}
            	else if(current == 'D')
            	{
            		
                    System.setOut(D);
                    System.out.println("Method " + m.group(2));
            	}
            	count++;
                
            }
        }
       
        //LOOP_check
        Pattern LoopPattern = Pattern.compile("\\d+");
        BufferedReader t = new BufferedReader(new FileReader(f3));
        String Loopline;
        count =0;
        while ((Loopline = t.readLine()) != null) 
        {
        	Matcher m = LoopPattern.matcher(Loopline);
        	while(m.find()) 
        	{
        		char current = Loopline.charAt(1);
            	if(current == 'C' && (Loopline.contains("while")|| Loopline.contains("for") || Loopline.contains("if") ) )
            	{
                    System.setOut(C);                 
                    if(Loopline.contains("while"))
                    	System.out.println("WhileLoopAt " + m.group(0));     
                    else if(Loopline.contains("for"))
                    	System.out.println("ForLoopAt " + m.group(0) );
                    else
                    	System.out.println("IfConditionAt " + m.group(0) );
            	}
            	else if(current == 'I' && (Loopline.contains("while")|| Loopline.contains("for") ) )
            	{
                    System.setOut(I);
                    if(Loopline.contains("while"))
                    	System.out.println("WhileLoopAt " + m.group(0));     
                    else if(Loopline.contains("for"))
                    	System.out.println("ForLoopAt " + m.group(0) );
                    else
                    	System.out.println("IfConditionAt " + m.group(0) );
            	}
            	else if(current == 'D' && (Loopline.contains("while")|| Loopline.contains("for") ) )
            	{
                    System.setOut(D);
                    if(Loopline.contains("while"))
                    	System.out.println("WhileLoopAt " + m.group(0));     
                    else if(Loopline.contains("for"))
                    	System.out.println("ForLoopAt " + m.group(0) );
                    else
                    	System.out.println("IfConditionAt " + m.group(0) );
            	}
            	count++;
                break;
        	}
        }  
        
      //Variable_Check
      
        
        
    }
}

