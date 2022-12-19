package Coupling_Graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.lang.*;

import Coupling_Graph.InvocationGraph;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import java.util.Scanner;

public class Parser {
	
	public static final String projectPath42 = "C:\\Users\\HP\\Desktop\\Automaton\\src";
	public static final String projectPath2 = "C:\\Users\\HP\\eclipse-workspace\\Program_understanding\\src";
	public static final String projectPath = "C:\\Users\\HP\\Downloads\\CorrectionTP2_Partie1 (1)\\CorrectionTP1_Partie1\\step2\\src";
	public static final String jrePath = "C:\\Program Files\\Java\\jdk-11.0.1\\lib\\jrt-fs.jar";
	public static final String projectPath3 = "C:\\Users\\HP\\Downloads\\project2\\project2\\src";
	
	public static void main(String[] args) throws IOException {
		
		
		
		System.out.println("Entrez le chemin vers l'SRC de votre projet JAVA: ");
		Scanner path_scanner= new Scanner(System.in);
		String project_path= path_scanner.nextLine();
		System.out.println("Entrez le chemin vers le jre: ");
		Scanner jre_scanner= new Scanner(System.in);
		String jre_path= jre_scanner.nextLine();
		// read java files
		final File folder = new File(project_path);
		
		//List all files of the project
		ArrayList<File> javaFiles = listJavaFilesForFolder(folder);
		
		
		//List of the classes of the project
		HashSet<String> classes= new HashSet<String>();
		//List of the methods of the project
		HashSet<MethodDeclaration> methods= new HashSet<MethodDeclaration>();
		//Map of classes and the number of their methods
		LinkedHashMap<Name, Integer> mpclass = new LinkedHashMap<Name, Integer>();
		//Map of classes and the number of their attributes
		LinkedHashMap<Name, Integer> fpclass = new LinkedHashMap<Name, Integer>();
		//Map of methods and the number of their parameters
		
		CoupleGraph invocG= new CoupleGraph();
		
		
		for (File fileEntry : javaFiles) {
			String content = FileUtils.readFileToString(fileEntry);
			CompilationUnit parse = parse(content.toCharArray(), project_path,jre_path);
			collectTypeDeclarationInfo(parse,classes);
			
		}
		for (File fileEntry : javaFiles) {		
			String content = FileUtils.readFileToString(fileEntry);
			CompilationUnit parse = parse(content.toCharArray(), project_path,jre_path);
			collectMethodInvocationInfo(parse,invocG, classes);		
		}
	
		
		
		System.out.println(classes);
		
		String menu= "*********** Menu Principal de l'application ***************\n1- Nombre de classes de l'application \n2- Construire le graphe de couplage de l'application \n3- Afficher les regroupement hiérarchique possible des classes  \n4- Quitter";
		System.out.println(menu);
		Boolean out=false;
		while(!out) {
			
	        System.out.print("Choisissez l'opération que vous voulez faire: ");
	        Scanner sc= new Scanner(System.in);
	        int in=sc.nextInt();
	        
			
			switch(in) {
				case(1):
					System.out.println("Nombre de classes du projet :"+ classes.size());
					out=false;
					break;
				
				case(2):
					
					//invocG.printCoupleGraph();
					invocG.writeCouplingGraphInDotFile(projectPath3 +"graph.dot");
					System.out.println("Generating the graph, please ignore the warnings...");
					invocG.convertDotToSVG(projectPath3 +"graph.dot");
					System.out.println("Graphe de couplage construit est sauvegardé dans un fichier .SVG dans le dossier du projet!");
					out=false;
					break;
				case(3):
					invocG.initial_hirerarchy();
					System.out.println("********************************************************");
					System.out.println(invocG.coupled_classes);
					System.out.println("Voici la liste des hiérarchies de classe pour chaque étape: ");
					invocG.cluster_steps(invocG.coupled_classes.size()-1);
					System.out.println("********************************************************");
					System.out.println("Voici la liste des moyennes du couplages (CP) pour chaque étape: ");
					System.out.println(invocG.cp_map);
					int max = classes.size()/2 ;
					System.out.println("Le nombre maximal des modules est: " + max);
					System.out.print("Donner le paramètre CP (Moyenne de couplage entre chaque deux classes des modules, la valeur est entre 0 et 1 et utilisez le ',' comme délimiteur) : ");
			        Scanner sc2= new Scanner(System.in);
			        float cp=sc2.nextFloat();
			        System.out.println("parameter is "+ cp);
					invocG.hierarchyCP(max, cp);
					out=false;
					break;
				case(4):
					
				default:
					sc.close();
					out=true;
					
			}
			
		}
		
	}

	// read all java files from specific folder
	public static ArrayList<File> listJavaFilesForFolder(final File folder) {
		ArrayList<File> javaFiles = new ArrayList<File>();
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				javaFiles.addAll(listJavaFilesForFolder(fileEntry));
			} else if (fileEntry.getName().contains(".java")) {
				// System.out.println(fileEntry.getName());
				javaFiles.add(fileEntry);
			}
		}

		return javaFiles;
	}

	// create AST
	private static CompilationUnit parse(char[] classSource, String project_Path, String jre_Path) {
		ASTParser parser = ASTParser.newParser(AST.JLS4); // java +1.6
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
 
		parser.setBindingsRecovery(true);
 
		Map options = JavaCore.getOptions();
		parser.setCompilerOptions(options);
 
		parser.setUnitName("");
 
		String[] sources = { project_Path }; 
		String[] classpath = {jre_Path};
 
		parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
		parser.setSource(classSource);
		
		return (CompilationUnit) parser.createAST(null); // create and parse
	}

	
	
	// navigate method invocations inside method
		public static void collectMethodInvocationInfo(CompilationUnit parse, CoupleGraph invocG, HashSet<String> classes) {
			
			
			MethodDeclarationVisitor visitor1 = new MethodDeclarationVisitor();
			parse.accept(visitor1);
			for (MethodDeclaration method : visitor1.getMethods()) {
				
				
				MethodInvocationVisitor visitor2 = new MethodInvocationVisitor();
				method.accept(visitor2);
				
				
				
				if(visitor2.getMethods().size()!=0) {
				
					for (MethodInvocation methodInvocation : visitor2.getMethods()) {
						if(methodInvocation.getExpression()!=null) {
							if (methodInvocation.getExpression().resolveTypeBinding() != null) {
								
								if(classes.contains(methodInvocation.getExpression().resolveTypeBinding().getName().toString())) {
									String key= method.resolveBinding().getDeclaringClass().getName().toString();
									String value= methodInvocation.getExpression().resolveTypeBinding().getName().toString();	
									invocG.addCoupling(key,value );
								}
				
							}
						}
						
						else if(methodInvocation.resolveMethodBinding()!=null) {
							
							if(classes.contains(methodInvocation.resolveMethodBinding().getDeclaringClass().getName())) {
							String key= method.resolveBinding().getDeclaringClass().getName().toString();
							String value= methodInvocation.resolveMethodBinding().getDeclaringClass().getName().toString(); 
							invocG.addCoupling(key,value );
						}
							}
						
					}
				}
				
			}
			
		}
		
		
		// navigate class information
		public static void collectTypeDeclarationInfo(CompilationUnit parse, HashSet<String> classes) {
			TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
			parse.accept(visitor);

			for (TypeDeclaration t : visitor.getTypes()) {
				if(!t.isInterface()) {
				classes.add(t.resolveBinding().getName());}
			}

		}	
		
		
		// function to sort LinkedHashMap by values
	    public static LinkedHashMap<Name, Integer> sortByValue(LinkedHashMap<Name, Integer> hm)
	    {
	        // Create a list from elements of LinkedHashMap
	        List<Map.Entry<Name, Integer> > list =
	               new LinkedList<Map.Entry<Name, Integer> >(hm.entrySet());
	 
	        // Sort the list
	        Collections.sort(list, new Comparator<Map.Entry<Name, Integer> >() {
	            public int compare(Map.Entry<Name, Integer> o1,
	                               Map.Entry<Name, Integer> o2)
	            {
	                return (-1)*(o1.getValue()).compareTo(o2.getValue());
	            }
	        });
	         
	        // put data from sorted list to LinkedHashMap
	        LinkedHashMap<Name, Integer> temp = new LinkedHashMap<Name, Integer>();
	        for (Map.Entry<Name, Integer> aa : list) {
	            temp.put(aa.getKey(), aa.getValue());
	        }
	        return temp;
	    }
	 
	  
	    //Gets the average of map integer values
	    public static int mapAverage(LinkedHashMap<Name,Integer> map) {
	    	int total=0; 
	    	
	    	for(Map.Entry<Name,Integer> entry : map.entrySet()) {
	    		
	    		total+= entry.getValue();
	    	}
	    	
	    	return (total/map.size());
	    }
	    
	    
	   
	    
	    
}